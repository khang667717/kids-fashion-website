package com.example.kidsfashion.controller;

import com.example.kidsfashion.dto.CategoryDTO;
import com.example.kidsfashion.dto.CouponDTO;
import com.example.kidsfashion.dto.OrderDTO;
import com.example.kidsfashion.dto.ProductDTO;
import com.example.kidsfashion.dto.ReviewDTO;              // ✅ THÊM IMPORT
import com.example.kidsfashion.entity.User;
import com.example.kidsfashion.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;                         // ✅ THÊM IMPORT NÀY

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final CouponService couponService;
    private final UserService userService;
    private final ReviewService reviewService;              // ✅ THÊM DÒNG NÀY
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalProducts", productService.countAll());
        model.addAttribute("totalOrders", orderService.countAll());
        model.addAttribute("totalRevenue", orderService.getTotalRevenue());
        model.addAttribute("topProducts", productService.getTopSelling(5));
        model.addAttribute("latestOrders", orderService.getLatestOrders(5));

        // Thêm dữ liệu biểu đồ
        model.addAttribute("monthlyRevenue", orderService.getMonthlyRevenue());
        model.addAttribute("monthlyOrders", orderService.getMonthlyOrderCounts());
        model.addAttribute("months", List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun"));

        // ✅ THỐNG KÊ REVIEW
        List<ReviewDTO> allReviews = reviewService.getAllReviews();
        model.addAttribute("totalReviews", allReviews.size());
        model.addAttribute("recentReviews", allReviews.stream()
                .limit(5)
                .collect(Collectors.toList()));

        return "admin-dashboard";
    }

    // ========== Product Management ==========
    @GetMapping("/products")
    public String listProducts(@RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "size", defaultValue = "10") int size,
                               Model model) {
        Page<ProductDTO> productPage = productService.getProductsPaginated(page, size, "id", "asc");

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalProducts", productPage.getTotalElements());
        model.addAttribute("pageSize", size);

        return "admin-products";
    }

    @GetMapping("/products/new")
    public String newProductForm(Model model) {
        model.addAttribute("product", new ProductDTO());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin-product-form";
    }

    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute("product") ProductDTO productDTO,
                              @RequestParam("imageFile") MultipartFile imageFile,
                              RedirectAttributes redirectAttributes) {
        try {
            if (!imageFile.isEmpty() && !imageFile.getContentType().startsWith("image/")) {
                redirectAttributes.addFlashAttribute("error", "Only image files are allowed");
                return "redirect:/admin/products";
            }

            if (productDTO.getId() == null) {
                productService.createProduct(productDTO, imageFile);
                redirectAttributes.addFlashAttribute("success", "Product created successfully");
            } else {
                productService.updateProduct(productDTO.getId(), productDTO, imageFile);
                redirectAttributes.addFlashAttribute("success", "Product updated successfully");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable("id") Long id, Model model) {
        ProductDTO product = productService.getProductById(id);
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin-product-form";
    }

    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("success", "Product deleted");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/products";
    }

    // ========== Category Management ==========
    @GetMapping("/categories")
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin-categories";
    }

    @GetMapping("/categories/new")
    public String newCategoryForm(Model model) {
        model.addAttribute("category", new CategoryDTO());
        return "admin-category-form";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@ModelAttribute("category") CategoryDTO categoryDTO,
                               RedirectAttributes redirectAttributes) {
        try {
            if (categoryDTO.getId() == null) {
                categoryService.createCategory(categoryDTO);
                redirectAttributes.addFlashAttribute("success", "Category created");
            } else {
                categoryService.updateCategory(categoryDTO.getId(), categoryDTO);
                redirectAttributes.addFlashAttribute("success", "Category updated");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/categories/edit/{id}")
    public String editCategoryForm(@PathVariable("id") Long id, Model model) {
        CategoryDTO category = categoryService.getCategoryById(id);
        model.addAttribute("category", category);
        return "admin-category-form";
    }

    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("success", "Category deleted");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    // ========== Order Management ==========
    @GetMapping("/orders")
    public String listOrders(@RequestParam(value = "page", defaultValue = "0") int page,
                             @RequestParam(value = "size", defaultValue = "10") int size,
                             Model model) {
        // Lấy danh sách orders có phân trang
        Page<OrderDTO> orderPage = orderService.getOrdersPaginated(page, size);

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("totalItems", orderPage.getTotalElements());
        model.addAttribute("pageSize", size);

        return "admin-orders";
    }

    @GetMapping("/orders/{id}")
    public String viewOrder(@PathVariable("id") Long id, Model model) {
        OrderDTO order = orderService.getOrderById(id);
        model.addAttribute("order", order);
        return "admin-order-detail";
    }

    @PostMapping("/orders/update-status")
    public String updateOrderStatus(@RequestParam("orderId") Long orderId,
                                    @RequestParam("status") String status,
                                    RedirectAttributes redirectAttributes) {
        try {
            orderService.updateOrderStatus(orderId, status);
            redirectAttributes.addFlashAttribute("success", "Order status updated");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/orders";
    }

    // ========== Coupon Management ==========
    @GetMapping("/coupons")
    public String listCoupons(Model model) {
        model.addAttribute("coupons", couponService.getAllCoupons());
        return "admin-coupons";
    }

    @GetMapping("/coupons/new")
    public String newCouponForm(Model model) {
        model.addAttribute("coupon", new CouponDTO());
        return "admin-coupon-form";
    }

    @PostMapping("/coupons/save")
    public String saveCoupon(@ModelAttribute("coupon") CouponDTO couponDTO,
                             RedirectAttributes redirectAttributes) {
        try {
            if (couponDTO.getId() == null) {
                couponService.createCoupon(couponDTO);
                redirectAttributes.addFlashAttribute("success", "Coupon created");
            } else {
                couponService.updateCoupon(couponDTO.getId(), couponDTO);
                redirectAttributes.addFlashAttribute("success", "Coupon updated");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/coupons";
    }

    @GetMapping("/coupons/edit/{id}")
    public String editCouponForm(@PathVariable("id") Long id, Model model) {
        CouponDTO coupon = couponService.getCouponById(id);
        model.addAttribute("coupon", coupon);
        return "admin-coupon-form";
    }

    @GetMapping("/coupons/delete/{id}")
    public String deleteCoupon(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            couponService.deleteCoupon(id);
            redirectAttributes.addFlashAttribute("success", "Coupon deleted");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/coupons";
    }

    // ========== User Management ==========
    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin-users";
    }

    @GetMapping("/users/new")
    public String newUserForm(Model model) {
        model.addAttribute("user", new User());
        return "admin-user-form";
    }

    @PostMapping("/users/save")
    public String saveUser(@ModelAttribute("user") User user,
                           @RequestParam(value = "changePassword", required = false) Boolean changePassword,
                           RedirectAttributes redirectAttributes) {
        try {
            System.out.println("========== SAVE USER ==========");
            System.out.println("User ID: " + user.getId());
            System.out.println("Username: " + user.getUsername());
            System.out.println("Email: " + user.getEmail());
            System.out.println("Role: " + user.getRole());
            System.out.println("Raw password from form: " + user.getPassword());
            System.out.println("ChangePassword: " + changePassword);

            if (user.getId() == null) {
                // THÊM MỚI
                if (user.getPassword() == null || user.getPassword().isEmpty()) {
                    throw new RuntimeException("Password is required for new user");
                }
                user.setRole(user.getRole() != null ? user.getRole() : "ROLE_CUSTOMER");

                // Dùng createUser để tự động mã hóa
                userService.createUser(user);
                System.out.println("✅ New user created with encoded password");
                redirectAttributes.addFlashAttribute("success", "User created successfully");

            } else {
                // CẬP NHẬT
                User existing = userService.findById(user.getId())
                        .orElseThrow(() -> new RuntimeException("User not found"));

                System.out.println("Existing user password hash: " + existing.getPassword());

                // Cập nhật thông tin cơ bản
                existing.setUsername(user.getUsername());
                existing.setEmail(user.getEmail());
                existing.setRole(user.getRole());

                // XỬ LÝ PASSWORD
                if (changePassword != null && changePassword) {
                    System.out.println("Change password is CHECKED");
                    if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                        // Mã hóa password mới
                        String encodedPassword = passwordEncoder.encode(user.getPassword());
                        System.out.println("New raw password: " + user.getPassword());
                        System.out.println("New encoded password: " + encodedPassword);
                        existing.setPassword(encodedPassword);
                    } else {
                        throw new RuntimeException("New password is required when changing password");
                    }
                } else {
                    System.out.println("Change password is NOT checked - Keeping old password");
                    // Giữ nguyên password cũ - KHÔNG thay đổi
                }

                // Dùng updateUser để lưu mà KHÔNG mã hóa lại
                userService.updateUser(existing);
                System.out.println("Final saved password hash: " + existing.getPassword());
                redirectAttributes.addFlashAttribute("success", "User updated successfully");
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/users/edit/{id}")
    public String editUserForm(@PathVariable("id") Long id, Model model) {
        User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);
        return "admin-user-form";
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }
}