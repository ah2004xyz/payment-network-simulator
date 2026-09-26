package org.example.bankA.model.repository;

import org.example.bankA.model.Entity.TransactionAEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionAEntity, Long> {
}
