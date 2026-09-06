package com.student.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;


    // =====================================================
    // SEND OTP EMAIL
    // =====================================================

    public void sendOtpEmail(
            String email,
            String otp) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email is required"
            );
        }

        if (otp == null || otp.isBlank()) {
            throw new IllegalArgumentException(
                    "OTP is required"
            );
        }


        SimpleMailMessage message =
                new SimpleMailMessage();


        // =================================================
        // TO
        // =================================================

        message.setTo(email);


        // =================================================
        // SUBJECT
        // =================================================

        message.setSubject(
                "Password Reset OTP"
        );


        // =================================================
        // MESSAGE
        // =================================================

        message.setText(
                "Hello,\n\n"
                + "Your OTP for password reset is: "
                + otp
                + "\n\n"
                + "This OTP is valid for 5 minutes."
                + "\n\n"
                + "If you did not request a password reset, "
                + "please ignore this email."
                + "\n\n"
                + "Regards,\n"
                + "Student Management System"
        );


        // =================================================
        // SEND EMAIL
        // =================================================

        mailSender.send(message);
    }
}