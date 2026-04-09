package com.example.kidsfashion.controller;

import com.example.kidsfashion.entity.Order;
import com.example.kidsfashion.entity.User;
import com.example.kidsfashion.service.OrderService;
import com.example.kidsfashion.service.UserService;
import com.example.kidsfashion.service.VNPayService;
import com.example.kidsfashion.repository.OrderRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class PaymentController {

    private final OrderService orderService;
    private final UserService userService;
    private final VNPayService vnPayService;
    private final OrderRepository orderRepository;

    @PostMapping("/api/payment")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> processPayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("addressId") Long addressId,
            @RequestParam("paymentMethod") String paymentMethod,
            HttpSession session,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();
        try {
            User user = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Tạo order với status = PENDING (dù COD hay VNPAY)
            Order order = orderService.createOrder(user.getId(), addressId, paymentMethod, session);

            if ("VNPAY".equalsIgnoreCase(paymentMethod)) {
                // Tạo payment URL
                String paymentUrl = vnPayService.createPaymentUrl(order, request);
                // Lưu vnpTxnRef vào DB
                orderRepository.save(order);
                
                response.put("success", true);
                response.put("paymentUrl", paymentUrl);
            } else {
                // Mặc định COD
                response.put("success", true);
                response.put("redirectUrl", "/orders");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/vnpay-callback")
    public String vnpayCallback(@RequestParam Map<String, String> params, RedirectAttributes redirectAttributes) {
        try {
            boolean isValid = vnPayService.verifyCallback(params);
            
            String vnp_TxnRef = params.get("vnp_TxnRef");
            String vnp_ResponseCode = params.get("vnp_ResponseCode");
            
            if (vnp_TxnRef == null) {
                redirectAttributes.addFlashAttribute("error", "Mã giao dịch không hợp lệ.");
                return "redirect:/orders";
            }

            // Extract order ID từ chuỗi: orderId_timestamp
            Long orderId = Long.parseLong(vnp_TxnRef.split("_")[0]);
            Order order = orderRepository.findById(orderId).orElse(null);

            if (order != null) {
                if (isValid && "00".equals(vnp_ResponseCode)) {
                    order.setPaymentStatus("PAID");
                    order.setStatus("PROCESSING"); // Cập nhật trạng thái đơn hàng chung
                    orderRepository.save(order);
                    redirectAttributes.addFlashAttribute("success", "Thanh toán thành công! Mã giao dịch: " + params.get("vnp_TransactionNo"));
                } else {
                    order.setPaymentStatus("FAILED");
                    orderRepository.save(order);
                    redirectAttributes.addFlashAttribute("error", "Thanh toán thất bại hoặc đã bị huỷ.");
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi xử lý giao dịch: " + e.getMessage());
        }
        return "redirect:/orders";
    }
}
