package com.quind.banking.domain.model.entity;

import com.quind.banking.domain.model.enums.TransactionType;
import com.quind.banking.domain.model.vo.AccountNumber;
import com.quind.banking.domain.model.vo.Money;
import com.quind.banking.domain.model.vo.TransactionId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing a financial transaction between accounts
 */
public class Transaction {

    private final TransactionId id;
    private final AccountNumber sourceAccountNumber;
    private final AccountNumber targetAccountNumber;
    private final Money amount;
    private final TransactionType type;
    private final String description;
    private final LocalDateTime createdAt;

    private Transaction(TransactionId id, AccountNumber sourceAccountNumber,
                        AccountNumber targetAccountNumber, Money amount,
                        TransactionType type, String description, LocalDateTime createdAt) {
        this.id = Objects.requireNonNull(id, "Transaction ID cannot be null");
        this.sourceAccountNumber = Objects.requireNonNull(sourceAccountNumber, "Source account cannot be null");
        this.targetAccountNumber = Objects.requireNonNull(targetAccountNumber, "Target account cannot be null");
        this.amount = Objects.requireNonNull(amount, "Amount cannot be null");
        this.type = Objects.requireNonNull(type, "Transaction type cannot be null");
        this.description = description;
        this.createdAt = Objects.requireNonNull(createdAt, "Created date cannot be null");
    }

    // Factory method for creating a new transfer transaction
    public static Transaction createTransfer(AccountNumber sourceAccount, AccountNumber targetAccount,
                                             Money amount, String description) {
        return new Transaction(
            TransactionId.generate(),
            sourceAccount,
            targetAccount,
            amount,
            TransactionType.DEBIT,
            description,
            LocalDateTime.now()
        );
    }

    // Factory method for reconstituting from persistence
    public static Transaction reconstitute(TransactionId id, AccountNumber sourceAccount,
                                           AccountNumber targetAccount, Money amount,
                                           TransactionType type, String description,
                                           LocalDateTime createdAt) {
        return new Transaction(id, sourceAccount, targetAccount, amount, type, description, createdAt);
    }

    // Getters
    public TransactionId getId() {
        return id;
    }

    public AccountNumber getSourceAccountNumber() {
        return sourceAccountNumber;
    }

    public AccountNumber getTargetAccountNumber() {
        return targetAccountNumber;
    }

    public Money getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
