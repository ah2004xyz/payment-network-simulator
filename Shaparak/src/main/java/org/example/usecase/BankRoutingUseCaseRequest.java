package org.example.usecase;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class BankRoutingUseCaseRequest {

    @Pattern(regexp = "\\d{16}", message = "شماره کارت باید ۱۶ رقم عددی باشد")

    private String sourceCardNumber;

    @Size(min = 12, max = 13, message = "شماره حساب باید بین 12 تا 13 رقم باشد")
    @Pattern(regexp = "\\d{12,13}", message = "شماره حساب فقط باید شامل اعداد باشد")
    private String targetAccountNumber;

    @Min(value = 1000, message = "مبلغ باید حداقل ۱۰۰۰ باشد")
    @Digits(integer = 10, fraction = 2, message = "مقدار باید حداکثر ۱۰ رقم صحیح و ۲ رقم اعشار داشته باشد")
    @Positive(message = "مقدار باید مثبت باشد")
    private BigDecimal amount;
}
