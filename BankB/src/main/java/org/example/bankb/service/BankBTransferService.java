package org.example.bankb.service;

import lombok.RequiredArgsConstructor;
import org.example.bankb.dto.TransferRequest;
import org.example.bankb.dto.TransferResponse;
import org.example.bankb.exception.AccountNotFoundException;
import org.example.bankb.exception.InsufficientBalanceException;
import org.example.bankb.model.BankBAccount;
import org.example.bankb.model.BankBTransaction;
import org.example.bankb.model.TransactionStatus;
import org.example.bankb.repository.BankBAccountRepository;
import org.example.bankb.repository.BankBTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BankBTransferService {

    private final BankBAccountRepository accountRepository;
    private final BankBTransactionRepository transactionRepository;

    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        BankBAccount sourceAccount = accountRepository.findByCardNumber(request.getSourceCardNumber())
                .orElseThrow(() -> new AccountNotFoundException("حساب مبدأ پیدا نشد"));

        BankBAccount targetAccount = accountRepository.findByAccountNumber(request.getTargetAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException("حساب مقصد پیدا نشد"));

        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException();
        }

        sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.getAmount()));
        targetAccount.setBalance(targetAccount.getBalance().add(request.getAmount()));

        BankBTransaction transaction = new BankBTransaction();
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setSourceAccount(sourceAccount);
        transaction.setTargetAccount(targetAccount);
        transaction.setAmount(request.getAmount());
        transaction.setStatus(TransactionStatus.SUCCESS);
        transactionRepository.save(transaction);

        return new TransferResponse(transaction.getStatus(), transaction.getTransactionDate(), transaction.getAmount());
    }
}
