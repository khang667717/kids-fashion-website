package com.example.kidsfashion.controller;

import com.example.kidsfashion.entity.CartItem;
import com.example.kidsfashion.service.CartService;
import com.example.kidsfashion.service.CouponService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private static final Logger log = LoggerFactory.getLogger(CartController.class);

    private final CartService cartService;
    private final CouponService couponService;

    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        log.debug("=== VIEW CART ===");
        log.debug("Session ID: {}", session.getId());

        List<CartItem> cartItems = cartService.getCartItems(session);
        BigDecimal total = cartService.getDiscountedTotal(session);
        BigDecimal originalTotal = cartService.getTotalPrice(session);
        String appliedCoupon = cartService.getAppliedCoupon(session);

        log.debug("Cart items count: {}", cartItems.size());
        log.debug("Original total: {}", originalTotal);
        log.debug("Discounted total: {}", total);
        log.debug("Applied coupon: {}", appliedCoupon);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);
        model.addAttribute("originalTotal", originalTotal);
        model.addAttribute("savedAmount", originalTotal.subtract(total));
        model.addAttribute("appliedCoupon", appliedCoupon);
        model.addAttribute("cartEmpty", cartItems.isEmpty());
        model.addAttribute("activeCoupons", couponService.getActiveCoupons());

        return "cart";
    }

    @GetMapping("/api/cart/size")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCartSize(HttpSession session) {
        log.debug("=== GET CART SIZE API ===");
        log.debug("Session ID: {}", session.getId());

        int size = cartService.getCartSize(session);
        log.debug("Cart size: {}", size);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("cartSize", size);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToCart(@RequestParam("productId") Long productId,
                                                         @RequestParam(value = "quantity", defaultValue = "1") int quantity,
                                                         @RequestParam("size") String size,   // ✅ Thêm tham số size
                                                         HttpSession session) {
        log.info("========== CART ADD ENDPOINT CALLED ==========");
        log.info("Session ID: {}", session.getId());
        log.info("Product ID: {}", productId);
        log.info("Quantity: {}", quantity);
        log.info("Size: {}", size);

        try {
            if (quantity <= 0) {
                log.warn("Invalid quantity: {}", quantity);
                throw new RuntimeException("Quantity must be greater than 0");
            }

            log.debug("Calling cartService.addToCart with productId: {}, quantity: {}, size: {}", productId, quantity, size);
            cartService.addToCart(session, productId, quantity, size);
            log.debug("cartService.addToCart completed successfully");

            int cartSize = cartService.getCartSize(session);
            BigDecimal total = cartService.getDiscountedTotal(session);

            log.debug("Updated cart size: {}", cartSize);
            log.debug("Updated total: {}", total);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("cartSize", cartSize);
            response.put("total", total);
            response.put("message", "Product added to cart successfully");

            log.info("Returning success response: {}", response);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("ERROR in addToCart: {}", e.getMessage(), e);

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            log.error("Returning error response: {}", error);
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCart(@RequestParam("productId") Long productId,
                                                          @RequestParam("quantity") int quantity,
                                                          HttpSession session) {
        log.info("========== CART UPDATE ENDPOINT CALLED ==========");
        log.info("Session ID: {}", session.getId());
        log.info("Product ID: {}", productId);
        log.info("Quantity: {}", quantity);

        try {
            if (quantity <= 0) {
                log.warn("Invalid quantity: {}", quantity);
                throw new RuntimeException("Quantity must be greater than 0");
            }

            log.debug("Calling cartService.updateCartItem");
            cartService.updateCartItem(session, productId, quantity);
            log.debug("cartService.updateCartItem completed");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("total", cartService.getDiscountedTotal(session));
            response.put("cartSize", cartService.getCartSize(session));
            response.put("message", "Cart updated successfully");

            log.info("Returning success response");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("ERROR in updateCart: {}", e.getMessage(), e);

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/remove")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeFromCart(@RequestParam("productId") Long productId,
                                                              HttpSession session) {
        log.info("========== CART REMOVE ENDPOINT CALLED ==========");
        log.info("Session ID: {}", session.getId());
        log.info("Product ID: {}", productId);

        try {
            log.debug("Calling cartService.removeFromCart");
            cartService.removeFromCart(session, productId);
            log.debug("cartService.removeFromCart completed");

            int cartSize = cartService.getCartSize(session);
            boolean cartEmpty = cartSize == 0;

            log.debug("Cart empty: {}", cartEmpty);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("total", cartService.getDiscountedTotal(session));
            response.put("cartSize", cartSize);
            response.put("cartEmpty", cartEmpty);
            response.put("message", "Product removed from cart");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("ERROR in removeFromCart: {}", e.getMessage(), e);

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/apply-coupon")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> applyCoupon(@RequestParam("couponCode") String couponCode,
                                                           HttpSession session) {
        log.info("========== APPLY COUPON ENDPOINT CALLED ==========");
        log.info("Session ID: {}", session.getId());
        log.info("Coupon code: {}", couponCode);

        try {
            int cartSize = cartService.getCartSize(session);
            log.debug("Current cart size: {}", cartSize);

            if (cartSize == 0) {
                log.warn("Attempted to apply coupon to empty cart");
                throw new RuntimeException("Cannot apply coupon to empty cart");
            }

            BigDecimal currentTotal = cartService.getTotalPrice(session);
            log.debug("Current total before coupon: {}", currentTotal);

            log.debug("Calling cartService.applyCoupon");
            cartService.applyCoupon(session, couponCode);

            BigDecimal discountedTotal = cartService.getDiscountedTotal(session);
            BigDecimal savedAmount = cartService.getSavedAmount(session);

            log.debug("Discounted total: {}", discountedTotal);
            log.debug("Saved amount: {}", savedAmount);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("originalTotal", currentTotal);
            response.put("discountedTotal", discountedTotal);
            response.put("savedAmount", savedAmount);
            response.put("message", "Coupon applied successfully");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("ERROR in applyCoupon: {}", e.getMessage(), e);

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/remove-coupon")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeCoupon(HttpSession session) {
        log.info("========== REMOVE COUPON ENDPOINT CALLED ==========");
        log.info("Session ID: {}", session.getId());

        try {
            log.debug("Calling cartService.removeCoupon");
            cartService.removeCoupon(session);
            log.debug("cartService.removeCoupon completed");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("total", cartService.getTotalPrice(session));
            response.put("message", "Coupon removed successfully");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("ERROR in removeCoupon: {}", e.getMessage(), e);

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/summary")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCartSummary(HttpSession session) {
        log.debug("=== GET CART SUMMARY API ===");
        log.debug("Session ID: {}", session.getId());

        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("cartSize", cartService.getCartSize(session));
            response.put("total", cartService.getDiscountedTotal(session));
            response.put("originalTotal", cartService.getTotalPrice(session));
            response.put("savedAmount", cartService.getSavedAmount(session));
            response.put("appliedCoupon", cartService.getAppliedCoupon(session));
            response.put("items", cartService.getCartItems(session));

            log.debug("Cart summary retrieved successfully");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("ERROR in getCartSummary: {}", e.getMessage(), e);

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}