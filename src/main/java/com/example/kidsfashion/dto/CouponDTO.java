package com.example.kidsfashion.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object for Coupon.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CouponDTO {
    private Long id;
    private String code;
    private BigDecimal discountPercent;
    private LocalDate startDate;
    private LocalDate endDate;
}