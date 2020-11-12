package com.spring.bank.controller;

import com.spring.bank.exception.ResourceNotFoundException;
import com.spring.bank.model.*;
import com.spring.bank.service.AccountService;
import com.spring.bank.service.TransactionService;
import com.spring.bank.service.impl.AccountServiceImpl;
import net.bytebuddy.utility.RandomString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1")
public class AccountController {

    private static Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);

    private AccountService accountService;
    private TransactionService transactionService;

    public AccountController(AccountService accountService, TransactionService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    @PostMapping("/accounts")
    public Account createAccount(@Valid @RequestBody Account account) {
        logger.info("Request to create an account : {} ", account);
        addTransactions(account);
        account.setIban(generateIBAN());
        account.setBalance(account.getTransactions().stream()
                .map(AccountController::getAmountBasedOnTransactionType)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        return accountService.save(account);
    }

    @PutMapping("/accounts/{id}")
    public Account cancelAccount(@PathVariable Long id)  {
        logger.info("Request to close the account with id : {} ", id);
        Account account = accountService.findById(id).orElseThrow(() -> new ResourceNotFoundException("Account with id " + id + " not found"));
        account.setAccountStatus(AccountStatus.CLOSED);
        return accountService.save(account);
    }

    @GetMapping("/accounts/{id}/transactions")
    public List<Transaction> getTransactions(@PathVariable Long id, @RequestParam(name = "timeFrame") TimeFrame timeFrame,
                                             @RequestParam(name = "interval") int interval) {
        logger.info("Request to retrieve the list of transaction for the following account id : {} ", id);
        return transactionService.getTransactionsByCriteria(id, timeFrame.apply(interval));
    }

    private void addTransactions(Account account) {
        account.getTransactions().add(new Transaction(BigDecimal.TEN, TransactionType.CREDIT, "Ionescu Marius", "bank transfer", account));
        account.getTransactions().add(new Transaction(BigDecimal.ONE, TransactionType.DEBIT, "Ionescu Marius", "bank transfer", account));
    }

    private String generateIBAN() {
        return RandomString.make();
    }

    private static BigDecimal getAmountBasedOnTransactionType(Transaction transaction) {
        if (TransactionType.DEBIT == transaction.getTransactionType()) {
            return transaction.getAmount().negate();
        }
        return transaction.getAmount();
    }
}
