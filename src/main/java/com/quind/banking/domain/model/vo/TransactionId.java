package com.quind.banking.domain.model.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object representing the unique identifier of a transaction
 */
public record TransactionId(UUID value) {

    public TransactionId {
        Objects.requireNonNull(value, "Transaction ID cannot be null");
    }

    public static TransactionId generate() {
        return new TransactionId(UUID.randomUUID());
    }

    public static TransactionId of(String value) {
        return new TransactionId(UUID.fromString(value));
    }

    public static TransactionId of(UUID value) {
        return new TransactionId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
