package com.example.kidsfashion.exception;

import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailSendException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Xử lý global exception cho toàn bộ controller.
 * Các exception OTP được redirect về trang verify-otp với thông báo thân thiện.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /** OTP đã hết hạn → redirect về trang verify-otp với lỗi. */
    @ExceptionHandler(OtpExpiredException.class)
    public String handleOtpExpired(OtpExpiredException e, RedirectAttributes redirectAttributes) {
        log.warn("OTP expired: {}", e.getMessage());
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return "redirect:/verify-otp";
    }

    /** Sai OTP quá 5 lần → redirect về verify-otp. */
    @ExceptionHandler(TooManyAttemptsException.class)
    public String handleTooManyAttempts(TooManyAttemptsException e, RedirectAttributes redirectAttributes) {
        log.warn("Too many OTP attempts: {}", e.getMessage());
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return "redirect:/verify-otp";
    }

    /** Resend OTP quá nhanh → redirect lại verify-otp với cảnh báo. */
    @ExceptionHandler(RateLimitException.class)
    public String handleRateLimit(RateLimitException e, RedirectAttributes redirectAttributes) {
        log.warn("OTP resend rate limited: {}", e.getMessage());
        redirectAttributes.addFlashAttribute("warning", e.getMessage());
        return "redirect:/verify-otp";
    }

    /** Gửi email thất bại (SMTP error) → redirect đăng ký với lỗi. */
    @ExceptionHandler({MailSendException.class, MessagingException.class})
    public String handleMailError(Exception e, RedirectAttributes redirectAttributes) {
        log.error("Mail send error: {}", e.getMessage());
        redirectAttributes.addFlashAttribute("error",
                "Không thể gửi email xác thực. Vui lòng kiểm tra địa chỉ email và thử lại.");
        return "redirect:/register";
    }

    /** Fallback exception handler. */
    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, RedirectAttributes redirectAttributes) {
        log.error("Unhandled exception: {}", e.getMessage(), e);
        redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
        return "redirect:/";
    }
}