package com.quind.banking.shared;

import java.util.function.Function;

/**
 * Resul pattern for functional error handling
 *
 * @param <S> Success value type
 * @param <F> Failure error type
 */
public sealed interface Result<S, F> {

    record Success<S, F>(S value) implements Result<S, F> {}

    record Failure<S, F>(F error) implements Result<S, F> {}

    static <S, F> Result<S, F> success(S value) {
        return new Success<>(value);
    }

    static <S, F> Result<S, F> failure(F error) {
        return new Failure<>(error);
    }

    default boolean isSuccess() {
        return this instanceof Success;
    }

    default boolean isFailure() {
        return this instanceof Failure;
    }

    default S getValue() {
        if (this instanceof Success<S, F> success) {
            return success.value();
        }
        throw new IllegalStateException("Cannot get value from Failure");
    }

    default F getError() {
        if (this instanceof Failure<S, F> failure) {
            return failure.error();
        }
        throw new IllegalStateException("Cannot get error from Success");
    }

    default <T> T fold(Function<S, T> onSuccess, Function<F, T> onFailure) {
        return switch (this) {
            case Success<S, F> s -> onSuccess.apply(s.value());
            case Failure<S, F> f -> onFailure.apply(f.error());
        };
    }

    default <T> Result<T, F> map(Function<S, T> mapper) {
        return switch (this) {
            case Success<S, F> s -> Result.success(mapper.apply(s.value()));
            case Failure<S, F> f -> Result.failure(f.error());
        };
    }

    default <T> Result<T, F> flatMap(Function<S, Result<T, F>> mapper) {
        return switch (this) {
            case Success<S, F> s -> mapper.apply(s.value());
            case Failure<S, F> f -> Result.failure(f.error());
        };
    }
}
