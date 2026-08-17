package com.student.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.student.dto.LoginRequest;
import com.student.dto.LoginResponse;
import com.student.dto.RefreshTokenRequest;
import com.student.entity.RefreshToken;
import com.student.entity.Student;
import com.student.exception.ResourceNotFoundException;
import com.student.repository.StudentRepository;
import com.student.security.CustomUserDetails;
import com.student.security.JwtService;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private StudentRepository studentRepository;


    // =====================================================
    // LOGIN
    // =====================================================

    public LoginResponse login(LoginRequest request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
                                request.getPassword()
                        )
                );


        // =================================================
        // GET USER DETAILS
        // =================================================

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();


        // =================================================
        // GENERATE ACCESS TOKEN
        // =================================================

        String accessToken =
                jwtService.generateToken(
                        userDetails.getUsername()
                );


        // =================================================
        // GENERATE REFRESH TOKEN
        // =================================================

        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(
                        userDetails.getUsername()
                );


        // =================================================
        // GET ROLE
        // =================================================

        String role =
                userDetails.getAuthorities()
                        .iterator()
                        .next()
                        .getAuthority();


        // =================================================
        // GET USER ID
        // =================================================

        Long studentId =
                userDetails.getStudent().getId();


        // =================================================
        // ENTITY NAME
        // =================================================

        String entityName;

        if ("ROLE_ADMIN".equalsIgnoreCase(role)) {

            entityName = "ADMIN";

        } else if ("ROLE_STUDENT".equalsIgnoreCase(role)) {

            entityName = "STUDENT";

        } else {

            entityName = "USER";
        }


        // =================================================
        // LOGIN MESSAGE
        // =================================================

        String loginMessage;

        if ("ROLE_ADMIN".equalsIgnoreCase(role)) {

            loginMessage =
                    "Admin logged in successfully";

        } else if ("ROLE_STUDENT".equalsIgnoreCase(role)) {

            loginMessage =
                    "Student logged in successfully";

        } else {

            loginMessage =
                    "User logged in successfully";
        }


        // =================================================
        // LOGIN AUDIT LOG
        // =================================================

        auditLogService.createLog(
                studentId,
                userDetails.getUsername(),
                role,
                "LOGIN",
                entityName,
                studentId,
                loginMessage,
                null
        );


        // =================================================
        // RETURN RESPONSE
        // =================================================

        return new LoginResponse(
                accessToken,
                refreshToken.getToken(),
                role
        );
    }


    // =====================================================
    // REFRESH ACCESS TOKEN
    // =====================================================

    public LoginResponse refreshToken(
            RefreshTokenRequest request) {

        // =================================================
        // FIND REFRESH TOKEN
        // =================================================

        RefreshToken refreshToken =
                refreshTokenService.findByToken(
                        request.getRefreshToken()
                );


        // =================================================
        // VERIFY EXPIRATION
        // =================================================

        refreshTokenService.verifyExpiration(
                refreshToken
        );


        // =================================================
        // GENERATE NEW ACCESS TOKEN
        // =================================================

        String accessToken =
                jwtService.generateToken(
                        refreshToken.getStudent().getEmail()
                );


        // =================================================
        // GET ROLE
        // =================================================

        String role =
                refreshToken.getStudent()
                        .getRole()
                        .getName();


        // =================================================
        // RETURN RESPONSE
        // =================================================

        return new LoginResponse(
                accessToken,
                refreshToken.getToken(),
                role
        );
    }


    // =====================================================
    // LOGOUT
    // =====================================================

    public void logout(
            Long studentId,
            String ipAddress) {

        // =================================================
        // FIND ACTIVE USER
        // =================================================

        Student student =
                studentRepository
                        .findByIdAndIsDeleted(
                                studentId,
                                "false"
                        )
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Student not found"
                                )
                        );


        // =================================================
        // DELETE REFRESH TOKEN
        // =================================================

        refreshTokenService.deleteByStudentId(
                studentId
        );


        // =================================================
        // GET ROLE
        // =================================================

        String role = "ROLE_STUDENT";

        if (student.getRole() != null
                && student.getRole().getName() != null) {

            role =
                    student.getRole().getName();
        }


        // =================================================
        // ENTITY NAME
        // =================================================

        String entityName;

        if ("ROLE_ADMIN".equalsIgnoreCase(role)) {

            entityName = "ADMIN";

        } else if ("ROLE_STUDENT".equalsIgnoreCase(role)) {

            entityName = "STUDENT";

        } else {

            entityName = "USER";
        }


        // =================================================
        // LOGOUT MESSAGE
        // =================================================

        String logoutMessage;

        if ("ROLE_ADMIN".equalsIgnoreCase(role)) {

            logoutMessage =
                    "Admin logged out successfully";

        } else if ("ROLE_STUDENT".equalsIgnoreCase(role)) {

            logoutMessage =
                    "Student logged out successfully";

        } else {

            logoutMessage =
                    "User logged out successfully";
        }


        // =================================================
        // LOGOUT AUDIT LOG
        // =================================================

        auditLogService.createLog(
                student.getId(),
                student.getEmail(),
                role,
                "LOGOUT",
                entityName,
                student.getId(),
                logoutMessage,
                ipAddress
        );
    }
}