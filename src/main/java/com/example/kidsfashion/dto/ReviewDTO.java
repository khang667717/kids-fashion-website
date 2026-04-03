package com.example.kidsfashion.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private Long id;
    private Long userId;
    private String username;
    private Long productId;
    private String productName;
    private Integer rating;
    private String comment;
    private String status; // PENDING, APPROVED, REJECTED
    private LocalDateTime createdAt;
}