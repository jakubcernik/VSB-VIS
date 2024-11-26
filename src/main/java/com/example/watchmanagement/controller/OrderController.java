package com.example.watchmanagement.controller;

import com.example.watchmanagement.model.Order;
import com.example.watchmanagement.model.OrderItem;
import com.example.watchmanagement.model.User;
import com.example.watchmanagement.model.Watch;
import com.example.watchmanagement.repository.OrderRepository;
import com.example.watchmanagement.repository.OrderItemRepository;
import com.example.watchmanagement.repository.WatchRepository;
import jakarta.annotation.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    private void updateTotalPrice(Order order) {
        double total = order.getItems().stream()
                .mapToDouble(item -> item.getQuantity() * item.getWatch().getPrice())
                .sum();
        order.setTotalPrice(total);
        orderRepository.save(order);
    }


    @GetMapping("/cart/add/{id}")
    public String addToCart(@PathVariable Long id, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Watch watch = watchRepository.findById(id).orElse(null);
        if (watch == null || watch.getStock() <= 0) { // Kontrola, zda je skladem
            return "redirect:/order/cart";
        }

        Order cart = orderRepository.findByUserAndStatus(loggedInUser, "PENDING")
                .orElseGet(() -> {
                    Order newCart = new Order();
                    newCart.setUser(loggedInUser);
                    newCart.setStatus("PENDING");
                    return orderRepository.save(newCart);
                });

        Optional<OrderItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getWatch().getId().equals(id))
                .findFirst();

        if (existingItem.isPresent()) {
            OrderItem item = existingItem.get();
            if (item.getQuantity() < watch.getStock()) { // Ověření, že nepřekročí sklad
                item.setQuantity(item.getQuantity() + 1);
                orderItemRepository.save(item);
            }
        } else {
            OrderItem newItem = new OrderItem();
            newItem.setOrder(cart);
            newItem.setWatch(watch);
            newItem.setQuantity(1);
            orderItemRepository.save(newItem);
        }
        updateTotalPrice(cart);
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
            Optional<OrderItem> itemToRemove = cart.getItems().stream()
                    .filter(item -> item.getWatch().getId().equals(id))
                    .findFirst();

            if (itemToRemove.isPresent()) {
                OrderItem item = itemToRemove.get();
                if (item.getQuantity() > 1) {
                    // Snížení počtu kusů
                    item.setQuantity(item.getQuantity() - 1);
                    orderItemRepository.save(item);
                } else {
                    // Odstranění položky, pokud je množství 1
                    orderItemRepository.delete(item);
                    cart.getItems().remove(item); // Aktualizace kolekce
                    orderRepository.save(cart); // Uložíme změnu košíku
                }
            }
        }
        updateTotalPrice(cart);
        return "redirect:/order/cart";
    }

    @GetMapping("/cart/delete/{id}")
    public String deleteFromCart(@PathVariable Long id, HttpSession session) {
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
                        orderItemRepository.delete(item); // Odstraníme položku z databáze
                        cart.getItems().remove(item);    // Odstraníme položku z kolekce
                        orderRepository.save(cart);     // Uložíme změnu košíku
                    });
        }
        updateTotalPrice(cart);
        return "redirect:/order/cart";
    }

    @GetMapping("/cart/checkout")
    public String showCheckoutForm(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        // Najdeme košík uživatele (stav PENDING)
        Order cart = orderRepository.findByUserAndStatus(loggedInUser, "PENDING")
                .orElseGet(() -> {
                    // Pokud není nalezen, vrátíme null nebo nový objekt
                    Order newCart = new Order();
                    newCart.setUser(loggedInUser);
                    newCart.setStatus("PENDING");
                    return newCart; // Tento košík není uložen do databáze
                });

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            return "redirect:/order/cart"; // Přesměrování, pokud je košík prázdný
        }

        model.addAttribute("order", cart); // Přidání do modelu
        return "checkout"; // Odkaz na šablonu
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

        Order cart = orderRepository.findById(order.getId())
                .orElse(null);

        if (cart == null || cart.getItems().isEmpty()) {
            return "redirect:/order/cart";
        }

        // Aktualizace údajů objednávky
        cart.setCustomerName(order.getCustomerName());
        cart.setCustomerAddress(order.getCustomerAddress());
        cart.setPaymentMethod(order.getPaymentMethod());
        cart.setStatus("CREATED");
        orderRepository.save(cart);

        // Generování faktury
        try {
            generateInvoiceFile(cart);
        } catch (IOException e) {
            e.printStackTrace();
            // Můžete přidat logiku pro zobrazení chyby uživateli
        }

        return "redirect:/order/orders";
    }


    @GetMapping("/orders")
    public String showOrders(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        List<Order> orders = orderRepository.findByUserAndStatusNot(loggedInUser, "PENDING");
        model.addAttribute("orders", orders);

        return "orders"; // Vrací šablonu `orders.html`
    }

    @GetMapping("/invoice/{id}")
    @ResponseBody
    public String getInvoice(@PathVariable Long id) {
        Order order = orderRepository.findById(id)
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
        // Cesta k uložení souborů
        Path invoiceDir = Paths.get("invoices");
        if (!Files.exists(invoiceDir)) {
            Files.createDirectories(invoiceDir);
        }

        // Název souboru
        Path invoiceFile = invoiceDir.resolve("invoice_" + order.getId() + ".txt");

        // Obsah faktury
        String invoiceContent = generateInvoice(order);

        // Zápis do souboru
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
