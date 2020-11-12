package com.spring.bank.service.impl;

import com.spring.bank.model.Transaction;
import com.spring.bank.repository.TransactionRepository;
import com.spring.bank.service.TransactionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private TransactionRepository transactionRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByCriteria(Long accountId, LocalDateTime startDate) {
        return transactionRepository.findAllByAccountIdAndCreatedDateTimeGreaterThanEqual(accountId, startDate);
    }

}
