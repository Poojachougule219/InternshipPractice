package com.legalcontract.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            return "redirect:/login";
        }

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        "ROLE_ADMIN".equals(
                                authority.getAuthority()
                        ));

        if (isAdmin) {
            return "redirect:/admin/dashboard";
        }

        return "redirect:/user/dashboard";
    }
}