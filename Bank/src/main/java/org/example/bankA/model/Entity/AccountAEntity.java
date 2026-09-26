package org.example.bankA.model.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "account_a")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class AccountAEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private long AccountId;
    @Column(name = "card_number", unique = true, nullable = false)
    private String cardNumber;
    @Column(name = "account_number", unique = true, nullable = false)
    private String accountNumber;
    @Column(name = "balance")
    private BigDecimal balance;
    @OneToMany(mappedBy = "sourceAccount")
    private List<TransactionAEntity> sourceTransactions;
    @OneToMany(mappedBy = "targetAccount")
    private List<TransactionAEntity> targetTransactions;

}

