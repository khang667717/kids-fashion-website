package com.example.kidsfashion.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user (customer or admin).
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String role; // e.g., "ROLE_ADMIN", "ROLE_CUSTOMER"

    // ===== Email OTP Verification =====
    /** true nếu email đã xác thực hoặc là admin. false = chưa xác thực. */
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean enabled = false;

    /** Hash (SHA-256) của OTP 6 số. null sau khi xác thực thành công. */
    @Column(name = "otp_code")
    private String otp;

    /** Thời điểm OTP hết hạn (5 phút từ lúc tạo). */
    @Column(name = "otp_expiry")
    private LocalDateTime otpExpiry;

    /** Số lần nhập sai OTP. Reset về 0 sau mỗi lần resend hoặc xác thực thành công. */
    @Column(name = "otp_attempt", nullable = false, columnDefinition = "INT DEFAULT 0")
    private int otpAttempt = 0;

    /** Thời điểm gửi OTP gần nhất – dùng để rate-limit resend (30s). */
    @Column(name = "last_otp_sent_time")
    private LocalDateTime lastOtpSentTime;

    // ===== Profile fields =====
    @Column(name = "full_name")
    private String fullName;

    private String phone;

    private String gender; // MALE, FEMALE, OTHER

    private LocalDate birthday;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> addresses = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}