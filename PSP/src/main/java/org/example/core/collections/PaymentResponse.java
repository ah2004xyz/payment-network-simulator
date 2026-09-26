package org.example.core.collections;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "payment_response")
@Data
@Accessors(chain = true)
public class PaymentResponse {

    private String traceId;

    private String status;
    private String transactionDate;
    private BigDecimal amount;

}
