package com.example.kidsfashion.service;

import com.example.kidsfashion.dto.CouponDTO;
import com.example.kidsfashion.entity.Coupon;
import com.example.kidsfashion.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final ModelMapper modelMapper;

    public List<CouponDTO> getAllCoupons() {
        return couponRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<CouponDTO> getActiveCoupons() {
        LocalDate today = LocalDate.now();
        return couponRepository.findAll().stream()
                .filter(c -> !c.getStartDate().isAfter(today) && !c.getEndDate().isBefore(today))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public CouponDTO getCouponById(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));
        return convertToDTO(coupon);
    }

    public CouponDTO getCouponByCode(String code) {
        LocalDate today = LocalDate.now();
        Coupon coupon = couponRepository.findByCodeAndStartDateLessThanEqualAndEndDateGreaterThanEqual(code, today, today)
                .orElseThrow(() -> new RuntimeException("Invalid or expired coupon"));
        return convertToDTO(coupon);
    }

    @Transactional
    public CouponDTO createCoupon(CouponDTO couponDTO) {
        Coupon coupon = convertToEntity(couponDTO);
        Coupon saved = couponRepository.save(coupon);
        return convertToDTO(saved);
    }

    @Transactional
    public CouponDTO updateCoupon(Long id, CouponDTO couponDTO) {
        Coupon existing = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));
        existing.setCode(couponDTO.getCode());
        existing.setDiscountPercent(couponDTO.getDiscountPercent());
        existing.setStartDate(couponDTO.getStartDate());
        existing.setEndDate(couponDTO.getEndDate());
        Coupon updated = couponRepository.save(existing);
        return convertToDTO(updated);
    }

    @Transactional
    public void deleteCoupon(Long id) {
        if (!couponRepository.existsById(id)) {
            throw new RuntimeException("Coupon not found");
        }
        couponRepository.deleteById(id);
    }

    public BigDecimal applyDiscount(BigDecimal total, String couponCode) {
        CouponDTO coupon = getCouponByCode(couponCode);
        BigDecimal discount = total.multiply(coupon.getDiscountPercent().divide(new BigDecimal(100)));
        return total.subtract(discount);
    }

    private CouponDTO convertToDTO(Coupon coupon) {
        return modelMapper.map(coupon, CouponDTO.class);
    }

    private Coupon convertToEntity(CouponDTO dto) {
        return modelMapper.map(dto, Coupon.class);
    }
}