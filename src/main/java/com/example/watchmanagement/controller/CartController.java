package com.example.watchmanagement.controller;

import com.example.watchmanagement.model.Watch;
import com.example.watchmanagement.repository.WatchRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final WatchRepository watchRepository;

    public CartController(WatchRepository watchRepository) {
        this.watchRepository = watchRepository;
    }

    @GetMapping
    public String showCart(Model model, HttpSession session) {
        Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttribute("cart");
        if (cart == null) {
            cart = new HashMap<>();
            session.setAttribute("cart", cart);
        }
        model.addAttribute("cart", cart);

        Map<Long, Watch> watchDetails = new HashMap<>();
        cart.forEach((id, quantity) -> {
            Watch watch = watchRepository.findById(id).orElse(null);
            if (watch != null) {
                watchDetails.put(id, watch);
            }
        });
        model.addAttribute("watchDetails", watchDetails);

        return "cart";
    }

    @GetMapping("/add/{id}")
    public String addToCart(@PathVariable Long id, HttpSession session) {
        Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttribute("cart");
        if (cart == null) {
            cart = new HashMap<>();
            session.setAttribute("cart", cart);
        }
        cart.put(id, cart.getOrDefault(id, 0) + 1); // Zvýšíme počet kusů o 1
        return "redirect:/cart";
    }

    @GetMapping("/remove/{id}")
    public String removeFromCart(@PathVariable Long id, HttpSession session) {
        Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttribute("cart");
        if (cart != null && cart.containsKey(id)) {
            int quantity = cart.get(id);
            if (quantity > 1) {
                cart.put(id, quantity - 1); // Snížíme počet kusů o 1
            } else {
                cart.remove(id); // Odstraníme produkt z košíku
            }
        }
        return "redirect:/cart";
    }

    @GetMapping("/delete/{id}")
    public String deleteFromCart(@PathVariable Long id, HttpSession session) {
        Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttribute("cart");
        if (cart != null) {
            cart.remove(id); // Odstraníme produkt z košíku bez ohledu na množství
        }
        return "redirect:/cart";
    }
}
