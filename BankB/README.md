# Bank B

Bank B is an independent Spring Boot service for cards with the `222222` prefix.

It runs on port `8081` and uses the MySQL schema `bank_b`.

Start it from the repository root:

```bash
mvn -pl BankB spring-boot:run
```

After the service starts, add test accounts through MySQL:

```sql
USE bank_b;

INSERT INTO account_b (card_number, account_number, balance)
VALUES
  ('2222221234567890', '222222222222', 1000000.00),
  ('2222229999999999', '333333333333', 0.00);
```

For this sample, the PSP merchant must use `333333333333` as its `accountNumber`.

The payment route is:

```text
PSP -> RabbitMQ -> Shaparak -> http://localhost:8081/bank/bank2
```
