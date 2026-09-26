package org.example.usecase;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class PaymentUseCaseResponse {

    private String status;
    private String transactionDate;
    private BigDecimal amount;

}
