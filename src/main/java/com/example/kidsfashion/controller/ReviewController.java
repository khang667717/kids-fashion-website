package com.example.kidsfashion.controller;

import com.example.kidsfashion.dto.ReviewDTO;
import com.example.kidsfashion.entity.User;
import com.example.kidsfashion.service.ReviewService;
import com.example.kidsfashion.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;

    @PostMapping("/product/{productId}/review")
    public String submitReview(@PathVariable("productId") Long productId,
                               @RequestParam("rating") Integer rating,
                               @RequestParam(value = "comment", required = false) String comment,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            reviewService.addReview(user.getId(), productId, rating, comment);

            // ✅ SỬA THÔNG BÁO - không còn chờ admin duyệt
            redirectAttributes.addFlashAttribute("success",
                    " Thank you! Your review has been posted successfully and is now visible to everyone.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/product/" + productId;
    }
}