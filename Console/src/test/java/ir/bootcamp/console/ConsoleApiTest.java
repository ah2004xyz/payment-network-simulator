package ir.bootcamp.console;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class ConsoleApiTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final ConsoleApi api = new ConsoleApi(mapper, mock(MongoClient.class),
            mock(JdbcTemplate.class), mock(ConnectionFactory.class));

    @Test
    void rejectsNonObjectRequestBody() {
        var response = api.execute(new ConsoleApi.ExecuteRequest("psp", mapper.createArrayNode()));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void rejectsUnknownServiceWithoutMakingExternalCall() {
        var response = api.execute(new ConsoleApi.ExecuteRequest("arbitrary-url", mapper.createObjectNode()));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void rejectsMissingExecution() {
        var response = api.execution("unknown-trace");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
