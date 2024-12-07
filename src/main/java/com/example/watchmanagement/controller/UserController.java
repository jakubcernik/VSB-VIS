package com.example.watchmanagement.controller;

import com.example.watchmanagement.model.User;
import com.example.watchmanagement.repository.UserRepository;
import com.example.watchmanagement.repository.WatchRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

@Controller
public class UserController {

    private final UserRepository userRepository;
    private final WatchRepository watchRepository;

    public UserController(UserRepository userRepository, WatchRepository watchRepository) {
        this.userRepository = userRepository;
        this.watchRepository = watchRepository;
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

        System.out.println("Uživatel je: " + user);
        user.setRole("USER");
        userRepository.save(user);
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
        User user = userRepository.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            session.setAttribute("loggedInUser", user);
            return "redirect:/home";
        } else {
            model.addAttribute("error", "Neplatné uživatelské jméno nebo heslo.");
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
        model.addAttribute("watches", watchRepository.findAll());
        return "home";
    }

    @GetMapping("/logout")
    public String logoutUser(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }
}
