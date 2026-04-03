package com.example.kidsfashion.service;

import com.example.kidsfashion.dto.ReviewDTO;
import com.example.kidsfashion.entity.*;
import com.example.kidsfashion.repository.OrderRepository;
import com.example.kidsfashion.repository.ProductRepository;
import com.example.kidsfashion.repository.ReviewRepository;
import com.example.kidsfashion.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    /**
     * Kiểm tra user có thể review sản phẩm không (đã mua và đơn hàng đã giao)
     */
    @Transactional(readOnly = true)
    public boolean canReview(Long userId, Long productId) {
        try {
            System.out.println("===== CAN REVIEW CHECK =====");
            System.out.println("User ID: " + userId);
            System.out.println("Product ID: " + productId);

            boolean hasPurchased = orderRepository.existsByUserIdAndProductIdAndStatus(
                    userId, productId, "DELIVERED");
            System.out.println("Has purchased (DELIVERED): " + hasPurchased);

            if (!hasPurchased) return false;

            boolean alreadyReviewed = reviewRepository.existsByUserAndProduct(
                    userRepository.getReferenceById(userId),
                    productRepository.getReferenceById(productId));
            System.out.println("Already reviewed: " + alreadyReviewed);

            return !alreadyReviewed;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Thêm mới review (user đã mua)
     */
    @Transactional
    public ReviewDTO addReview(Long userId, Long productId, Integer rating, String comment) {
        System.out.println("===== ADD REVIEW =====");
        System.out.println("User ID: " + userId);
        System.out.println("Product ID: " + productId);
        System.out.println("Rating: " + rating);
        System.out.println("Comment: " + comment);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Kiểm tra đã review chưa
        if (reviewRepository.existsByUserAndProduct(user, product)) {
            throw new RuntimeException("You have already reviewed this product");
        }

        // Kiểm tra quyền review
        if (!canReview(userId, productId)) {
            throw new RuntimeException("You can only review products you have purchased and received");
        }

        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(rating);
        review.setComment(comment);
        // ✅ KHÔNG cần set status vì Entity đã mặc định là APPROVED
        // review.setStatus(ReviewStatus.PENDING);  // <--- XÓA DÒNG NÀY

        Review saved = reviewRepository.save(review);
        System.out.println("Review saved with ID: " + saved.getId());
        System.out.println("Status: APPROVED (auto-approved)"); // Thêm log

        return convertToDTO(saved);
    }

    /**
     * Lấy danh sách review đã được duyệt của sản phẩm
     */
    @Transactional(readOnly = true)
    public List<ReviewDTO> getApprovedReviewsForProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        return reviewRepository.findByProductAndStatus(product, ReviewStatus.APPROVED)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Tính điểm trung bình của sản phẩm (chỉ tính review đã duyệt)
     */
    @Transactional(readOnly = true)
    public Double getAverageRating(Long productId) {
        Double avg = reviewRepository.getAverageRatingForProduct(productId);
        return avg != null ? Math.round(avg * 10) / 10.0 : 0.0;
    }

    /**
     * Đếm số lượng review đã duyệt
     */
    @Transactional(readOnly = true)
    public Long getReviewCount(Long productId) {
        Long count = reviewRepository.countApprovedByProduct(productId);
        return count != null ? count : 0L;
    }

    // ========== Admin methods ==========

    @Transactional(readOnly = true)
    public List<ReviewDTO> getAllReviews() {
        return reviewRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReviewDTO> getReviewsByStatus(ReviewStatus status) {
        return reviewRepository.findByStatus(status)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReviewDTO getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        return convertToDTO(review);
    }

    @Transactional
    public ReviewDTO updateReviewStatus(Long id, ReviewStatus status) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        review.setStatus(status);
        return convertToDTO(reviewRepository.save(review));
    }

    @Transactional
    public void deleteReview(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new RuntimeException("Review not found");
        }
        reviewRepository.deleteById(id);
    }

    /**
     * Convert Entity sang DTO - Đã sửa lỗi LAZY loading bằng Hibernate.initialize()
     */
    private ReviewDTO convertToDTO(Review review) {
        // Đánh thức các đối tượng LAZY trước khi session đóng
        if (review.getUser() != null) {
            Hibernate.initialize(review.getUser());
        }
        if (review.getProduct() != null) {
            Hibernate.initialize(review.getProduct());
        }

        // Tạo mới DTO thủ công
        ReviewDTO dto = new ReviewDTO();

        // Map các trường cơ bản
        dto.setId(review.getId());

        if (review.getUser() != null) {
            dto.setUserId(review.getUser().getId());
            dto.setUsername(review.getUser().getUsername());
        }

        if (review.getProduct() != null) {
            dto.setProductId(review.getProduct().getId());
            dto.setProductName(review.getProduct().getName());
        }

        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setStatus(review.getStatus() != null ? review.getStatus().toString() : null);
        dto.setCreatedAt(review.getCreatedAt());

        return dto;
    }
}