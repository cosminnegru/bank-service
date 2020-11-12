package com.spring.bank.service;

import com.spring.bank.model.Account;

import java.util.Optional;

public interface AccountService {

    Account save(Account account);

    Optional<Account> findById(Long id);

}
