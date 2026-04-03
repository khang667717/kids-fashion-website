package com.example.kidsfashion.controller;

import com.example.kidsfashion.dto.CategoryDTO;
import com.example.kidsfashion.dto.ProductDTO;
import com.example.kidsfashion.entity.User;
import com.example.kidsfashion.service.CategoryService;
import com.example.kidsfashion.service.ProductService;
import com.example.kidsfashion.service.ReviewService;
import com.example.kidsfashion.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ReviewService reviewService; // Thêm ReviewService
    private final UserService userService; // Thêm UserService

    @GetMapping("/")
    public String home(Model model) {
        List<ProductDTO> latestProducts = productService.getLatestProducts(8);
        List<ProductDTO> bestSelling = productService.getBestSellingProducts(8);
        List<CategoryDTO> categories = categoryService.getAllCategories();
        model.addAttribute("latestProducts", latestProducts);
        model.addAttribute("bestSelling", bestSelling);
        model.addAttribute("categories", categories);
        return "index";
    }

    @GetMapping("/products")
    public String listProducts(@RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size,
            @RequestParam(value = "category", required = false) Long category,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "direction", defaultValue = "asc") String direction,
            Model model) {
        Page<ProductDTO> productPage;
        if (category != null) {
            productPage = productService.getProductsByCategory(category, page, size, sortBy, direction);
        } else {
            productPage = productService.getProductsPaginated(page, size, sortBy, direction);
        }
        List<CategoryDTO> categories = categoryService.getAllCategories();

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("categories", categories);
        return "product-list";
    }

    @GetMapping("/category/{slug}")
    public String listProductsByCategorySlug(@PathVariable("slug") String slug,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "direction", defaultValue = "asc") String direction,
            Model model) {
        List<CategoryDTO> categories = categoryService.getAllCategories();

        Long categoryId = null;
        for (CategoryDTO cat : categories) {
            if (slug.equals(cat.getSlug())) {
                categoryId = cat.getId();
                break;
            }
        }

        if (categoryId == null) {
            return "redirect:/products";
        }

        Page<ProductDTO> productPage = productService.getProductsByCategory(categoryId, page, size, sortBy, direction);

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("categories", categories);

        return "product-list";
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable("id") Long id, Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
        ProductDTO product = productService.getProductById(id);
        model.addAttribute("product", product);

        // Lấy danh sách review đã duyệt
        try {
            model.addAttribute("reviews", reviewService.getApprovedReviewsForProduct(id));
        } catch (Exception e) {
            // Nếu có lỗi (ví dụ ReviewService chưa được implement đầy đủ), hiển thị danh
            // sách rỗng
            model.addAttribute("reviews", List.of());
        }

        // Kiểm tra user hiện tại có thể review không
        boolean canReview = false;
        if (userDetails != null) {
            try {
                User user = userService.findByUsername(userDetails.getUsername())
                        .orElse(null);
                if (user != null) {
                    canReview = reviewService.canReview(user.getId(), id);
                }
            } catch (Exception e) {
                // Nếu có lỗi, mặc định không cho review
                canReview = false;
            }
        }
        model.addAttribute("canReview", canReview);

        return "product-detail";
    }

    @GetMapping("/search")
    public String searchProducts(@RequestParam("keyword") String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size,
            Model model) {
        Page<ProductDTO> productPage = productService.searchProducts(keyword, page, size);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("keyword", keyword);
        return "product-list";
    }
}