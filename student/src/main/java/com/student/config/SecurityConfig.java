package com.student.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.student.entity.Student;
import com.student.repository.StudentRepository;
import com.student.security.CustomUserDetailsService;
import com.student.security.JwtAuthenticationFilter;
import com.student.security.RoleBasedSuccessHandler;
import com.student.service.AuditLogService;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private RoleBasedSuccessHandler successHandler;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private StudentRepository studentRepository;


    // =====================================================
    // SECURITY FILTER CHAIN
    // =====================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http)
            throws Exception {

        http

            // =================================================
            // CSRF
            // =================================================

            .csrf(csrf -> csrf.disable())


            // =================================================
            // EXCEPTION HANDLING
            // =================================================

            .exceptionHandling(exception -> exception

                    // =================================================
                    // 401 - USER IS NOT AUTHENTICATED
                    // =================================================

                    .defaultAuthenticationEntryPointFor(

                        (request, response, authException) -> {

                            response.setStatus(
                                    HttpStatus.UNAUTHORIZED.value()
                            );

                            response.setContentType(
                                    "application/json"
                            );

                            response.getWriter().write(
                                    "{\"success\":false,\"message\":\"Authentication required\"}"
                            );
                        },

                        request ->
                                request
                                        .getRequestURI()
                                        .startsWith("/api/")
                    )

                    // =================================================
                    // 403 - USER IS AUTHENTICATED
                    // BUT DOES NOT HAVE REQUIRED ROLE
                    // =================================================

                    .accessDeniedHandler(
                        (request, response, accessDeniedException) -> {

                            response.setStatus(
                                    HttpStatus.FORBIDDEN.value()
                            );

                            response.setContentType(
                                    "application/json"
                            );

                            response.getWriter().write(
                                    "{\"success\":false,\"message\":\"Access denied\"}"
                            );
                        }
                    )
            )


            // =================================================
            // AUTHORIZATION
            // =================================================

            .authorizeHttpRequests(auth -> auth

                // =================================================
                // PUBLIC URLS
                // =================================================

                .requestMatchers(
                        "/",
                        "/login",
                        "/register",
                        "/forgot-password",
                        "/verify-otp",
                        "/reset-password",
                        "/api/auth/**",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/uploads/**"
                )
                .permitAll()


                // =================================================
                // AUDIT LOG API - ADMIN ONLY
                // =================================================

                .requestMatchers(
                        "/api/audit-logs/**"
                )
                .hasRole("ADMIN")


                // =================================================
                // GET STUDENTS - ADMIN + STUDENT
                // =================================================

                .requestMatchers(
                        HttpMethod.GET,
                        "/api/students",
                        "/api/students/**"
                )
                .hasAnyRole(
                        "STUDENT",
                        "ADMIN"
                )


                // =================================================
                // CREATE STUDENT - ADMIN ONLY
                // POST /api/students
                // =================================================

                .requestMatchers(
                        HttpMethod.POST,
                        "/api/students",
                        "/api/students/**"
                )
                .hasRole("ADMIN")


                // =================================================
                // CHANGE PASSWORD - STUDENT ONLY
                //
                // IMPORTANT:
                // This must come BEFORE the general PUT rule
                // =================================================

                .requestMatchers(
                        HttpMethod.PUT,
                        "/api/students/change-password"
                )
                .hasRole("STUDENT")


                // =================================================
                // UPDATE STUDENT - ADMIN ONLY
                // PUT /api/students/{id}
                // =================================================

                .requestMatchers(
                        HttpMethod.PUT,
                        "/api/students",
                        "/api/students/**"
                )
                .hasRole("ADMIN")


                // =================================================
                // DELETE STUDENT - ADMIN ONLY
                // DELETE /api/students/{id}
                // =================================================

                .requestMatchers(
                        HttpMethod.DELETE,
                        "/api/students",
                        "/api/students/**"
                )
                .hasRole("ADMIN")


                // =================================================
                // ADMIN PAGES
                // =================================================

                .requestMatchers(
                        "/admin/**"
                )
                .hasRole("ADMIN")


                // =================================================
                // STUDENT PAGES
                // =================================================

                .requestMatchers(
                        "/student/**"
                )
                .hasRole("STUDENT")


                // =================================================
                // OTHER REQUESTS
                // =================================================

                .anyRequest()
                .authenticated()
            )


            // =================================================
            // FORM LOGIN
            // =================================================

            .formLogin(form -> form

                .loginPage("/login")

                .loginProcessingUrl("/login")

                .usernameParameter("username")

                .passwordParameter("password")


                // =================================================
                // LOGIN AUDIT
                // =================================================

                .successHandler(
                        successHandler
                )

                .failureUrl(
                        "/login?error=true"
                )

                .permitAll()
            )


            // =================================================
            // LOGOUT
            // =================================================

            .logout(logout -> logout

                .logoutSuccessHandler(

                    (request, response, authentication) -> {

                        // =========================================
                        // CHECK AUTHENTICATION
                        // =========================================

                        if (authentication != null) {

                            // =====================================
                            // GET USERNAME
                            // =====================================

                            String username =
                                    authentication.getName();


                            // =====================================
                            // GET ROLE
                            // =====================================

                            String role = null;

                            for (var authority :
                                    authentication.getAuthorities()) {

                                String authorityName =
                                        authority.getAuthority();

                                if ("ROLE_ADMIN".equals(
                                        authorityName)) {

                                    role = "ROLE_ADMIN";
                                    break;
                                }

                                if ("ROLE_STUDENT".equals(
                                        authorityName)) {

                                    role = "ROLE_STUDENT";
                                    break;
                                }
                            }


                            // =====================================
                            // FIND STUDENT / ADMIN
                            // =====================================

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


                            // =====================================
                            // GET IP ADDRESS
                            // =====================================

                            String ipAddress =
                                    request.getRemoteAddr();


                            // =====================================
                            // CONVERT IPV6 LOCALHOST
                            // =====================================

                            if ("0:0:0:0:0:0:0:1"
                                    .equals(ipAddress)) {

                                ipAddress =
                                        "127.0.0.1";
                            }


                            // =====================================
                            // CREATE LOGOUT AUDIT
                            // =====================================

                            if (role != null) {

                                String logoutMessage;

                                if ("ROLE_ADMIN".equals(
                                        role)) {

                                    logoutMessage =
                                            "Admin logged out successfully";

                                } else if ("ROLE_STUDENT".equals(
                                        role)) {

                                    logoutMessage =
                                            "Student logged out successfully";

                                } else {

                                    logoutMessage =
                                            "User logged out successfully";
                                }


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
                            }
                        }


                        // =====================================
                        // REDIRECT TO LOGIN
                        // =====================================

                        response.sendRedirect(
                                "/login"
                        );
                    }
                )

                .permitAll()
            )


            // =================================================
            // AUTHENTICATION PROVIDER
            // =================================================

            .authenticationProvider(
                    authenticationProvider()
            )


            // =================================================
            // JWT FILTER
            // =================================================

            .addFilterBefore(
                    jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class
            );


        return http.build();
    }


    // =====================================================
    // AUTHENTICATION PROVIDER
    // =====================================================

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();

        provider.setUserDetailsService(
                userDetailsService
        );

        provider.setPasswordEncoder(
                passwordEncoder()
        );

        return provider;
    }


    // =====================================================
    // PASSWORD ENCODER
    // =====================================================

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }


    // =====================================================
    // AUTHENTICATION MANAGER
    // =====================================================

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }
}