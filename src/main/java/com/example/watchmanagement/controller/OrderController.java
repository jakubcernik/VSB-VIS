package com.example.watchmanagement.controller;

import com.example.watchmanagement.model.Order;
import com.example.watchmanagement.model.OrderItem;
import com.example.watchmanagement.model.User;
import com.example.watchmanagement.model.Watch;
import com.example.watchmanagement.service.OrderService;
import com.example.watchmanagement.service.OrderItemService;
import com.example.watchmanagement.service.WatchService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final WatchService watchService;

    public OrderController(OrderService orderService, OrderItemService orderItemService, WatchService watchService) {
        this.orderService = orderService;
        this.orderItemService = orderItemService;
        this.watchService = watchService;
    }

    @GetMapping("/cart")
    public String showCart(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        } else {
            model.addAttribute("username", loggedInUser.getUsername());
        }

        Order cart = orderService.findByUserAndStatus(loggedInUser, "PENDING")
                .orElseGet(() -> {
                    Order newCart = new Order();
                    newCart.setUser(loggedInUser);
                    newCart.setStatus("PENDING");
                    return orderService.save(newCart);
                });

        model.addAttribute("cart", cart);
        return "cart";
    }

    @GetMapping("/cart/add/{id}")
    public String addToCart(@PathVariable Long id, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Watch watch = watchService.findById(id).orElse(null);
        if (watch == null || watch.getStock() <= 0) {
            return "redirect:/order/cart";
        }

        Order cart = orderService.findByUserAndStatus(loggedInUser, "PENDING")
                .orElseGet(() -> {
                    Order newCart = new Order();
                    newCart.setUser(loggedInUser);
                    newCart.setStatus("PENDING");
                    return orderService.save(newCart);
                });

        Optional<OrderItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getWatch().getId().equals(id))
                .findFirst();

        if (existingItem.isPresent()) {
            OrderItem item = existingItem.get();
            if (item.getQuantity() < watch.getStock()) {
                item.setQuantity(item.getQuantity() + 1);
                item.setOrder(cart);
                orderItemService.save(item);
            } else {
                System.err.println("Not enough stock for product: " + watch.getName());
            }
        } else {
            OrderItem newItem = new OrderItem();
            newItem.setOrder(cart);
            newItem.setWatch(watch);
            newItem.setQuantity(1);
            orderItemService.save(newItem);
        }

        orderService.updateTotalPrice(cart);
        return "redirect:/order/cart";
    }

    @GetMapping("/cart/remove/{id}")
    public String removeFromCart(@PathVariable Long id, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Order cart = orderService.findByUserAndStatus(loggedInUser, "PENDING").orElse(null);

        if (cart != null) {
            Optional<OrderItem> itemToRemove = cart.getItems().stream()
                    .filter(item -> item.getWatch().getId().equals(id))
                    .findFirst();

            if (itemToRemove.isPresent()) {
                OrderItem item = itemToRemove.get();
                if (item.getQuantity() > 1) {
                    item.setQuantity(item.getQuantity() - 1);
                    item.setOrder(cart);
                    orderItemService.save(item);
                } else {
                    orderItemService.delete(item);
                    cart.getItems().remove(item);
                    orderService.save(cart);
                }
            }
        }
        orderService.updateTotalPrice(cart);
        return "redirect:/order/cart";
    }

    @GetMapping("/cart/delete/{id}")
    public String deleteFromCart(@PathVariable Long id, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Order cart = orderService.findByUserAndStatus(loggedInUser, "PENDING").orElse(null);

        if (cart != null) {
            cart.getItems().stream()
                    .filter(item -> item.getWatch().getId().equals(id))
                    .findFirst()
                    .ifPresent(item -> {
                        orderItemService.delete(item);
                        cart.getItems().remove(item);
                        orderService.save(cart);
                    });
        }
        orderService.updateTotalPrice(cart);
        return "redirect:/order/cart";
    }

    @GetMapping("/cart/checkout")
    public String showCheckoutForm(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Order cart = orderService.findByUserAndStatus(loggedInUser, "PENDING")
                .orElseGet(() -> {
                    Order newCart = new Order();
                    newCart.setUser(loggedInUser);
                    newCart.setStatus("PENDING");
                    return newCart;
                });

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            return "redirect:/order/cart";
        }
        model.addAttribute("order", cart);
        return "checkout";
    }

    @PostMapping("/cart/checkout")
    public String processCheckout(@ModelAttribute("order") Order order, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        if (order.getId() == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }

        Order cart = orderService.findById(order.getId()).orElse(null);
        if (cart == null || cart.getItems().isEmpty()) {
            return "redirect:/order/cart";
        }

        cart.setCustomerName(order.getCustomerName());
        cart.setCustomerAddress(order.getCustomerAddress());
        cart.setPaymentMethod(order.getPaymentMethod());
        cart.setStatus("CREATED");

        for (OrderItem item : cart.getItems()) {
            Watch watch = item.getWatch();
            int newStock = watch.getStock() - item.getQuantity();
            if (newStock < 0) {
                throw new IllegalStateException("Nedostatečné zásoby pro produkt: " + watch.getName());
            }
            watchService.updateStock(watch.getId(), newStock);
        }

        orderService.updateTotalPrice(cart);
        orderService.save(cart);

        try {
            generateInvoiceFile(cart);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "redirect:/order/orders";
    }

    @GetMapping("/orders")
    public String showOrders(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        } else {
            model.addAttribute("username", loggedInUser.getUsername());
        }

        List<Order> orders = orderService.findByUserAndStatusNot(loggedInUser, "PENDING");
        model.addAttribute("orders", orders);

        return "orders";
    }

    @GetMapping("/invoice/{id}")
    @ResponseBody
    public String getInvoice(@PathVariable Long id) {
        Order order = orderService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid order ID"));

        StringBuilder invoiceContent = new StringBuilder();
        invoiceContent.append("Faktura č. ").append(order.getId()).append("\n");
        invoiceContent.append("Jméno: ").append(order.getCustomerName()).append("\n");
        invoiceContent.append("Adresa: ").append(order.getCustomerAddress()).append("\n");
        invoiceContent.append("Platební metoda: ").append(order.getPaymentMethod()).append("\n");
        invoiceContent.append("Položky:\n");

        for (OrderItem item : order.getItems()) {
            invoiceContent.append("- ").append(item.getWatch().getName())
                    .append(", ks: ").append(item.getQuantity())
                    .append(", cena: ").append(item.getQuantity() * item.getWatch().getPrice())
                    .append(" Kč\n");
        }

        invoiceContent.append("Celková cena: ").append(order.getTotalPrice()).append(" Kč\n");
        return invoiceContent.toString();
    }

    private void generateInvoiceFile(Order order) throws IOException {
        Path invoiceDir = Paths.get("invoices");
        if (!Files.exists(invoiceDir)) {
            Files.createDirectories(invoiceDir);
        }

        Path invoiceFile = invoiceDir.resolve("invoice_" + order.getId() + ".txt");

        String invoiceContent = generateInvoice(order);

        Files.writeString(invoiceFile, invoiceContent);
    }

    private String generateInvoice(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("Faktura číslo: ").append(order.getId()).append("\n");
        sb.append("Jméno: ").append(order.getCustomerName()).append("\n");
        sb.append("Adresa: ").append(order.getCustomerAddress()).append("\n");
        sb.append("Platební metoda: ").append(order.getPaymentMethod()).append("\n");
        sb.append("Stav: ").append(order.getStatus()).append("\n");
        sb.append("Celková cena: ").append(order.getTotalPrice()).append("\n\n");
        sb.append("Položky objednávky:\n");
        for (OrderItem item : order.getItems()) {
            sb.append("- ").append(item.getWatch().getName())
                    .append(", ks: ").append(item.getQuantity())
                    .append(", cena za kus: ").append(item.getWatch().getPrice())
                    .append("\n");
        }
        return sb.toString();
    }
}
