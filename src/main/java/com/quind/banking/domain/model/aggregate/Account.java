package com.quind.banking.domain.model.aggregate;

import com.quind.banking.domain.error.TransferError;
import com.quind.banking.domain.event.AccountCreatedEvent;
import com.quind.banking.domain.model.enums.AccountStatus;
import com.quind.banking.domain.model.vo.AccountId;
import com.quind.banking.domain.model.vo.AccountNumber;
import com.quind.banking.domain.model.vo.Money;
import com.quind.banking.shared.Result;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Aggregate Root representing a bank account
 * All balance modifications must go through this class methods
 */
public class Account extends AggregateRoot<AccountId> {

    private final AccountNumber accountNumber;
    private final String ownerName;
    private Money balance;
    private AccountStatus status;
    private final LocalDateTime createdAt;

    private Account(AccountId id, AccountNumber accountNumber, String ownerName,
                    Money balance, AccountStatus status, LocalDateTime createdAt) {
        super(id);
        this.accountNumber = Objects.requireNonNull(accountNumber, "Account number cannot be null");
        this.ownerName = Objects.requireNonNull(ownerName, "Owner name cannot be null");
        this.balance = Objects.requireNonNull(balance, "Balance cannot be null");
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "Created date cannot be null");
    }

    /**
     * Factory method for creating a new account
     */
    public static Account create(String ownerName, Money initialBalance) {
        Objects.requireNonNull(ownerName, "Owner name cannot be null");
        if (ownerName.isBlank()) {
            throw new IllegalArgumentException("Owner name cannot be blank");
        }

        Money balance = initialBalance != null ? initialBalance : Money.zero();

        Account account = new Account(
            AccountId.generate(),
            AccountNumber.generate(),
            ownerName.trim(),
            balance,
            AccountStatus.ACTIVE,
            LocalDateTime.now()
        );

        // Register domain event
        account.registerEvent(new AccountCreatedEvent(
            account.getId(),
            account.getAccountNumber(),
            account.getOwnerName(),
            account.getBalance()
        ));

        return account;
    }

    /**
     * Factory method for reconstituting from persistence
     */
    public static Account reconstitute(AccountId id, AccountNumber accountNumber,
                                       String ownerName, Money balance,
                                       AccountStatus status, LocalDateTime createdAt) {
        return new Account(id, accountNumber, ownerName, balance, status, createdAt);
    }

    /**
     * Debits the account with the specified amount
     * Returns Result with new balance on success or TransferError on failure
     */
    public Result<Money, TransferError> debit(Money amount) {
        if (!amount.isPositive()) {
            return Result.failure(new TransferError.InvalidAmount("Debit amount must be positive"));
        }

        if (!hasSufficientFunds(amount)) {
            return Result.failure(new TransferError.InsufficientFunds(this.balance, amount));
        }

        this.balance = this.balance.subtract(amount);
        return Result.success(this.balance);
    }

    /**
     * Credits the account with the specified amount
     */
    public void credit(Money amount) {
        if (!amount.isPositive()) {
            throw new IllegalArgumentException("Credit amount must be positive");
        }
        this.balance = this.balance.add(amount);
    }

    /**
     * Checks if account has sufficient funds for the given amount
     */
    public boolean hasSufficientFunds(Money amount) {
        return this.balance.isGreaterThanOrEqual(amount);
    }

    // Getters
    public AccountNumber getAccountNumber() {
        return accountNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public Money getBalance() {
        return balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
