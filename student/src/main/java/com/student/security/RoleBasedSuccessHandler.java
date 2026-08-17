package com.student.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.student.entity.Student;
import com.student.repository.StudentRepository;
import com.student.service.AuditLogService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RoleBasedSuccessHandler
        implements AuthenticationSuccessHandler {

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private StudentRepository studentRepository;

    // =====================================================
    // JWT SERVICE
    // =====================================================

    @Autowired
    private JwtService jwtService;


    // =====================================================
    // AUTHENTICATION SUCCESS
    // =====================================================

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {


        // =====================================================
        // USERNAME
        // =====================================================

        String username =
                authentication.getName();


        // =====================================================
        // FIND ROLE
        // =====================================================

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


        // =====================================================
        // FIND STUDENT / ADMIN RECORD
        // =====================================================

        Student student =
                studentRepository
                        .findByEmailAndIsDeleted(
                                username,
                                "false"
                        )
                        .orElse(null);

        Long studentId = null;

        if (student != null) {

            studentId =
                    student.getId();
        }


        // =====================================================
        // IP ADDRESS
        // =====================================================

        String ipAddress =
                request.getRemoteAddr();


        // =====================================================
        // CONVERT IPV6 LOCALHOST
        // =====================================================

        if ("0:0:0:0:0:0:0:1".equals(ipAddress)) {

            ipAddress =
                    "127.0.0.1";
        }


        // =====================================================
        // ROLE BASED LOGIN MESSAGE
        // =====================================================

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


        // =====================================================
        // CREATE LOGIN AUDIT LOG
        // =====================================================

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


        // =====================================================
        // GENERATE JWT ACCESS TOKEN
        // =====================================================

        String accessToken =
                jwtService.generateToken(username);


        // =====================================================
        // API REQUEST
        // RETURN ACCESS TOKEN
        // =====================================================

        if (request.getRequestURI()
                .startsWith("/api/")) {

            response.setStatus(
                    HttpServletResponse.SC_OK
            );

            response.setContentType(
                    "application/json"
            );

            response.setCharacterEncoding(
                    "UTF-8"
            );

            response.getWriter().write(

                    "{"
                    + "\"success\":true,"
                    + "\"message\":\"Login successful\","
                    + "\"accessToken\":\""
                    + accessToken
                    + "\","
                    + "\"username\":\""
                    + username
                    + "\","
                    + "\"role\":\""
                    + role
                    + "\""
                    + "}"
            );

            response.getWriter().flush();

            return;
        }


        // =====================================================
        // NORMAL WEBSITE LOGIN
        // KEEP EXISTING REDIRECT
        // =====================================================

        if ("ROLE_ADMIN".equals(role)) {

            response.sendRedirect(
                    "/admin/dashboard"
            );

            return;
        }


        if ("ROLE_STUDENT".equals(role)) {

            response.sendRedirect(
                    "/student/dashboard"
            );

            return;
        }


        // =====================================================
        // UNKNOWN ROLE
        // =====================================================

        response.sendRedirect(
                "/login"
        );
    }
}