package com.example.kidsfashion.controller;

import com.example.kidsfashion.dto.OrderDTO;
import com.example.kidsfashion.entity.User;
import com.example.kidsfashion.service.OrderService;
import com.example.kidsfashion.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    @GetMapping("/checkout")
    public String checkoutPage(Model model, HttpSession session) {
        List<?> cart = (List<?>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            return "redirect:/cart";
        }
        return "checkout";
    }

    @PostMapping("/checkout/place-order")
    public String placeOrder(@AuthenticationPrincipal UserDetails userDetails,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            orderService.createOrder(user.getId(), session);
            redirectAttributes.addFlashAttribute("success", "Order placed successfully!");
            return "redirect:/orders";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/checkout";
        }
    }

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