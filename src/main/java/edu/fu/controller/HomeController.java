package edu.fu.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves the public landing page at the root URL "/".
 * No authentication required (permitted in SecurityConfig).
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String landingPage() {
        return "index";
    }
}
