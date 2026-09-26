# Payment Network Simulator

A Spring Boot system that simulates a card purchase moving through a PSP, the Shaparak payment network, and an issuing bank.

## Architecture

```text
Merchant
   |
   | POST /payment/purchase
   v
PSP :8084  -- RPC over RabbitMQ -->  Shaparak :8082  -- REST -->  Bank A :8080
   |                                      |                              |
MongoDB                                MongoDB                          MySQL / bank_a
                                                \
                                                 -- REST --> Bank B :8081
                                                               |
                                                           MySQL / bank_b

Developer Console :8090 -- fixed API proxy --> PSP / Bank A / Bank B
                       -- read-only inspection --> MongoDB / MySQL / RabbitMQ health
```

The PSP validates the purchase and merchant, stores the request, and sends an RPC message. Shaparak selects the bank from the card's first six digits, stores its request and response, and calls the bank. The bank locks the source account, checks the balance, transfers the amount, and records the transaction.

| Card prefix | Route |
| --- | --- |
| `111111` | Bank A at port `8080` |
| `222222` | Bank B at port `8081` |

## Tech stack

- Java 17 and Spring Boot 3.4
- Spring Web and Bean Validation
- RabbitMQ RPC messaging
- MongoDB for PSP and Shaparak request/response records
- MySQL and Spring Data JPA for bank accounts and transactions
- Maven multi-module build
- Local, browser-based developer console (Spring Boot with framework-free HTML/CSS/JavaScript)

## Developer console

Open `http://127.0.0.1:8090` after starting the services. The console is bound to loopback by default and is intended only for local development; it has no authentication. It provides:

- Six editable scenarios, including the full Bank A/Bank B purchase routes, PSP validation failures, unknown merchant, and a direct insufficient-balance bank request.
- A playground restricted to the three actual POST endpoints. Each request receives a generated trace ID.
- HTTP request/response inspection and trace-scoped MongoDB and MySQL records. The trace ID is persisted through PSP, Shaparak, and the selected bank for new requests. Older records have no trace ID.
- Console-generated request events, search and filtering. These are **not** the application or Docker logs. RabbitMQ topology comes from code; no per-message broker history is claimed.
- Spring Actuator health for the four services and direct console probes for MongoDB, MySQL, and RabbitMQ.

The console keeps the last 100 executions and events in memory; they disappear when it restarts. Running scenarios performs real purchases/transfers and changes local database balances. The inspector does not allow arbitrary SQL or database writes. Sample values require seeded merchant and bank accounts; edit them to match your local data.

## API documentation

After starting the relevant service, Swagger UI is available at:

- PSP: `http://localhost:8084/swagger-ui/index.html`
- Bank A: `http://localhost:8080/bank/swagger-ui/index.html`
- Bank B: `http://localhost:8081/bank/swagger-ui/index.html`

Import `postman/Payment-Network-Simulator.postman_collection.json` into Postman for ready-to-use API requests.

## Run locally

Requirements: JDK 17+, Maven, and Docker Compose.

1. Start infrastructure with `docker compose up -d`.
2. Start all four backend services and the console from the repository root:

   ```powershell
   .\scripts\run-all.ps1
   ```

   The script writes logs to `.logs`. To start services one at a time, use:

   ```bash
   mvn -pl BankA spring-boot:run
   mvn -pl BankB spring-boot:run
   mvn -pl Shaparak spring-boot:run
   mvn -pl PSP spring-boot:run
   mvn -pl Console spring-boot:run
   ```

3. Add a merchant to the `psp.merchant` MongoDB collection. The document id is sent as `merchantNumber`; `accountNumber` is its destination bank account:

   ```javascript
   db.merchant.insertOne({
     _id: "merchant-1001",
     merchantId: "merchant-1001",
     accountNumber: "333333333333"
   })
   ```

4. Add source and destination accounts. For the console's Bank A default scenario, run this SQL in your local MySQL instance (only if these sample rows do not already exist):

   ```sql
   USE bank_a;
   INSERT INTO account_a (card_number, account_number, balance) VALUES
     ('1111111234567890', '111111111111', 1000000.00),
     ('1111119999999999', '333333333333', 0.00);
   ```

   For Bank B use the sample rows in [Bank B setup](BankB/README.md). The merchant destination account must exist in the selected bank. These are real balance-changing test records; use a disposable local database.
5. Submit a purchase:

   ```bash
   curl -X POST http://localhost:8084/payment/purchase \
     -H "Content-Type: application/json" \
     -d '{"sourceCardNumber":"1111111234567890","merchantNumber":"merchant-1001","amount":25000}'
   ```

RabbitMQ management is available at `http://localhost:15672` with local credentials `guest` / `guest`.

The service ports are Bank A `8080`, Bank B `8081`, Shaparak `8082`, PSP `8084`, and Console `8090`. Docker publishes MySQL `3306`, MongoDB `27017`, RabbitMQ `5672`, and RabbitMQ management `15672`. The console's host, port, service URLs, and database connections can be overridden with the `CONSOLE_*` variables in [Console application properties](Console/src/main/resources/application.properties).

## Configuration

Connection settings can be overridden with environment variables. See each service's `application.properties`. Defaults target the services in `compose.yaml` on localhost.

## Current scope

This is an educational simulator, not a production payment system. Authentication and authorization, idempotency, distributed tracing, resilient retries, and complete automated integration tests remain future work.
