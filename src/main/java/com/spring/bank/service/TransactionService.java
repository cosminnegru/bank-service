package com.spring.bank.service;

import com.spring.bank.model.Transaction;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionService {

    List<Transaction> getTransactionsByCriteria(Long accountId, LocalDateTime startDate);

}
