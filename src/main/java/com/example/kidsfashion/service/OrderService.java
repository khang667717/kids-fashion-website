package com.example.kidsfashion.service;

import com.example.kidsfashion.dto.OrderDTO;
import com.example.kidsfashion.dto.OrderItemDTO;
import com.example.kidsfashion.entity.*;
import com.example.kidsfashion.repository.OrderRepository;
import com.example.kidsfashion.repository.ProductRepository;
import com.example.kidsfashion.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;  // Thêm import này
import org.springframework.data.domain.Page;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final CouponService couponService;
    private final ModelMapper modelMapper;

    /**
     * Tạo đơn hàng mới từ giỏ hàng trong session
     */
    @Transactional
    public Order createOrder(Long userId, HttpSession session) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CartItem> cartItems = cartService.getCartItems(session);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus("PENDING");

        // Xử lý giá tổng và Coupon
        BigDecimal total = cartService.getTotalPrice(session);
        String couponCode = cartService.getAppliedCoupon(session);
        if (couponCode != null) {
            total = couponService.applyDiscount(total, couponCode);
            order.setAppliedCoupon(couponCode);
        }
        order.setTotalPrice(total);

        // Chuyển đổi CartItem sang OrderItem và cập nhật kho hàng
        List<OrderItem> orderItems = cartItems.stream().map(cartItem -> {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            // Kiểm tra và trừ tồn kho
            if (product.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getPrice());

            // ✅ QUAN TRỌNG: Lưu Size từ giỏ hàng vào chi tiết đơn hàng
            orderItem.setSize(cartItem.getSize());

            return orderItem;
        }).collect(Collectors.toList());

        order.setOrderItems(orderItems);
        Order savedOrder = orderRepository.save(order);

        // Xóa giỏ hàng sau khi đặt hàng thành công
        cartService.clearCart(session);

        return savedOrder;
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return orderRepository.findByUser(user).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return convertToDTO(order);
    }

    @Transactional
    public OrderDTO updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        Order updated = orderRepository.save(order);
        return convertToDTO(updated);
    }

    @Transactional(readOnly = true)
    public long countAll() {
        return orderRepository.count();
    }

    @Transactional(readOnly = true)
    public Double getTotalRevenue() {
        Double revenue = orderRepository.getTotalRevenue();
        return revenue != null ? revenue : 0.0;
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getLatestOrders(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return orderRepository.findLatestOrders(pageable).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<OrderDTO> getOrdersPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return orderRepository.findAll(pageable).map(this::convertToDTO);
    }

    // Mock data cho Dashboard
    @Transactional(readOnly = true)
    public List<Double> getMonthlyRevenue() {
        return Arrays.asList(1200.0, 1900.0, 3000.0, 2500.0, 4200.0, 3800.0);
    }

    @Transactional(readOnly = true)
    public List<Integer> getMonthlyOrderCounts() {
        return Arrays.asList(15, 20, 25, 22, 30, 28);
    }



    /**
     * Chuyển đổi Entity sang DTO để hiển thị ra View
     */
    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = modelMapper.map(order, OrderDTO.class);
        dto.setUserId(order.getUser().getId());
        dto.setUsername(order.getUser().getUsername());

        // Đảm bảo Coupon được truyền đi
        dto.setAppliedCoupon(order.getAppliedCoupon());

        // Chuyển đổi danh sách OrderItem sang OrderItemDTO (bao gồm cả Size)
        List<OrderItemDTO> itemDTOs = order.getOrderItems().stream()
                .map(item -> new OrderItemDTO(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getSize(), // ✅ Lấy Size thực tế từ database
                        item.getPrice()
                ))
                .collect(Collectors.toList());

        dto.setItems(itemDTOs);
        return dto;
    }
}