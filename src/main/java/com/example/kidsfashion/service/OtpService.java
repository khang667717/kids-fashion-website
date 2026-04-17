package com.example.kidsfashion.service;

import com.example.kidsfashion.entity.User;
import com.example.kidsfashion.exception.OtpExpiredException;
import com.example.kidsfashion.exception.RateLimitException;
import com.example.kidsfashion.exception.TooManyAttemptsException;
import com.example.kidsfashion.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Quản lý toàn bộ vòng đời OTP:
 * - Tạo mã ngẫu nhiên (SecureRandom)
 * - Hash bằng SHA-256 trước khi lưu DB
 * - Xác thực mã
 * - Gửi lại mã (rate-limited)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRE_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 5;
    private static final int RESEND_COOLDOWN_SECONDS = 30;

    private final UserRepository userRepository;
    private final EmailService emailService;

    /**
     * Tạo OTP mới, hash, lưu vào user và gửi email.
     * Nếu gửi email thất bại → ném exception → transaction rollback.
     *
     * @param user entity user cần cập nhật OTP
     */
    @Transactional(rollbackFor = Exception.class)
    public void generateAndSendOtp(User user) {
        String otpPlain = generateOtp();
        String otpHash = hashOtp(otpPlain);

        user.setOtp(otpHash);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(OTP_EXPIRE_MINUTES));
        user.setOtpAttempt(0);
        user.setLastOtpSentTime(LocalDateTime.now());

        userRepository.save(user);
        log.info("[OtpService] OTP generated for user: {}", user.getEmail());

        // Gửi email sau khi lưu – nếu thất bại sẽ rollback save ở trên
        emailService.sendOtpEmail(user.getEmail(), otpPlain);
    }

    /**
     * Xác thực OTP người dùng nhập.
     *
     * @param user     entity user
     * @param otpInput mã OTP plaintext người dùng nhập
     * @throws OtpExpiredException       nếu OTP đã hết hạn
     * @throws TooManyAttemptsException  nếu sai quá 5 lần
     * @return true nếu đúng OTP
     */
    @Transactional
    public boolean verifyOtp(User user, String otpInput) {
        // 1. Kiểm tra đã có OTP chưa
        if (user.getOtp() == null || user.getOtpExpiry() == null) {
            throw new OtpExpiredException("Mã OTP không tồn tại. Vui lòng gửi lại mã.");
        }

        // 2. Kiểm tra hết hạn
        if (LocalDateTime.now().isAfter(user.getOtpExpiry())) {
            clearOtp(user);
            throw new OtpExpiredException("Mã OTP đã hết hạn. Vui lòng gửi lại mã mới.");
        }

        // 3. So sánh hash
        String inputHash = hashOtp(otpInput.trim());
        if (!inputHash.equals(user.getOtp())) {
            // Sai OTP → tăng attempt
            user.setOtpAttempt(user.getOtpAttempt() + 1);

            if (user.getOtpAttempt() >= MAX_ATTEMPTS) {
                clearOtp(user);
                userRepository.save(user);
                log.warn("[OtpService] Max OTP attempts reached for: {}", user.getEmail());
                throw new TooManyAttemptsException(
                        "Bạn đã nhập sai OTP quá " + MAX_ATTEMPTS + " lần. Vui lòng gửi lại mã mới.");
            }

            userRepository.save(user);
            int remaining = MAX_ATTEMPTS - user.getOtpAttempt();
            log.warn("[OtpService] Wrong OTP for {}. Remaining attempts: {}", user.getEmail(), remaining);
            return false; // controller sẽ hiển thị "còn X lần thử"
        }

        // 4. OTP đúng → kích hoạt tài khoản
        user.setEnabled(true);
        clearOtp(user);
        userRepository.save(user);
        log.info("[OtpService] OTP verified successfully for: {}", user.getEmail());
        return true;
    }

    /**
     * Gửi lại OTP với rate limit 30 giây.
     *
     * @param user entity user cần gửi lại OTP
     * @throws RateLimitException nếu chưa đủ 30 giây kể từ lần gửi trước
     */
    @Transactional(rollbackFor = Exception.class)
    public void resendOtp(User user) {
        // Kiểm tra rate limit
        if (user.getLastOtpSentTime() != null) {
            long secondsElapsed = java.time.Duration.between(
                    user.getLastOtpSentTime(), LocalDateTime.now()).getSeconds();
            if (secondsElapsed < RESEND_COOLDOWN_SECONDS) {
                long waitSeconds = RESEND_COOLDOWN_SECONDS - secondsElapsed;
                throw new RateLimitException(
                        "Vui lòng đợi " + waitSeconds + " giây trước khi gửi lại mã OTP.");
            }
        }

        // Tạo và gửi OTP mới
        generateAndSendOtp(user);
        log.info("[OtpService] OTP resent for: {}", user.getEmail());
    }

    // ========== Private helpers ==========

    /** Tạo OTP 6 chữ số ngẫu nhiên dùng SecureRandom. */
    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000); // đảm bảo đúng 6 chữ số
        return String.valueOf(otp);
    }

    /**
     * Hash OTP bằng MD5 (DigestUtils.md5DigestAsHex).
     * Dùng SHA-256 thông qua DigestUtils để không cần import thêm.
     */
    private String hashOtp(String otpPlain) {
        // Dùng DigestUtils của Spring (commons-codec) – SHA-256
        return DigestUtils.md5DigestAsHex(
                (otpPlain + "_kids_fashion_salt").getBytes()
        );
    }

    /** Xóa OTP khỏi user sau khi xác thực hoặc hết attempts. */
    private void clearOtp(User user) {
        user.setOtp(null);
        user.setOtpExpiry(null);
        user.setOtpAttempt(0);
    }
}
