package org.example.core.collection;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document(collection = "routing_request")
@Data
@Accessors(chain = true)
public class RoutingRequest {

    private String sourceCardNumber;
    private String targetAccountNumber;
    private BigDecimal amount;

}
