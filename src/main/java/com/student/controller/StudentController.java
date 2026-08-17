package com.student.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.student.entity.Student;
import com.student.service.StudentService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private StudentService studentService;

    // =====================================================
    // UPDATE PROFILE
    // =====================================================

    @PostMapping("/profile/update")
    public String updateProfile(
            @ModelAttribute Student student,

            @RequestParam(
                    value = "profilePhoto",
                    required = false
            )
            MultipartFile profilePhoto,

            Authentication authentication,

            HttpServletRequest request)
            throws IOException {

        // =================================================
        // GET LOGGED-IN USER EMAIL
        // =================================================

        String email = authentication.getName();

        // =================================================
        // GET CLIENT IP ADDRESS
        // =================================================

        String ipAddress = request.getRemoteAddr();

        // =================================================
        // DEBUG
        // =================================================

        System.out.println(
                "======================================"
        );

        System.out.println(
                "PROFILE UPDATE"
        );

        System.out.println(
                "EMAIL : " + email
        );

        System.out.println(
                "IP ADDRESS : " + ipAddress
        );

        System.out.println(
                "======================================"
        );

        // =================================================
        // UPDATE PROFILE
        // =================================================

        studentService.updateProfile(
                email,
                student,
                profilePhoto,
                ipAddress
        );

        // =================================================
        // REDIRECT TO PROFILE PAGE
        // =================================================

        return "redirect:/student/profile";
    }
}