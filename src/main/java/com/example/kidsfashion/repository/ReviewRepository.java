package com.example.kidsfashion.repository;

import com.example.kidsfashion.entity.Product;
import com.example.kidsfashion.entity.Review;
import com.example.kidsfashion.entity.ReviewStatus;
import com.example.kidsfashion.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // ✅ ĐÃ THÊM
    List<Review> findByProductAndStatus(Product product, ReviewStatus status);

    Optional<Review> findByUserAndProduct(User user, Product product);

    boolean existsByUserAndProduct(User user, Product product);

    // ✅ SỬA LẠI
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId AND r.status = 'APPROVED'")
    Double getAverageRatingForProduct(@Param("productId") Long productId);

    // ✅ SỬA LẠI
    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId AND r.status = 'APPROVED'")
    Long countApprovedByProduct(@Param("productId") Long productId);

    List<Review> findAllByOrderByCreatedAtDesc();

    List<Review> findByStatus(ReviewStatus status);

    List<Review> findByProductId(Long productId);
}