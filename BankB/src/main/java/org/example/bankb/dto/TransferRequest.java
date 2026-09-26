package org.example.bankb.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {

    private String traceId;

    @Size(min = 16, max = 16, message = "شماره کارت باید دقیقاً 16 رقم باشد")
    @Pattern(regexp = "\\d{16}", message = "شماره کارت فقط باید شامل اعداد باشد")
    private String sourceCardNumber;

    @Size(min = 12, max = 13, message = "شماره حساب باید بین 12 تا 13 رقم باشد")
    @Pattern(regexp = "\\d{12,13}", message = "شماره حساب فقط باید شامل اعداد باشد")
    private String targetAccountNumber;

    @NotNull(message = "مبلغ نمی‌تواند خالی باشد")
    @Positive(message = "مبلغ باید مثبت باشد")
    private BigDecimal amount;
}
