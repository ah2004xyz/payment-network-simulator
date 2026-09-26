package org.example.core.collections;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.UUID;

@Document(collection = "payment_request")
@Data
@Accessors(chain = true)
public class PaymentRequest {

    private String sourceCardNumber;

    private String merchantNumber;

    private BigDecimal amount;

}
