package ir.bootcamp.console;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.MongoClient;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

@RestController
@RequestMapping("/api")
public class ConsoleApi {
    private static final int HISTORY_LIMIT = 100;
    private final ObjectMapper mapper;
    private final MongoClient mongo;
    private final JdbcTemplate jdbc;
    private final ConnectionFactory rabbit;
    private final Deque<Map<String, Object>> executions = new ConcurrentLinkedDeque<>();
    private final Deque<Map<String, Object>> logs = new ConcurrentLinkedDeque<>();

    @Value("${console.psp-url}") private String pspUrl;
    @Value("${console.bank-a-url}") private String bankAUrl;
    @Value("${console.bank-b-url}") private String bankBUrl;
    @Value("${console.shaparak-url}") private String shaparakUrl;

    public ConsoleApi(ObjectMapper mapper, MongoClient mongo, JdbcTemplate jdbc, ConnectionFactory rabbit) {
        this.mapper = mapper;
        this.mongo = mongo;
        this.jdbc = jdbc;
        this.rabbit = rabbit;
    }

    public record ExecuteRequest(String service, JsonNode body) {}

    @GetMapping("/system")
    public Map<String, Object> system() {
        Map<String, Object> statuses = new LinkedHashMap<>();
        statuses.put("psp", checkHttp(pspUrl + "/actuator/health"));
        statuses.put("shaparak", checkHttp(shaparakUrl + "/actuator/health"));
        statuses.put("bankA", checkHttp(bankAUrl + "/actuator/health"));
        statuses.put("bankB", checkHttp(bankBUrl + "/actuator/health"));
        statuses.put("mongodb", check(() -> mongo.getDatabase("psp").runCommand(new Document("ping", 1))));
        statuses.put("mysqlBankA", check(() -> jdbc.queryForObject("SELECT COUNT(*) FROM bank_a.account_a", Long.class)));
        statuses.put("mysqlBankB", check(() -> jdbc.queryForObject("SELECT COUNT(*) FROM bank_b.account_b", Long.class)));
        statuses.put("rabbitmq", check(() -> {
            var connection = rabbit.createConnection();
            try { return connection.isOpen(); } finally { connection.close(); }
        }));
        return Map.of("checkedAt", Instant.now().toString(), "services", statuses,
                "messaging", Map.of("exchange", "payment.exchange", "routingKey", "shaparak.purchase", "queue", "payment.purchase.queue"));
    }

    @GetMapping("/executions")
    public List<Map<String, Object>> executions() { return List.copyOf(executions); }

    @GetMapping("/logs")
    public List<Map<String, Object>> logs() { return List.copyOf(logs); }

