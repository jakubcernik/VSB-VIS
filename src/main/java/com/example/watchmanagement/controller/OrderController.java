package com.example.watchmanagement.controller;

import com.example.watchmanagement.model.Order;
import com.example.watchmanagement.model.OrderItem;
import com.example.watchmanagement.model.User;
import com.example.watchmanagement.model.Watch;
import com.example.watchmanagement.repository.OrderRepository;
import com.example.watchmanagement.repository.OrderItemRepository;
import com.example.watchmanagement.repository.WatchRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/order")
public class OrderController {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final WatchRepository watchRepository;

    public OrderController(OrderRepository orderRepository, OrderItemRepository orderItemRepository, WatchRepository watchRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.watchRepository = watchRepository;
    }

    @GetMapping("/cart")
    public String showCart(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login"; // Přesměrování, pokud není uživatel přihlášen
        }

        // Najdeme košík uživatele (stav PENDING)
        Order cart = orderRepository.findByUserAndStatus(loggedInUser, "PENDING")
                .orElseGet(() -> {
                    Order newCart = new Order();
                    newCart.setUser(loggedInUser);
                    newCart.setStatus("PENDING");
                    return orderRepository.save(newCart); // Vytvoříme nový košík
                });

        model.addAttribute("cart", cart);
        return "cart"; // Vrátí šablonu cart.html
    }


    @GetMapping("/cart/add/{id}")
    public String addToCart(@PathVariable Long id, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Watch watch = watchRepository.findById(id).orElse(null);
        if (watch == null) {
            return "redirect:/home";
        }

        // Najdeme košík uživatele nebo vytvoříme nový
        Order cart = orderRepository.findByUserAndStatus(loggedInUser, "PENDING")
                .orElseGet(() -> {
                    Order newCart = new Order();
                    newCart.setUser(loggedInUser);
                    newCart.setStatus("PENDING");
                    return orderRepository.save(newCart);
                });

        // Přidáme položku do košíku nebo zvýšíme množství
        Optional<OrderItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getWatch().getId().equals(id))
                .findFirst();

        if (existingItem.isPresent()) {
            OrderItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + 1);
            orderItemRepository.save(item);
        } else {
            OrderItem newItem = new OrderItem();
            newItem.setOrder(cart);
            newItem.setWatch(watch);
            newItem.setQuantity(1);
            orderItemRepository.save(newItem);
        }

        return "redirect:/order/cart";
    }

    @GetMapping("/cart/remove/{id}")
    public String removeFromCart(@PathVariable Long id, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        // Najdeme košík uživatele
        Order cart = orderRepository.findByUserAndStatus(loggedInUser, "PENDING")
                .orElse(null);

        if (cart != null) {
            cart.getItems().stream()
                    .filter(item -> item.getWatch().getId().equals(id))
                    .findFirst()
                    .ifPresent(item -> {
                        if (item.getQuantity() > 1) {
                            item.setQuantity(item.getQuantity() - 1);
                            orderItemRepository.save(item);
                        } else {
                            orderItemRepository.delete(item);
                        }
                    });
        }

        return "redirect:/order/cart";
    }

    @PostMapping("/cart/checkout")
    public String checkout(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login"; // Přesměrování na login
        }

        Order cart = orderRepository.findByUserAndStatus(loggedInUser, "PENDING")
                .orElse(null);

        if (cart == null || cart.getItems().isEmpty()) {
            return "redirect:/order/cart"; // Pokud je košík prázdný
        }

        cart.setStatus("CONFIRMED");
        orderRepository.save(cart); // Uložíme změnu stavu
        return "redirect:/orders"; // Přesměrování na stránku s historií objednávek
    }

}
