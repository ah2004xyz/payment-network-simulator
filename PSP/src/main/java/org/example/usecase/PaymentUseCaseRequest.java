package org.example.usecase;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Accessors(chain = true)

public class PaymentUseCaseRequest {


    @Pattern(regexp = "\\d{16}", message = "شماره کارت باید ۱۶ رقم عددی باشد")
    private String sourceCardNumber;

    @NotNull(message = "شناسه حساب پذیرنده نمی‌تواند خالی باشد")
    private String merchantNumber;

    @Min(value = 1000, message = "مبلغ باید حداقل ۱۰۰۰ باشد")
    @Digits(integer = 10, fraction = 2, message = "مقدار باید حداکثر ۱۰ رقم صحیح و ۲ رقم اعشار داشته باشد")
    @Positive(message = "مقدار باید مثبت باشد")
    private BigDecimal amount;

    private String traceId;


}
