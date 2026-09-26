package org.example.bankA.model.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.example.bankA.model.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_a")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class TransactionAEntity {

    @Column(name = "trace_id", length = 64)
    private String traceId;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Transaction_id_A")
    private Long id;
    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;
    @ManyToOne
    @JoinColumn(name = "source_card_number", referencedColumnName = "card_number")
    private AccountAEntity sourceAccount;
    @ManyToOne
    @JoinColumn(name = "target_card_number", referencedColumnName = "card_number")
    private AccountAEntity targetAccount;
    @Column(name = "amount")
    private BigDecimal amount;
    @Column(name = "status")
    private TransactionStatus status;

}
