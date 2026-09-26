package org.example.bankb.repository;

import jakarta.persistence.LockModeType;
import org.example.bankb.model.BankBAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface BankBAccountRepository extends JpaRepository<BankBAccount, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<BankBAccount> findByCardNumber(String cardNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<BankBAccount> findByAccountNumber(String accountNumber);
}
