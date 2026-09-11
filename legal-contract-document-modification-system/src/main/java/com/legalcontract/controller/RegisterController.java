package com.legalcontract.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.legalcontract.dto.RegisterRequest;
import com.legalcontract.service.UserService;

@Controller
public class RegisterController {

    private final UserService userService;

    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model) {

        model.addAttribute("user", new RegisterRequest());
        model.addAttribute("roles", userService.getAllRoles());

        return "Common/register";
    }

    @PostMapping("/register")
    public String registerUser(
            @ModelAttribute("user") RegisterRequest request,
            Model model) {

        try {

            if (request.getPassword() == null
                    || request.getConfirmPassword() == null
                    || !request.getPassword()
                            .equals(request.getConfirmPassword())) {

                model.addAttribute(
                        "error",
                        "Password and Confirm Password do not match.");

                model.addAttribute(
                        "roles",
                        userService.getAllRoles());

                return "Common/register";
            }

            if (request.getRole() == null
                    || request.getRole().trim().isEmpty()) {

                model.addAttribute(
                        "error",
                        "Please select a role.");

                model.addAttribute(
                        "roles",
                        userService.getAllRoles());

                return "Common/register";
            }

            userService.registerUser(
                    request.getFullName(),
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getRole()
            );

            return "redirect:/login?registered=true";

        } catch (RuntimeException e) {

            model.addAttribute(
                    "error",
                    e.getMessage());

            model.addAttribute(
                    "roles",
                    userService.getAllRoles());

            return "Common/register";

        } catch (Exception e) {

            model.addAttribute(
                    "error",
                    "Registration failed. Please try again.");

            model.addAttribute(
                    "roles",
                    userService.getAllRoles());

            return "Common/register";
        }
    }
}