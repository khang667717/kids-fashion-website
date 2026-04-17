package com.example.kidsfashion.controller;

import com.example.kidsfashion.entity.User;
import com.example.kidsfashion.exception.OtpExpiredException;
import com.example.kidsfashion.exception.TooManyAttemptsException;
import com.example.kidsfashion.repository.UserRepository;
import com.example.kidsfashion.service.AuthService;
import com.example.kidsfashion.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.Optional;

/**
 * Controller xử lý đăng ký tài khoản và xác thực OTP.
 * Admin không đi qua flow này.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;
    private final UserRepository userRepository;

    // ============================================================
    // REGISTER
    // ============================================================

    /** Hiển thị trang đăng ký. */
    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    /**
     * Xử lý form đăng ký.
     * Validate password → tạo user (enabled=false) → gửi OTP → redirect verify-otp.
     */
    @PostMapping("/register")
    public String register(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes) {

        // ---------- Client-side validation ----------
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp.");
            return "redirect:/register";
        }
        if (password.length() < 8) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu phải có ít nhất 8 ký tự.");
            return "redirect:/register";
        }

        // ---------- Business logic (AuthService) ----------
        try {
            authService.register(email.trim().toLowerCase(), password);
            log.info("[AuthController] Registration successful, redirecting to OTP page: {}", email);
            redirectAttributes.addFlashAttribute("success",
                    "Đăng ký thành công! Chúng tôi đã gửi mã OTP tới " + email + ". Vui lòng kiểm tra hộp thư.");
            return "redirect:/verify-otp?email=" + java.net.URLEncoder.encode(
                    email.trim().toLowerCase(), java.nio.charset.StandardCharsets.UTF_8);

        } catch (IllegalArgumentException e) {
            // Email đã tồn tại
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        } catch (Exception e) {
            log.error("[AuthController] Register error: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error",
                    "Đăng ký thất bại: " + e.getMessage());
            return "redirect:/register";
        }
    }

    // ============================================================
    // VERIFY OTP
    // ============================================================

    /**
     * Hiển thị trang nhập OTP.
     * Nhận email qua query param để pre-fill.
     */
    @GetMapping("/verify-otp")
    public String verifyOtpForm(@RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "msg", required = false) String msg,
            Model model) {
        model.addAttribute("email", email != null ? email : "");

        // Nếu bị redirect từ login vì chưa verify
        if ("unverified".equals(msg)) {
            model.addAttribute("infoMsg",
                    "Tài khoản chưa được xác thực. Vui lòng nhập mã OTP đã gửi tới email của bạn.");
        }
        return "verify-otp";
    }

    /**
     * Xử lý POST xác thực OTP.
     * Ghép 6 ô input thành chuỗi OTP rồi verify.
     */
    @PostMapping("/verify-otp")
    public String verifyOtp(
            @RequestParam("email") String email,
            @RequestParam("otp1") String otp1,
            @RequestParam("otp2") String otp2,
            @RequestParam("otp3") String otp3,
            @RequestParam("otp4") String otp4,
            @RequestParam("otp5") String otp5,
            @RequestParam("otp6") String otp6,
            RedirectAttributes redirectAttributes) {

        String otpInput = otp1 + otp2 + otp3 + otp4 + otp5 + otp6;
        String emailTrimmed = email.trim().toLowerCase();

        log.info("[AuthController] OTP verification attempt for: {}", emailTrimmed);

        // Tìm user theo email
        Optional<User> userOpt = userRepository.findByEmail(emailTrimmed);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error",
                    "Không tìm thấy tài khoản với email: " + emailTrimmed);
            return "redirect:/verify-otp?email=" + encode(emailTrimmed);
        }

        User user = userOpt.get();

        // Nếu đã kích hoạt rồi
        if (user.isEnabled()) {
            redirectAttributes.addFlashAttribute("success",
                    "Tài khoản đã được xác thực. Vui lòng đăng nhập.");
            return "redirect:/";
        }

        try {
            boolean ok = otpService.verifyOtp(user, otpInput);
            if (ok) {
                redirectAttributes.addFlashAttribute("success",
                        "Xác thực email thành công! Vui lòng đăng nhập.");
                return "redirect:/";
            } else {
                // Sai OTP nhưng chưa đạt max attempts
                int remaining = 5 - user.getOtpAttempt();
                redirectAttributes.addFlashAttribute("error",
                        "Mã OTP không đúng. Bạn còn " + remaining + " lần thử.");
                return "redirect:/verify-otp?email=" + encode(emailTrimmed);
            }
        } catch (OtpExpiredException | TooManyAttemptsException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/verify-otp?email=" + encode(emailTrimmed);
        }
    }

    // ============================================================
    // RESEND OTP (AJAX)
    // ============================================================

    /**
     * Gửi lại OTP. Nhận JSON body hoặc form param.
     * Trả về JSON để AJAX xử lý trên frontend.
     */
    @PostMapping("/resend-otp")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> resendOtp(
            @RequestParam("email") String email) {

        String emailTrimmed = email.trim().toLowerCase();
        log.info("[AuthController] Resend OTP request for: {}", emailTrimmed);

        Optional<User> userOpt = userRepository.findByEmail(emailTrimmed);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Không tìm thấy tài khoản với email này."));
        }

        User user = userOpt.get();
        if (user.isEnabled()) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Tài khoản đã được xác thực rồi. Vui lòng đăng nhập."));
        }

        try {
            otpService.resendOtp(user);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đã gửi lại mã OTP tới " + emailTrimmed +
                            ". Vui lòng kiểm tra hộp thư (kể cả thư mục Spam)."));
        } catch (Exception e) {
            log.error("[AuthController] Resend OTP error for {}: {}", emailTrimmed, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    // ============================================================
    // Helper
    // ============================================================

    private String encode(String s) {
        try {
            return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return s;
        }
    }
}
