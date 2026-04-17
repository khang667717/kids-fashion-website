package com.example.kidsfashion.exception;

/**
 * Ném ra khi OTP đã hết hạn (quá 5 phút).
 */
public class OtpExpiredException extends RuntimeException {
    public OtpExpiredException(String message) {
        super(message);
    }
}
