package com.student.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUserDetailsService userDetailsService;


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println(
                "===== JWT FILTER CALLED ====="
        );

        String authHeader =
                request.getHeader("Authorization");

        System.out.println(
                "AUTH HEADER : " + authHeader
        );


        // =================================================
        // NO TOKEN
        // =================================================

        if (authHeader == null
                || !authHeader.startsWith("Bearer ")) {

            System.out.println(
                    "NO JWT TOKEN FOUND"
            );

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }


        // =================================================
        // GET TOKEN
        // =================================================

        String jwt =
                authHeader.substring(7);

        String email =
                jwtService.extractUsername(jwt);

        System.out.println(
                "EMAIL FROM TOKEN : " + email
        );


        // =================================================
        // AUTHENTICATE USER
        // =================================================

        if (email != null
                && SecurityContextHolder
                        .getContext()
                        .getAuthentication() == null) {

            UserDetails userDetails =
                    userDetailsService
                            .loadUserByUsername(email);

            System.out.println(
                    "USER : "
                    + userDetails.getUsername()
            );

            System.out.println(
                    "AUTHORITIES : "
                    + userDetails.getAuthorities()
            );


            // =============================================
            // VALIDATE TOKEN
            // =============================================

            if (jwtService.isTokenValid(
                    jwt,
                    userDetails.getUsername())) {

                UsernamePasswordAuthenticationToken
                        authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );


                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );


                SecurityContextHolder
                        .getContext()
                        .setAuthentication(
                                authentication
                        );


                System.out.println(
                        "JWT AUTHENTICATION SUCCESS"
                );

            } else {

                System.out.println(
                        "JWT TOKEN INVALID"
                );
            }
        }


        // =================================================
        // CONTINUE
        // =================================================

        filterChain.doFilter(
                request,
                response
        );
    }
}