package com.quind.banking.domain.event;

import com.quind.banking.domain.model.vo.AccountId;
import com.quind.banking.domain.model.vo.AccountNumber;
import com.quind.banking.domain.model.vo.Money;

import java.time.Instant;

/**
 * Event raised when a new account is created
 */
public record AccountCreatedEvent(
    AccountId accountId,
    AccountNumber accountNumber,
    String ownerName,
    Money initialBalance,
    Instant occurredOn
) implements DomainEvent {

    public AccountCreatedEvent(AccountId accountId, AccountNumber accountNumber,
                               String ownerName, Money initialBalance) {
        this(accountId, accountNumber, ownerName, initialBalance, Instant.now());
    }

    @Override
    public String getAggregateId() {
        return accountId.toString();
    }
}
