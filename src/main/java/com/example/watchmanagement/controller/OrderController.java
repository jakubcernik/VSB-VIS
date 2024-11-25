package com.example.watchmanagement.controller;

import com.example.watchmanagement.model.*;
import com.example.watchmanagement.repository.OrderRepository;
import com.example.watchmanagement.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/order")
public class OrderController {

    private final OrderRepository orderRepository;

    public OrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping("/create")
    public String createOrder(HttpSession session) {
        Cart cart = (Cart) session.getAttribute("cart");
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return "redirect:/login";
        }

        if (cart == null || cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        Order order = new Order();
        order.setUser(loggedInUser);
        order.setStatus("Pending");
        order.setTotalPrice(cart.getTotalPrice());

        List<OrderItem> orderItems = new ArrayList<>();
        for (Map.Entry<Watch, Integer> entry : cart.getItems().entrySet()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setWatch(entry.getKey());
            orderItem.setQuantity(entry.getValue());
            orderItems.add(orderItem);
        }
        order.setOrderItems(orderItems);

        orderRepository.save(order);

        // Vyprázdni košík
        session.removeAttribute("cart");

        return "redirect:/";
    }
}
