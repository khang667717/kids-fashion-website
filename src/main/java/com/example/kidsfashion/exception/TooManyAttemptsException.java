package com.example.kidsfashion.exception;

/**
 * Ném ra khi người dùng nhập sai OTP quá 5 lần.
 */
public class TooManyAttemptsException extends RuntimeException {
    public TooManyAttemptsException(String message) {
        super(message);
    }
}
