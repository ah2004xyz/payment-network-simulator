package org.example.bankb.repository;

import org.example.bankb.model.BankBTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankBTransactionRepository extends JpaRepository<BankBTransaction, Long> {
}
