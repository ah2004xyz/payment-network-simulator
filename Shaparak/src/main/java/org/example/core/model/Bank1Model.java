package org.example.core.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class Bank1Model implements BankModel{

    //validation numeric-16digit-ask ai
    private String sourceCardNumber;
    private String targetAccountNumber;
    private BigDecimal amount;

}
