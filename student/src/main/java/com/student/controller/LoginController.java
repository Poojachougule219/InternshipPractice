package com.student.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.student.entity.Student;
import com.student.exception.EmailAlreadyExistsException;
import com.student.service.StudentService;

import jakarta.servlet.http.HttpSession;

@Controller

public class LoginController {

    @Autowired
    private StudentService service;

    // Login Page
    @GetMapping("/")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }
   
    
    // Login
    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        Model model,
                        HttpSession session) {

        Student student = service.login(email, password);

        if (student != null) {

            // Save student in session
            session.setAttribute("student", student);

            return "redirect:/dashboard";
        }

        model.addAttribute("message", "Invalid Email or Password");
        return "login";
    }
    

    //  register page
    @GetMapping("/register")
    public String registerPage() {
      return "register";
    }
    
    @PostMapping("/register")
    public String register(Student student,
                           RedirectAttributes redirectAttributes,
                           Model model) {

        try {

            service.signup(student);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Student registered successfully!");

            return "redirect:/login";

        } catch (EmailAlreadyExistsException e) {

            model.addAttribute("errorMessage", e.getMessage());

            return "register";
        }
    }
    
    
    
    // Dashboard
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {

        Student student = (Student) session.getAttribute("student");

        if (student == null) {
            return "redirect:/";
        }

        model.addAttribute("student", student);

        model.addAttribute("totalStudents", service.getAllStudents().size());
        model.addAttribute("activeRecords", service.getAllStudents().size());
        model.addAttribute("recentRecords", service.getAllStudents().size());

        return "dashboard";
    }

    
    // Logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {

        session.invalidate();

        return "redirect:/";
    }
    
    
//    sign up page
    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }
    
    @PostMapping("/signup")
    public String signup(Student student, RedirectAttributes redirectAttributes) {
        service.signup(student);
        redirectAttributes.addFlashAttribute("successMessage",
                "Student registered successfully!");
        return "redirect:/login";
    }

}