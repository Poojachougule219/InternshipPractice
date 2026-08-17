package com.student.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.student.entity.Student;
import com.student.exception.EmailAlreadyExistsException;
import com.student.repository.RoleRepository;
import com.student.service.StudentService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;

@Controller
public class LoginController {

    @Autowired
    private StudentService service;

    @Autowired
    private RoleRepository roleRepository;


    // =====================================================
    // HOME
    // =====================================================

    @GetMapping("/")
    public String home() {

        return "login";
    }


    // =====================================================
    // LOGIN PAGE
    // =====================================================

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(
                    value = "error",
                    required = false
            )
            String error,
            Model model) {

        if (error != null) {

            model.addAttribute(
                    "message",
                    "Invalid email or password"
            );
        }

        return "login";
    }


    // =====================================================
    // REGISTER PAGE
    // =====================================================

    @GetMapping("/register")
    public String registerPage(
            Model model) {

        model.addAttribute(
                "student",
                new Student()
        );

        model.addAttribute(
                "roles",
                roleRepository.findAll()
        );

        return "register";
    }


    // =====================================================
    // REGISTER STUDENT / ADMIN
    // =====================================================

    @PostMapping("/register")
    public String register(
            @ModelAttribute Student student,
            @RequestParam("roleId") String roleId,
            @RequestParam(value = "profilePhoto", required = false) MultipartFile profilePhoto,
            HttpServletRequest request,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {

            String ipAddress =
                    request.getRemoteAddr();

            service.signup(
                    student,
                    roleId,
                    profilePhoto,
                    ipAddress,
                    authentication
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Registration successful"
            );

            return "redirect:/admin/users";

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return "redirect:/admin/register";
        }
    }
    }