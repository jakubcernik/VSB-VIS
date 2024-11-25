package com.example.watchmanagement.controller;

import com.example.watchmanagement.model.User;
import com.example.watchmanagement.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

@Controller
public class UserController {

    private final UserRepository userRepository;

    // Konstruktor bez passwordEncoder
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        User user = new User();
        user.setRole("USER"); // Nastav výchozí hodnotu role
        model.addAttribute("user", user);
        return "register";
    }


    @PostMapping("/register")
    public String registerUser(@ModelAttribute @Valid User user, BindingResult result, Model model) {
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
    public String showLoginForm(Model model) {
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String username,
                            @RequestParam String password,
                            Model model,
                            HttpSession session) {
        User user = userRepository.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) { // Bez šifrování
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
            // Přidej další atributy, pokud je třeba
            return "home"; // Vrátí šablonu `home.html`
        } else {
            return "redirect:/login"; // Přesměrování na login, pokud uživatel není přihlášen
        }
    }


    @GetMapping("/logout")
    public String logoutUser(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }
}
