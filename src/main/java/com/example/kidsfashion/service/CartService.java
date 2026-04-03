package com.example.kidsfashion.service;

import com.example.kidsfashion.entity.CartItem;
import com.example.kidsfashion.entity.Product;
import com.example.kidsfashion.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final ProductRepository productRepository;
    private static final String CART_SESSION_KEY = "cart";
    private static final String COUPON_SESSION_KEY = "appliedCoupon";
    private static final String DISCOUNTED_TOTAL_KEY = "discountedTotal";
    private final CouponService couponService;

    @SuppressWarnings("unchecked")
    private List<CartItem> getCart(HttpSession session) {
        Object cartObj = session.getAttribute(CART_SESSION_KEY);
        List<CartItem> cart;

        if (cartObj instanceof List) {
            cart = (List<CartItem>) cartObj;
        } else {
            cart = new ArrayList<>();
            session.setAttribute(CART_SESSION_KEY, cart);
        }
        return cart;
    }

    public List<CartItem> getCartItems(HttpSession session) {
        return getCart(session);
    }

    // ===== PHƯƠNG THỨC THÊM VÀO ĐỂ TƯƠNG THÍCH =====
    @Transactional(readOnly = true)
    public void addToCart(HttpSession session, Long productId, Integer quantity) {
        // Gọi phương thức chính với size mặc định là null
        // Lưu ý: UI đã được sửa để luôn gửi size, nhưng để an toàn ta thêm phương thức này
        this.addToCart(session, productId, quantity, null);
    }

    @Transactional(readOnly = true)
    public void addToCart(HttpSession session, Long productId, Integer quantity, String size) {
        // Validate quantity
        if (quantity == null || quantity <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }

        List<CartItem> cart = getCart(session);

        // Check product exists and stock
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        // ✅ Kiểm tra size có hợp lệ không
        if (size == null || size.trim().isEmpty()) {
            throw new RuntimeException("Please select a size");
        }

        // Kiểm tra product có sizes không (cho các sản phẩm cũ có thể chưa có size)
        if (product.getSizes() != null && !product.getSizes().isEmpty()) {
            boolean sizeValid = product.getSizes().stream()
                    .anyMatch(s -> s.name().equals(size));
            if (!sizeValid) {
                throw new RuntimeException("Size " + size + " is not available for this product");
            }
        }

        int currentStock = product.getStock();
        int currentQuantityInCart = cart.stream()
                .filter(item -> item.getProductId().equals(productId) &&
                        (size == null ? item.getSize() == null : size.equals(item.getSize())))
                .mapToInt(CartItem::getQuantity)
                .sum();

        if (currentQuantityInCart + quantity > currentStock) {
            int available = currentStock - currentQuantityInCart;
            if (available <= 0) {
                throw new RuntimeException("Product is out of stock");
            }
            throw new RuntimeException("Only " + available + " items available (you already have " +
                    currentQuantityInCart + " in cart)");
        }

        // Tìm item cùng product và size
        Optional<CartItem> existingItem = cart.stream()
                .filter(item -> item.getProductId().equals(productId) &&
                        (size == null ? item.getSize() == null : size.equals(item.getSize())))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setProductId(product.getId());
            newItem.setProductName(product.getName());
            newItem.setPrice(product.getPrice());
            newItem.setQuantity(quantity);
            newItem.setImageUrl(product.getImageUrl());
            newItem.setSize(size);
            cart.add(newItem);
        }
        session.setAttribute(CART_SESSION_KEY, cart);
        session.removeAttribute(DISCOUNTED_TOTAL_KEY);
    }

    @Transactional(readOnly = true)
    public void updateCartItem(HttpSession session, Long productId, Integer quantity) {
        // Validate quantity
        if (quantity == null || quantity <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }

        List<CartItem> cart = getCart(session);

        // Tìm item (có thể có nhiều size khác nhau? Ở đây ta chỉ cho sửa số lượng của item đầu tiên tìm thấy – thực tế cần phân biệt theo size, nhưng UI hiện tại chưa hỗ trợ sửa theo size riêng.
        // Để đơn giản, ta giả sử mỗi sản phẩm chỉ có một size trong giỏ (không thể có 2 size khác nhau của cùng sản phẩm). Nếu có nhiều size, cần thêm logic phức tạp hơn.
        // Tạm thời lấy item đầu tiên có productId.
        CartItem item = cart.stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found in cart"));

        // Check stock
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (quantity > product.getStock()) {
            throw new RuntimeException("Only " + product.getStock() + " items available");
        }

        item.setQuantity(quantity);
        session.setAttribute(CART_SESSION_KEY, cart);
        session.removeAttribute(DISCOUNTED_TOTAL_KEY);
    }

    public void removeFromCart(HttpSession session, Long productId) {
        List<CartItem> cart = getCart(session);
        cart.removeIf(item -> item.getProductId().equals(productId));
        session.setAttribute(CART_SESSION_KEY, cart);

        if (cart.isEmpty()) {
            session.removeAttribute(COUPON_SESSION_KEY);
            session.removeAttribute(DISCOUNTED_TOTAL_KEY);
        } else {
            session.removeAttribute(DISCOUNTED_TOTAL_KEY);
        }
    }

    public void clearCart(HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
        session.removeAttribute(COUPON_SESSION_KEY);
        session.removeAttribute(DISCOUNTED_TOTAL_KEY);
    }

    public BigDecimal getTotalPrice(HttpSession session) {
        List<CartItem> cart = getCart(session);
        return cart.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getDiscountedTotal(HttpSession session) {
        BigDecimal discountedTotal = (BigDecimal) session.getAttribute(DISCOUNTED_TOTAL_KEY);
        if (discountedTotal != null) {
            return discountedTotal;
        }
        return getTotalPrice(session);
    }

    public BigDecimal getSavedAmount(HttpSession session) {
        BigDecimal originalTotal = getTotalPrice(session);
        BigDecimal discountedTotal = getDiscountedTotal(session);
        return originalTotal.subtract(discountedTotal);
    }

    public int getCartSize(HttpSession session) {
        return getCart(session).size();
    }

    public void applyCoupon(HttpSession session, String couponCode) {
        if (couponCode == null || couponCode.trim().isEmpty()) {
            throw new RuntimeException("Coupon code cannot be empty");
        }

        BigDecimal originalTotal = getTotalPrice(session);
        BigDecimal discountedTotal = couponService.applyDiscount(originalTotal, couponCode);

        session.setAttribute(COUPON_SESSION_KEY, couponCode.trim().toUpperCase());
        session.setAttribute(DISCOUNTED_TOTAL_KEY, discountedTotal);
    }

    public String getAppliedCoupon(HttpSession session) {
        return (String) session.getAttribute(COUPON_SESSION_KEY);
    }

    public boolean hasItems(HttpSession session) {
        return !getCart(session).isEmpty();
    }

    public void removeCoupon(HttpSession session) {
        session.removeAttribute(COUPON_SESSION_KEY);
        session.removeAttribute(DISCOUNTED_TOTAL_KEY);
    }

    public boolean hasAppliedCoupon(HttpSession session) {
        return session.getAttribute(COUPON_SESSION_KEY) != null;
    }
}