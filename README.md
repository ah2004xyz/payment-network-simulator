# Payment Network Simulator

A Spring Boot system that simulates a card purchase moving through a PSP, the Shaparak payment network, and an issuing bank.

## Architecture

```text
Merchant
   |
   | POST /payment/purchase
   v
PSP :8084  -- RPC over RabbitMQ -->  Shaparak :8082  -- REST -->  Bank :8080
   |                                      |                         |
MongoDB                                MongoDB                     MySQL
```

The PSP validates the purchase and merchant, stores the request, and sends an RPC message. Shaparak selects the bank from the card's first six digits, stores its request and response, and calls the bank. The bank locks the source account, checks the balance, transfers the amount, and records the transaction.

| Card prefix | Route |
| --- | --- |
| `111111` | Bank 1 |
| `222222` | Bank 2 |

## Tech stack

- Java 17 and Spring Boot 3.4
- Spring Web and Bean Validation
- RabbitMQ RPC messaging
- MongoDB for PSP and Shaparak request/response records
- MySQL and Spring Data JPA for bank accounts and transactions
- Maven multi-module build

## Run locally

Requirements: JDK 17+, Maven, and Docker Compose.

1. Start infrastructure with `docker compose up -d`.
2. Start each service in a separate terminal from the repository root:

   ```bash
   mvn -pl Bank spring-boot:run
   mvn -pl Shaparak spring-boot:run
   mvn -pl PSP spring-boot:run
   ```

3. Add a merchant to the `psp.merchant` MongoDB collection. The document id is sent as `merchantNumber`; `accountNumber` is its destination bank account:

   ```javascript
   db.merchant.insertOne({
     _id: "merchant-1001",
     merchantId: "merchant-1001",
     accountNumber: "222222222222"
   })
   ```

4. Add source and destination accounts to MySQL table `account_a` after the Bank service creates the schema.
5. Submit a purchase:

   ```bash
   curl -X POST http://localhost:8084/payment/purchase \
     -H "Content-Type: application/json" \
     -d '{"sourceCardNumber":"1111111234567890","merchantNumber":"merchant-1001","amount":25000}'
   ```

RabbitMQ management is available at `http://localhost:15672` with local credentials `guest` / `guest`.

## Configuration

Connection settings can be overridden with environment variables. See each service's `application.properties`. Defaults target the services in `compose.yaml` on localhost.

## Current scope

This is an educational simulator, not a production payment system. Authentication and authorization, idempotency, distributed tracing, resilient retries, and complete automated integration tests remain future work.

