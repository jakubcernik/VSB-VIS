package com.example.watchmanagement.service;

import com.example.watchmanagement.model.User;
import com.example.watchmanagement.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Registruje nového uživatele, nastaví mu roli a uloží do DB.
     */
    public User registerUser(User user) {
        // Přesunuto z controlleru
        user.setRole("USER");
        userRepository.save(user);
        return user;
    }

    /**
     * Pokusí se přihlásit uživatele na základě username a password.
     * Vrátí Optional s uživatelem, pokud se podaří ověřit přihlašovací údaje.
     */
    public Optional<User> loginUser(String username, String password) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isPresent() && optionalUser.get().getPassword().equals(password)) {
            User loggedInUser = optionalUser.get();
            if (loggedInUser.getId() == null) {
                System.err.println("Login Error: User ID is null for user: " + loggedInUser);
                throw new IllegalStateException("Logged-in user must have a valid ID.");
            }
            System.out.println("Logged-in user: " + loggedInUser);
            return Optional.of(loggedInUser);
        } else {
            return Optional.empty();
        }
    }

    // Další pomocné metody, pokud bys je potřeboval
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}
