package com.example.watchmanagement.controller;

import com.example.watchmanagement.repository.WatchRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final WatchRepository watchRepository;

    public HomeController(WatchRepository watchRepository) {
        this.watchRepository = watchRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("watches", watchRepository.findAll());
        return "home";
    }
}
