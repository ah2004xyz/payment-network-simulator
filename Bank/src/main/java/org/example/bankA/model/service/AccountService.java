package org.example.bankA.model.service;

import lombok.AllArgsConstructor;
import org.example.bankA.Constants.ErrorConstants;
import org.example.bankA.Constants.WordConstants;
import org.example.bankA.dtos.TransferRequestDTO;
import org.example.bankA.dtos.TransferResponseDTO;
import org.example.bankA.exception.NotEnoughBalance;
import org.example.bankA.exception.NotFoundException;
import org.example.bankA.model.Entity.AccountAEntity;
import org.example.bankA.model.Entity.TransactionAEntity;
import org.example.bankA.model.enums.TransactionStatus;
import org.example.bankA.model.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class AccountService {

    private final AccountRepository repository;
    private final TransactionService transactionService;
    //create account
    //do transaction

    @Transactional
    public TransferResponseDTO transfer(TransferRequestDTO request) {
        //check if the source has enough money
        //any checker like if accounts are available and status is active
        //update source balance to current balance - amount
        //update target balance to current balance + amount
        System.out.println("first service");
        AccountAEntity sourceAccount = repository.findByCardNumber(request.getSourceCardNumber())
                .orElseThrow(() -> new NotFoundException(ErrorConstants.SOURCE_ACCOUNT_NOT_FOUND));

        AccountAEntity targetAccount = repository.findByAccountNumber(request.getTargetAccountNumber())
                .orElseThrow(() -> new NotFoundException(ErrorConstants.TARGET_ACCOUNT_NOT_FOUND));

        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new NotEnoughBalance(WordConstants.SOURCE);
        }
        TransactionAEntity transaction = new TransactionAEntity()
                .setTransactionDate(LocalDateTime.now())
                .setStatus(TransactionStatus.FAILED)
                .setSourceAccount(sourceAccount)
                .setTargetAccount(targetAccount)
                .setAmount(request.getAmount());

        transaction = transactionService.doTransaction(transaction);

        try {
            sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.getAmount()));
            targetAccount.setBalance(targetAccount.getBalance().add(request.getAmount()));
            repository.saveAll(List.of(sourceAccount, targetAccount));

            transaction.setStatus(TransactionStatus.SUCCESS);
            transaction = transactionService.doTransaction(transaction);
            System.out.println("end service");

            return new TransferResponseDTO()
                    .setStatus(transaction.getStatus())
                    .setTransactionDate(transaction.getTransactionDate())
                    .setAmount(transaction.getAmount());
        } catch (Exception e) {
            System.out.println("end service");
            return new TransferResponseDTO()
                    .setStatus(transaction.getStatus())
                    .setTransactionDate(transaction.getTransactionDate())
                    .setAmount(transaction.getAmount());
        }
    }

}
