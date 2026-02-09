package com.quind.banking.domain.error;

import com.quind.banking.domain.model.vo.AccountNumber;
import com.quind.banking.domain.model.vo.Money;

/**
 * Sealed interface representing all possible transfer errors
 * Using sealed types ensures all error cases are handled at compile time
 */
public sealed interface TransferError {

    record InsufficientFunds(
        Money availableBalance,
        Money requestedAmount
    ) implements TransferError {}

    record AccountNotFound(
        AccountNumber accountNumber
    ) implements TransferError {}

    record SourceAccountNotFound(
        AccountNumber accountNumber
    ) implements TransferError {}

    record TargetAccountNotFound(
        AccountNumber accountNumber
    ) implements TransferError {}

    record SelfTransferNotAllowed(
        AccountNumber accountNumber
    ) implements TransferError {}

    record InvalidAmount(
        String reason
    ) implements TransferError {}
}
