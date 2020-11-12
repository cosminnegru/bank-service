package com.spring.bank.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
@EntityListeners(AuditingEntityListener.class)
public class Transaction {

    @Id
    @GeneratedValue
    private long id;

    @Column(name = "balance", nullable = false)
    @NotNull(message = "Amount is mandatory")
    private BigDecimal amount;

    @Column(name = "transaction_type", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Transaction type is mandatory")
    private TransactionType transactionType;

    @Column(name = "counterparty", nullable = false)
    @NotBlank(message = "Counterparty is mandatory")
    private String counterparty;

    @Column(name = "description", nullable = true)
    private String description;

    @Column(name = "created_date_time")
    @CreatedDate
    private LocalDateTime createdDateTime;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id")
    private Account account;

    public Transaction() {

    }

    public Transaction(@NotNull(message = "Amount is mandatory") BigDecimal amount, @NotBlank(message = "Transaction type is mandatory") TransactionType transactionType, @NotBlank(message = "Counterparty is mandatory") String counterparty, String description, Account account) {
        this.amount = amount;
        this.transactionType = transactionType;
        this.counterparty = counterparty;
        this.description = description;
        this.account = account;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public String getCounterparty() {
        return counterparty;
    }

    public void setCounterparty(String counterparty) {
        this.counterparty = counterparty;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(LocalDateTime createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
