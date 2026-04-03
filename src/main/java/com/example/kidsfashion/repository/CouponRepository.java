package com.example.kidsfashion.repository;

import com.example.kidsfashion.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCodeAndStartDateLessThanEqualAndEndDateGreaterThanEqual(String code, LocalDate now1, LocalDate now2);
}