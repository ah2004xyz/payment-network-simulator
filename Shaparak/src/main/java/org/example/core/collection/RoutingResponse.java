package org.example.core.collection;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "routing_response")
@Data
@Accessors(chain = true)
public class RoutingResponse {
    private String status;
    private String transactionDate;
    private BigDecimal amount;

}
