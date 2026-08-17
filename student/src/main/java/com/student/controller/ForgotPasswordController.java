package com.student.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.student.service.PasswordResetService;


@Controller
public class ForgotPasswordController {


    @Autowired
    private PasswordResetService passwordResetService;



    // Open forgot password page
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {

        return "Student/forgot_password";
    }



    // Send OTP
    @PostMapping("/forgot-password")
    public String sendOtp(
            @RequestParam String email,
            Model model) {

        try {

            passwordResetService.generateOtp(email);

            model.addAttribute("email", email);

            model.addAttribute(
                    "success",
                    "OTP sent to your registered email id"
            );

            return "Student/verify_otp";


        } catch(Exception e) {

            model.addAttribute(
                    "error",
                    e.getMessage()
            );

            return "Student/forgot_password";
        }
    }


    
    // Verify OTP page
    @GetMapping("/verify-otp")
    public String verifyOtpPage(
            @RequestParam("email") String email,
            Model model) {


        model.addAttribute("email", email);


        return "Student/verify_otp";

    }





    // Verify OTP
    @PostMapping("/verify-otp")
    public String verifyOtp(
            @RequestParam String email,
            @RequestParam String otp,
            Model model) {


        boolean result =
                passwordResetService.verifyOtp(email, otp);


        if(result) {

            model.addAttribute(
                    "email",
                    email
            );


            model.addAttribute(
                    "success",
                    "OTP verified successfully. Please reset your password."
            );


            return "Student/reset_password";

        }


        model.addAttribute(
                "email",
                email
        );


        model.addAttribute(
                "error",
                "Invalid or expired OTP"
        );


        return "Student/verify_otp";
    }
    
    
    
//    reset password
    @GetMapping("/reset-password")
    public String resetPasswordPage(
            @RequestParam String email,
            Model model) {

        model.addAttribute("email", email);

        return "Student/reset_password";
    }



    @PostMapping("/reset-password")
    public String resetPassword(
            @RequestParam String email,
            @RequestParam String password,
            Model model) {


        boolean result =
                passwordResetService.resetPassword(
                        email,
                        password
                );


        if(result) {

            return "redirect:/login?success=Password+updated+successfully.+Please+login.";

        }


        model.addAttribute(
                "error",
                "Password update failed"
        );


        return "Student/reset_password";
    }
}