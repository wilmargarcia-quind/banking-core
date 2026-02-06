package com.quind.banking.domain.model.vo;

import java.util.Objects;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * Value Object representing a bank account number
 * Must be between 10 and 20 digits
 */
public record AccountNumber(String value) {

    private static final Pattern VALID_PATTERN = Pattern.compile("^[0-9]{10,20}$");
    private static final Random RANDOM = new Random();

    public AccountNumber {
        Objects.requireNonNull(value, "Account number cannot be null");
        value = value.trim();

        if (!VALID_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                "Invalid account number format. Must be 10-20 digits: " + value
            );
        }
    }

    public static AccountNumber of(String value) {
        return new AccountNumber(value);
    }

    public static AccountNumber generate() {
        // Generate 16 digit account number
        long number = Math.abs(RANDOM.nextLong() % 10_000_000_000_000_000L);
        return new AccountNumber(String.format("%016d", number));
    }

    @Override
    public String toString() {
        return value;
    }
}
