package com.example.kidsfashion.exception;

/**
 * Ném ra khi người dùng gửi lại OTP quá nhanh (chưa qua 30 giây).
 */
public class RateLimitException extends RuntimeException {
    public RateLimitException(String message) {
        super(message);
    }
}
