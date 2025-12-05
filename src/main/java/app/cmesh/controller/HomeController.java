package app.cmesh.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;
    private static final String REDIRECT = "redirect:";

    @GetMapping("/")
    public String home() {
        return REDIRECT + frontendUrl + "/";
    }

    @GetMapping("/login")
    public String login() {
        return REDIRECT + frontendUrl + "/login";
    }

    @GetMapping("/signup")
    public String signup() {
        return REDIRECT + frontendUrl + "/signup";
    }
}
