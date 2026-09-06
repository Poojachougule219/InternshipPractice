package com.student.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.student.entity.PasswordResetToken;
import com.student.entity.Student;
import com.student.enums.StudentStatus;
import com.student.repository.PasswordResetTokenRepository;
import com.student.repository.StudentRepository;

@Service
public class PasswordResetService {


    @Autowired
    private PasswordResetTokenRepository tokenRepository;


    @Autowired
    private StudentRepository studentRepository;


    @Autowired
    private EmailService emailService;


    @Autowired
    private PasswordEncoder passwordEncoder;



    // Generate OTP and send email
    public void generateOtp(String email) {


        Student student = studentRepository
                .findByEmailAndStatusAndIsDeleted(
                        email,
                        StudentStatus.ACTIVE,
                        "false"
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Email not registered"));



        String otp = String.valueOf(
                100000 + new Random().nextInt(900000)
        );



        PasswordResetToken token =
                tokenRepository
                .findByEmail(email)
                .orElse(new PasswordResetToken());



        token.setEmail(student.getEmail());

        token.setOtp(otp);

        token.setExpiryTime(
                LocalDateTime.now()
                .plusMinutes(5)
        );



        tokenRepository.save(token);



        emailService.sendOtpEmail(
                student.getEmail(),
                otp
        );
    }






    // Verify OTP
    public boolean verifyOtp(
            String email,
            String otp) {


        PasswordResetToken token =
                tokenRepository
                .findByEmail(email)
                .orElse(null);



        if(token == null) {
            return false;
        }



        if(token.getExpiryTime()
                .isBefore(LocalDateTime.now())) {

            return false;
        }



        return token.getOtp()
                .equals(otp);
    }








    // Reset Password
    @Transactional
    public boolean resetPassword(
            String email,
            String newPassword) {



        Student student =
                studentRepository
                .findByEmailAndStatusAndIsDeleted(
                        email,
                        StudentStatus.ACTIVE,
                        "false"
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Student not found"));



        student.setPassword(
                passwordEncoder.encode(
                        newPassword)
        );



        studentRepository.save(student);



        tokenRepository.deleteByEmail(email);



        return true;
    }

}