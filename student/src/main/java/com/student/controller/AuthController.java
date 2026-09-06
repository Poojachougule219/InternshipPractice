package com.student.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.student.dto.LoginRequest;
import com.student.entity.Student;
import com.student.repository.StudentRepository;
import com.student.security.JwtService;
import com.student.service.AuditLogService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // =====================================================
    // DEPENDENCIES
    // =====================================================

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private StudentRepository studentRepository;


    // =====================================================
    // API LOGIN
    // POST /api/auth/login
    // =====================================================

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {

        try {

            // =================================================
            // VALIDATE REQUEST
            // =================================================

            if (loginRequest == null
                    || loginRequest.getEmail() == null
                    || loginRequest.getEmail().trim().isEmpty()
                    || loginRequest.getPassword() == null
                    || loginRequest.getPassword().trim().isEmpty()) {

                Map<String, Object> response =
                        new HashMap<>();

                response.put(
                        "success",
                        false
                );

                response.put(
                        "message",
                        "Email and password are required"
                );

                return ResponseEntity
                        .badRequest()
                        .body(response);
            }


            // =================================================
            // EMAIL
            // =================================================

            String email =
                    loginRequest.getEmail().trim();


            // =================================================
            // PASSWORD
            // =================================================

            String password =
                    loginRequest.getPassword();


            // =================================================
            // AUTHENTICATE USER
            // =================================================

            Authentication authentication =
                    authenticationManager.authenticate(

                            new UsernamePasswordAuthenticationToken(
                                    email,
                                    password
                            )
                    );


            // =================================================
            // GET USERNAME
            // =================================================

            String username =
                    authentication.getName();


            // =================================================
            // GET ROLE
            // =================================================

            String role = null;

            for (GrantedAuthority authority :
                    authentication.getAuthorities()) {

                String authorityName =
                        authority.getAuthority();

                if ("ROLE_ADMIN".equals(authorityName)) {

                    role = "ROLE_ADMIN";
                    break;
                }

                if ("ROLE_STUDENT".equals(authorityName)) {

                    role = "ROLE_STUDENT";
                    break;
                }
            }


            // =================================================
            // FIND USER
            // =================================================

            Student student =
                    studentRepository
                            .findByEmailAndIsDeleted(
                                    username,
                                    "false"
                            )
                            .orElse(null);


            // =================================================
            // STUDENT ID
            // =================================================

            Long studentId = null;

            if (student != null) {

                studentId =
                        student.getId();
            }


            // =================================================
            // GET IP ADDRESS
            // =================================================

            String ipAddress =
                    request.getRemoteAddr();


            // Convert IPv6 localhost
            // to IPv4 localhost

            if ("0:0:0:0:0:0:0:1"
                    .equals(ipAddress)) {

                ipAddress =
                        "127.0.0.1";
            }


            // =================================================
            // LOGIN AUDIT MESSAGE
            // =================================================

            String loginMessage;

            if ("ROLE_ADMIN".equals(role)) {

                loginMessage =
                        "Admin logged in successfully";

            } else if ("ROLE_STUDENT".equals(role)) {

                loginMessage =
                        "Student logged in successfully";

            } else {

                loginMessage =
                        "User logged in successfully";
            }


            // =================================================
            // CREATE LOGIN AUDIT LOG
            // =================================================

            auditLogService.createLog(

                    // entity_id
                    studentId,

                    // username
                    username,

                    // role
                    role,

                    // action
                    "LOGIN",

                    // entity_name
                    "AUTH",

                    // student_id
                    studentId,

                    // description
                    loginMessage,

                    // ip_address
                    ipAddress
            );


            // =================================================
            // GENERATE JWT
            // =================================================

            String token =
                    jwtService.generateToken(username);


            // =================================================
            // RESPONSE
            // =================================================

            Map<String, Object> response =
                    new HashMap<>();

            response.put(
                    "success",
                    true
            );

            response.put(
                    "message",
                    "Login successful"
            );

            response.put(
                    "email",
                    username
            );

            response.put(
                    "role",
                    role
            );

            response.put(
                    "accessToken",
                    token
            );

            response.put(
                    "tokenType",
                    "Bearer"
            );


            return ResponseEntity.ok(response);


        } catch (BadCredentialsException e) {

            // =================================================
            // INVALID LOGIN
            // =================================================

            Map<String, Object> response =
                    new HashMap<>();

            response.put(
                    "success",
                    false
            );

            response.put(
                    "message",
                    "Invalid username or password"
            );

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(response);


        } catch (Exception e) {

            // =================================================
            // OTHER ERROR
            // =================================================

            e.printStackTrace();

            Map<String, Object> response =
                    new HashMap<>();

            response.put(
                    "success",
                    false
            );

            response.put(
                    "message",
                    "Login failed"
            );

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(response);
        }
    }


    // =====================================================
    // API LOGOUT
    // POST /api/auth/logout
    // =====================================================

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            Authentication authentication,
            HttpServletRequest request) {

        try {

            // =================================================
            // CHECK AUTHENTICATION
            // =================================================

            if (authentication == null
                    || !authentication.isAuthenticated()) {

                Map<String, Object> response =
                        new HashMap<>();

                response.put(
                        "success",
                        false
                );

                response.put(
                        "message",
                        "User is not authenticated"
                );

                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(response);
            }


            // =================================================
            // GET USERNAME
            // =================================================

            String username =
                    authentication.getName();


            // =================================================
            // GET ROLE
            // =================================================

            String role = null;

            for (GrantedAuthority authority :
                    authentication.getAuthorities()) {

                String authorityName =
                        authority.getAuthority();

                if ("ROLE_ADMIN".equals(authorityName)) {

                    role = "ROLE_ADMIN";
                    break;
                }

                if ("ROLE_STUDENT".equals(authorityName)) {

                    role = "ROLE_STUDENT";
                    break;
                }
            }


            // =================================================
            // FIND USER
            // =================================================

            Student student =
                    studentRepository
                            .findByEmailAndIsDeleted(
                                    username,
                                    "false"
                            )
                            .orElse(null);


            // =================================================
            // STUDENT ID
            // =================================================

            Long studentId = null;

            if (student != null) {

                studentId =
                        student.getId();
            }


            // =================================================
            // GET IP ADDRESS
            // =================================================

            String ipAddress =
                    request.getRemoteAddr();


            // Convert IPv6 localhost
            // to IPv4 localhost

            if ("0:0:0:0:0:0:0:1"
                    .equals(ipAddress)) {

                ipAddress =
                        "127.0.0.1";
            }


            // =================================================
            // LOGOUT MESSAGE
            // =================================================

            String logoutMessage;

            if ("ROLE_ADMIN".equals(role)) {

                logoutMessage =
                        "Admin logged out successfully";

            } else if ("ROLE_STUDENT".equals(role)) {

                logoutMessage =
                        "Student logged out successfully";

            } else {

                logoutMessage =
                        "User logged out successfully";
            }


            // =================================================
            // CREATE LOGOUT AUDIT LOG
            // =================================================

            auditLogService.createLog(

                    // entity_id
                    studentId,

                    // username
                    username,

                    // role
                    role,

                    // action
                    "LOGOUT",

                    // entity_name
                    "AUTH",

                    // student_id
                    studentId,

                    // description
                    logoutMessage,

                    // ip_address
                    ipAddress
            );


            // =================================================
            // RESPONSE
            // =================================================

            Map<String, Object> response =
                    new HashMap<>();

            response.put(
                    "success",
                    true
            );

            response.put(
                    "message",
                    logoutMessage
            );

            response.put(
                    "email",
                    username
            );

            response.put(
                    "role",
                    role
            );


            return ResponseEntity.ok(response);


        } catch (Exception e) {

            // =================================================
            // ERROR
            // =================================================

            e.printStackTrace();

            Map<String, Object> response =
                    new HashMap<>();

            response.put(
                    "success",
                    false
            );

            response.put(
                    "message",
                    "Logout failed"
            );

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(response);
        }
    }
}