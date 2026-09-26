package org.example.bankA.model.service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.bankA.model.Entity.TransactionAEntity;
import org.example.bankA.model.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class TransactionService {

    private final TransactionRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TransactionAEntity doTransaction(TransactionAEntity transaction){
        return repository.save(transaction);
    }

}
