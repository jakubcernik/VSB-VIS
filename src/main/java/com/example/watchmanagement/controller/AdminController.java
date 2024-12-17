package com.example.watchmanagement.controller;

import com.example.watchmanagement.model.Order;
import com.example.watchmanagement.model.OrderItem;
import com.example.watchmanagement.model.User;
import com.example.watchmanagement.model.Watch;
import com.example.watchmanagement.service.OrderService;
import com.example.watchmanagement.service.WatchService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final WatchService watchService;
    private final OrderService orderService;

    public AdminController(WatchService watchService, OrderService orderService) {
        this.watchService = watchService;
        this.orderService = orderService;
    }

    @GetMapping("/watches")
    public String listWatches(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
            return "redirect:/login";
        }
        model.addAttribute("watches", watchService.findAll());
        return "admin/watches";
    }

    @GetMapping("/watches/new")
    public String showAddWatchForm(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
            return "redirect:/login";
        }
        model.addAttribute("watch", new Watch());
        return "admin/add-watch";
    }

    @PostMapping("/watches")
    public String addWatch(@ModelAttribute Watch watch, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
            return "redirect:/login";
        }
        watchService.save(watch);
        return "redirect:/admin/watches";
    }

    @GetMapping("/watches/edit/{id}")
    public String showEditWatchForm(@PathVariable Long id, Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
            return "redirect:/login";
        }
        Watch watch = watchService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid watch Id:" + id));
        model.addAttribute("watch", watch);
        return "admin/edit-watch";
    }

    @PostMapping("/watches/update/{id}")
    public String updateWatch(@PathVariable Long id, @ModelAttribute Watch watch, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
            return "redirect:/login";
        }
        watch.setId(id);
        watchService.save(watch);
        return "redirect:/admin/watches";
    }

    @GetMapping("/watches/delete/{id}")
    public String deleteWatch(@PathVariable Long id, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
            return "redirect:/login";
        }
        watchService.deleteById(id);
        return "redirect:/admin/watches";
    }

    @GetMapping("/orders-to-confirm")
    public String showOrdersToConfirm(Model model) {
        // Všechny objednávky ve stavu CREATED
        List<Order> orders = orderService.findByStatus("CREATED");
        model.addAttribute("orders", orders);
        return "/admin/orders-to-confirm";
    }

    @GetMapping("/orders/approve/{id}")
    public String approveOrder(@PathVariable Long id) {
        Order order = orderService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid order ID"));
        order.setStatus("COMPLETED");
        orderService.save(order);
        return "redirect:/admin/orders-to-confirm";
    }

    @GetMapping("/orders/reject/{id}")
    public String rejectOrder(@PathVariable Long id) {
        Order order = orderService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid order ID"));

        // Pro každou položku objednávky snížíme stock
        for (OrderItem item : order.getItems()) {
            Watch watch = item.getWatch();
            // Použijeme watchService.updateStock místo přímého volání repository
            watchService.updateStock(watch.getId(), watch.getStock() - item.getQuantity());
        }

        order.setStatus("CANCELED");
        orderService.save(order);
        return "redirect:/admin/orders-to-confirm";
    }

}
