package com.example.kidsfashion.controller;

import com.example.kidsfashion.dto.ReviewDTO;
import com.example.kidsfashion.entity.ReviewStatus;
import com.example.kidsfashion.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public String listReviews(@RequestParam(value = "status", required = false) ReviewStatus status, Model model) {
        List<ReviewDTO> reviews;
        if (status != null) {
            reviews = reviewService.getReviewsByStatus(status);
        } else {
            reviews = reviewService.getAllReviews();
        }
        model.addAttribute("reviews", reviews);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", ReviewStatus.values());
        return "admin-reviews";
    }

    @GetMapping("/{id}")
    public String viewReview(@PathVariable("id") Long id, Model model) {
        model.addAttribute("review", reviewService.getReviewById(id));
        return "admin-review-detail";
    }

    @PostMapping("/{id}/approve")
    public String approveReview(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            reviewService.updateReviewStatus(id, ReviewStatus.APPROVED);
            redirectAttributes.addFlashAttribute("success", "Review approved");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/reviews";
    }

    @PostMapping("/{id}/reject")
    public String rejectReview(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            reviewService.updateReviewStatus(id, ReviewStatus.REJECTED);
            redirectAttributes.addFlashAttribute("success", "Review rejected");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/reviews";
    }

    @PostMapping("/{id}/delete")
    public String deleteReview(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            reviewService.deleteReview(id);
            redirectAttributes.addFlashAttribute("success", "Review deleted");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/reviews";
    }
}