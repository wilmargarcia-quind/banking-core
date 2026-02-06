package com.quind.banking.domain.model.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object representing the unique identifier of an account
 */
public record AccountId(UUID value) {

    public AccountId {
        Objects.requireNonNull(value, "Account ID cannot be null");
    }

    public static AccountId generate() {
        return new AccountId(UUID.randomUUID());
    }

    public static AccountId of(String value) {
        return new AccountId(UUID.fromString(value));
    }

    public static AccountId of(UUID value) {
        return new AccountId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
