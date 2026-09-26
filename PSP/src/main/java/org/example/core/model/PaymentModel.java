package org.example.core.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class PaymentModel {

    private String traceId;

    private String sourceCardNumber;
    private String targetAccountNumber;
    private BigDecimal amount;

}
