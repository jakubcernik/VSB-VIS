package com.example.watchmanagement.controller;

import com.example.watchmanagement.model.Cart;
import com.example.watchmanagement.model.Watch;
import com.example.watchmanagement.repository.WatchRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final WatchRepository watchRepository;

    public CartController(WatchRepository watchRepository) {
        this.watchRepository = watchRepository;
    }

    @GetMapping
    public String showCart(HttpSession session, Model model) {
        Cart cart = getCart(session);
        model.addAttribute("items", cart.getItems());
        model.addAttribute("totalPrice", cart.getTotalPrice());
        return "cart";
    }

    @GetMapping("/add/{id}")
    public String addToCart(@PathVariable Long id, HttpSession session) {
        Watch watch = watchRepository.findById(id).orElse(null);
        if (watch != null) {
            Cart cart = getCart(session);
            cart.addItem(watch);
            session.setAttribute("cart", cart);
        }
        return "redirect:/";
    }

    @GetMapping("/remove/{id}")
    public String removeFromCart(@PathVariable Long id, HttpSession session) {
        Watch watch = watchRepository.findById(id).orElse(null);
        if (watch != null) {
            Cart cart = getCart(session);
            cart.removeItem(watch);
            session.setAttribute("cart", cart);
        }
        return "redirect:/cart";
    }

    private Cart getCart(HttpSession session) {
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null) {
            cart = new Cart();
            session.setAttribute("cart", cart);
        }
        return cart;
    }
}
