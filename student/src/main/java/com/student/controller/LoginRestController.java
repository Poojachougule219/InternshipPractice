
	
	package com.student.controller;

	import org.springframework.beans.factory.annotation.Autowired;
	
	import org.springframework.ui.Model;
	import org.springframework.web.bind.annotation.GetMapping;
	import org.springframework.web.bind.annotation.PostMapping;
	import org.springframework.web.bind.annotation.RequestMapping;
	import org.springframework.web.bind.annotation.RequestParam;
	import org.springframework.web.bind.annotation.RestController;

	import com.student.entity.Student;
	import com.student.service.StudentService;

	import jakarta.servlet.http.HttpSession;

	@RestController
	@RequestMapping("/api")
	public class LoginRestController {

	    @Autowired
	    private StudentService service;

	    // Login Page
	    @GetMapping("/")
	    public String loginPage() {
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

	    // Dashboard
	    @GetMapping("/dashboard")
	    public String dashboard(HttpSession session, Model model) {

	        Student student = (Student) session.getAttribute("student");

	        if (student == null) {
	            return "redirect:/";
	        }

	        model.addAttribute("student", student);

	        return "dashboard";
	    }

	    // Logout
	    @GetMapping("/logout")
	    public String logout(HttpSession session) {

	        session.invalidate();

	        return "redirect:/";
	    }

	}


