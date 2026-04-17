package com.example.kidsfashion.service;

import com.example.kidsfashion.entity.User;
import com.example.kidsfashion.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AuthService xử lý logic đăng ký tài khoản user mới kèm xác thực email OTP.
 * Admin không đi qua flow này.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;

    /**
     * Đăng ký tài khoản user mới:
     * 1. Validate email chưa tồn tại
     * 2. Tạo User với enabled=false, password BCrypt
     * 3. Tạo và gửi OTP qua email
     * Nếu gửi email thất bại → @Transactional rollback (user không được tạo)
     *
     * @param email           email người dùng (dùng làm username login)
     * @param password        mật khẩu plaintext (sẽ được mã hóa BCrypt)
     * @throws IllegalArgumentException nếu email đã tồn tại
     */
    @Transactional(rollbackFor = Exception.class)
    public User register(String email, String password) {
        log.info("[AuthService] Register attempt for email: {}", email);

        // 1. Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmail(email)) {
            log.warn("[AuthService] Email already exists: {}", email);
            throw new IllegalArgumentException("Email này đã được đăng ký. Vui lòng dùng email khác hoặc đăng nhập.");
        }

        // 2. Tạo user mới – enabled=false chờ xác thực OTP
        User user = new User();
        user.setUsername(email);          // Dùng email làm username để đơn giản hóa
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password)); // BCrypt
        user.setRole("ROLE_CUSTOMER");
        user.setEnabled(false);           // Chưa kích hoạt

        userRepository.save(user);
        log.info("[AuthService] User created (pending OTP): {}", email);

        // 3. Tạo và gửi OTP – nếu thất bại sẽ rollback cả bước save user ở trên
        otpService.generateAndSendOtp(user);

        return user;
    }
}
