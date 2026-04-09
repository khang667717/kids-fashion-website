package com.example.kidsfashion.controller;

import com.example.kidsfashion.entity.Address;
import com.example.kidsfashion.entity.User;
import com.example.kidsfashion.service.AddressService;
import com.example.kidsfashion.service.OrderService;
import com.example.kidsfashion.service.UserService;
import com.example.kidsfashion.dto.OrderDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final AddressService addressService;

    @GetMapping("/checkout")
    public String checkoutPage(@AuthenticationPrincipal UserDetails userDetails,
                               Model model, HttpSession session) {
        List<?> cart = (List<?>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            return "redirect:/cart";
        }

        // Load danh sách địa chỉ của user
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Address> addresses = addressService.getAddressesByUser(user);
        model.addAttribute("addresses", addresses);

        return "checkout";
    }

    // NOTE: POST /checkout/place-order đã được di chuyển sang PaymentController API (/api/payment)

    @GetMapping("/orders")
    public String userOrders(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("orders", orderService.getOrdersByUser(user.getId()));
        return "orders";
    }

    @GetMapping("/order/{id}")
    public String viewOrderDetail(@PathVariable("id") Long id,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  Model model) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        OrderDTO order = orderService.getOrderById(id);

        if (!order.getUserId().equals(user.getId())) {
            throw new RuntimeException("You don't have permission to view this order");
        }

        model.addAttribute("order", order);
        return "order-detail";
    }
}