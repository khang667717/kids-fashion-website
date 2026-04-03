package com.example.kidsfashion.service;

import com.example.kidsfashion.entity.User;
import com.example.kidsfashion.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("========== LOGIN ATTEMPT ==========");
        System.out.println("Username: " + username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        System.out.println("User found: " + user.getUsername());
        System.out.println("Password hash in DB: " + user.getPassword());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().replace("ROLE_", ""))
                .build();
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Method cho THÊM MỚI - tự mã hóa password
    public User createUser(User user) {
        // Mã hóa password trước khi lưu
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    // Method cho CẬP NHẬT - giữ nguyên password đã được mã hóa từ Controller
    public User updateUser(User user) {
        // KHÔNG mã hóa lại password - giữ nguyên
        return userRepository.save(user);
    }

    // Giữ lại method cũ cho tương thích (nếu cần)
    @Deprecated
    public User save(User user) {
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    /**
     * Cập nhật thông tin hồ sơ (không thay đổi password/username/role).
     */
    public User updateProfile(User user) {
        User existing = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        existing.setFullName(user.getFullName());
        existing.setEmail(user.getEmail());
        existing.setPhone(user.getPhone());
        existing.setGender(user.getGender());
        existing.setBirthday(user.getBirthday());
        existing.setAvatarUrl(user.getAvatarUrl());
        return userRepository.save(existing);
    }

    /**
     * Đổi mật khẩu - kiểm tra mật khẩu hiện tại trước.
     */
    public boolean changePassword(User user, String currentPassword, String newPassword) {
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return false; // Mật khẩu hiện tại không đúng
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }
}