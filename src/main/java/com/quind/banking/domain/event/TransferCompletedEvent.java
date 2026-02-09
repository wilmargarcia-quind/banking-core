package com.quind.banking.domain.event;

import com.quind.banking.domain.model.vo.AccountNumber;
import com.quind.banking.domain.model.vo.Money;
import com.quind.banking.domain.model.vo.TransactionId;

import java.time.Instant;

/**
 * Event raised when a transfer is completed sucessfully
 */
public record TransferCompletedEvent(
    TransactionId transactionId,
    AccountNumber sourceAccountNumber,
    AccountNumber targetAccountNumber,
    Money amount,
    Instant occurredOn
) implements DomainEvent {

    public TransferCompletedEvent(TransactionId transactionId, AccountNumber sourceAccountNumber,
                                  AccountNumber targetAccountNumber, Money amount) {
        this(transactionId, sourceAccountNumber, targetAccountNumber, amount, Instant.now());
    }

    @Override
    public String getAggregateId() {
        return transactionId.toString();
    }
}