    @GetMapping("/executions/{id}")
    public ResponseEntity<?> execution(@PathVariable String id) {
        return executions.stream().filter(e -> id.equals(e.get("id"))).findFirst()
                .<ResponseEntity<?>>map(e -> ResponseEntity.ok(details(e)))
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "Execution not found in this console session")));
    }

    @PostMapping(value = "/execute", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> execute(@RequestBody ExecuteRequest request) {
        if (request == null || request.body() == null || !request.body().isObject()) {
            return ResponseEntity.badRequest().body(Map.of("error", "A JSON object body is required"));
        }
        String service = request.service();
        String url = switch (service == null ? "" : service) {
            case "psp" -> pspUrl + "/payment/purchase";
            case "bankA" -> bankAUrl + "/bank1";
            case "bankB" -> bankBUrl + "/bank2";
            default -> null;
        };
        if (url == null) return ResponseEntity.badRequest().body(Map.of("error", "Unsupported service"));

        String id = UUID.randomUUID().toString();
        ObjectNode payload = ((ObjectNode) request.body()).deepCopy();
        payload.put("traceId", id);
        String started = Instant.now().toString();
        long start = System.nanoTime();
        int status = 0;
        Object responseBody;
        log("INFO", "Console", "Request sent to " + service, id);
        try {
            HttpURLConnection call = (HttpURLConnection) URI.create(url).toURL().openConnection();
            call.setRequestMethod("POST");
            call.setConnectTimeout(2000);
            call.setReadTimeout(20000);
            call.setRequestProperty("Content-Type", "application/json");
            call.setDoOutput(true);
            try (var output = call.getOutputStream()) { output.write(mapper.writeValueAsBytes(payload)); }
            status = call.getResponseCode();
            String content;
            try (var input = status >= 400 ? call.getErrorStream() : call.getInputStream()) {
                content = input == null ? "" : new String(input.readAllBytes(), StandardCharsets.UTF_8);
            }
            try { responseBody = mapper.readTree(content); }
            catch (Exception ignored) { responseBody = content; }
            call.disconnect();
        } catch (Exception ex) {
            responseBody = Map.of("error", "Service unavailable or request timed out", "detail", ex.getClass().getSimpleName());
        }
        long duration = Duration.ofNanos(System.nanoTime() - start).toMillis();
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("id", id);
        entry.put("service", service);
        entry.put("url", url);
        entry.put("method", "POST");
        entry.put("headers", Map.of("Content-Type", "application/json"));
        entry.put("request", payload);
        entry.put("response", responseBody);
        entry.put("status", status);
        entry.put("durationMs", duration);
        entry.put("startedAt", started);
        executions.addFirst(entry);
        trim(executions);
        log(status >= 400 || status == 0 ? "ERROR" : "INFO", "Console", "Request completed with HTTP " + (status == 0 ? "unavailable" : status), id);
        return ResponseEntity.ok(details(entry));
    }

    private Map<String, Object> details(Map<String, Object> entry) {
        Map<String, Object> result = new LinkedHashMap<>(entry);
        String id = (String) entry.get("id");
        Map<String, Object> persistence = new LinkedHashMap<>();
        persistence.put("pspRequest", mongoRecord("psp", "payment_request", id));
        persistence.put("pspResponse", mongoRecord("psp", "payment_response", id));
        persistence.put("routingRequest", mongoRecord("shaparak", "routing_request", id));
        persistence.put("routingResponse", mongoRecord("shaparak", "routing_response", id));
        persistence.put("bankA", sqlRecords("SELECT transaction_id_a AS id, transaction_date AS transactionDate, amount, CASE status WHEN 0 THEN 'FAILED' WHEN 1 THEN 'SUCCESS' WHEN 2 THEN 'PROCESSING' WHEN 3 THEN 'PENDING' ELSE 'UNKNOWN' END AS status, source_card_number AS sourceCardNumber, target_card_number AS targetCardNumber FROM bank_a.transaction_a WHERE trace_id = ? ORDER BY transaction_id_a DESC LIMIT 5", id));
        persistence.put("bankB", sqlRecords("SELECT t.id, t.transaction_date AS transactionDate, t.amount, t.status, a.card_number AS sourceCardNumber FROM bank_b.transaction_b t JOIN bank_b.account_b a ON a.id=t.source_account_id WHERE t.trace_id = ? ORDER BY t.id DESC LIMIT 5", id));
        result.put("persistence", persistence);
        result.put("events", logs.stream().filter(l -> id.equals(l.get("traceId"))).toList());
        return result;
    }

    private Object mongoRecord(String database, String collection, String id) {
        try {
            Document doc = mongo.getDatabase(database).getCollection(collection)
                    .find(Filters.eq("traceId", id)).sort(Sorts.descending("_id")).first();
            if (doc == null) return null;
            doc.remove("_class");
            return mapper.readTree(doc.toJson());
        } catch (Exception ex) { return Map.of("unavailable", ex.getClass().getSimpleName()); }
    }

    private Object sqlRecords(String sql, String id) {
        try { return jdbc.queryForList(sql, id); }
        catch (Exception ex) { return Map.of("unavailable", ex.getClass().getSimpleName()); }
    }

    private String checkHttp(String url) {
        try {
            HttpURLConnection call = (HttpURLConnection) URI.create(url).toURL().openConnection();
            call.setConnectTimeout(2000);
            call.setReadTimeout(2000);
            if (call.getResponseCode() != 200) return "unavailable";
            String content;
            try (var input = call.getInputStream()) { content = new String(input.readAllBytes(), StandardCharsets.UTF_8); }
            call.disconnect();
            return "UP".equals(mapper.readTree(content).path("status").asText()) ? "healthy" : "unavailable";
        } catch (Exception ex) { return "unavailable"; }
    }

    private String check(java.util.concurrent.Callable<?> action) {
        try { Object value = action.call(); return Boolean.FALSE.equals(value) ? "unavailable" : "healthy"; }
        catch (Exception ex) { return "unavailable"; }
    }

    private void log(String level, String source, String message, String id) {
        logs.addFirst(Map.of("timestamp", Instant.now().toString(), "level", level,
                "source", source, "message", message, "traceId", id));
        trim(logs);
    }

    private <T> void trim(Deque<T> queue) {
        while (queue.size() > HISTORY_LIMIT) queue.pollLast();
    }
}
