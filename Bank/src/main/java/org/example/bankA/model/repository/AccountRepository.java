package org.example.bankA.model.repository;

import org.example.bankA.model.Entity.AccountAEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;

@Repository
public interface AccountRepository extends JpaRepository<AccountAEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<AccountAEntity> findByCardNumber(String cardNumber);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<AccountAEntity> findByAccountNumber(String accountNumber);

}
