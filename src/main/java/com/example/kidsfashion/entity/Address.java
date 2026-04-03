package com.example.kidsfashion.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a shipping address for a user.
 */
@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String phone;

    @Column(name = "address_line", nullable = false)
    private String addressLine; // Số nhà, tên đường

    private String ward;     // Phường/Xã

    private String district; // Quận/Huyện

    @Column(nullable = false)
    private String city;     // Tỉnh/Thành phố

    @Column(nullable = false)
    private String label; // HOME, WORK, OTHER

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Trả về địa chỉ đầy đủ dạng chuỗi.
     */
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        sb.append(addressLine);
        if (ward != null && !ward.isEmpty()) sb.append(", ").append(ward);
        if (district != null && !district.isEmpty()) sb.append(", ").append(district);
        sb.append(", ").append(city);
        return sb.toString();
    }
}
