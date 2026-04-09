package com.example.kidsfashion.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a customer order.
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(nullable = false)
    private String status; // e.g., PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED

    @Column(name = "applied_coupon")
    private String appliedCoupon;

    // Shipping address snapshot (lưu snapshot, không FK tới Address vì address có thể bị sửa/xóa)
    @Column(name = "shipping_name")
    private String shippingName;

    @Column(name = "shipping_phone")
    private String shippingPhone;

    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress;

    // Các trường phục vụ thanh toán (VNPay / COD)
    @Column(name = "payment_method")
    private String paymentMethod; // "COD", "VNPAY"

    @Column(name = "payment_status")
    private String paymentStatus; // "PENDING", "PAID", "FAILED"

    @Column(name = "vnp_txn_ref")
    private String vnpTxnRef; // Mã giao dịch gửi sang VNPay

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}