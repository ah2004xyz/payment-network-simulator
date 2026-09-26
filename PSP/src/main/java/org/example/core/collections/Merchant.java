package org.example.core.collections;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.UUID;

@Document(collection = "merchant")
@Data
@Accessors(chain = true)
public class Merchant {
    @Id
    private String id;
    @Field(value = "merchantId")
    private String merchantNumber;
    @Field(value = "accountNumber")

    private String accountNumber;
}
