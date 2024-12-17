package com.example.watchmanagement.controller;

import com.example.watchmanagement.model.User;
import com.example.watchmanagement.service.UserService;
import com.example.watchmanagement.service.WatchService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

import java.util.Optional;

@Controller
public class UserController {

    private final UserService userService;
    private final WatchService watchService;

    // Přidáme WatchService do konstruktoru
    public UserController(UserService userService, WatchService watchService) {
        this.userService = userService;
        this.watchService = watchService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        User user = new User();
        user.setRole("USER");
        model.addAttribute("user", user);
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute @Valid User user, BindingResult result) {
        if (result.hasErrors()) {
            System.out.println("Validační chyby: " + result.getAllErrors());
            return "register";
        }
        userService.registerUser(user);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String username,
                            @RequestParam String password,
                            Model model,
                            HttpSession session) {
        Optional<User> optionalUser = userService.loginUser(username, password);

        if (optionalUser.isPresent()) {
            User loggedInUser = optionalUser.get();
            session.setAttribute("loggedInUser", loggedInUser);
            return "redirect:/home";
        } else {
            model.addAttribute("error", "Invalid username or password.");
            return "login";
        }
    }

    @GetMapping("/home")
    public String showHomePage(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser != null) {
            model.addAttribute("username", loggedInUser.getUsername());
            model.addAttribute("role", loggedInUser.getRole());
        } else {
            model.addAttribute("username", "guest");
            model.addAttribute("role", "USER");
        }

        // Místo watchRepository.findAll() voláme watchService.findAll()
        model.addAttribute("watches", watchService.findAll());

        return "home";
    }

    @GetMapping("/logout")
    public String logoutUser(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }
}
