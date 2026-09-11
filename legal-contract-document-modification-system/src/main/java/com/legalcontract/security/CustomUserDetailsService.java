package com.legalcontract.security;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;

import com.legalcontract.entity.User;
import com.legalcontract.repository.UserRepository;

@Service
public class CustomUserDetailsService
        implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(
            UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(
            String username)
            throws UsernameNotFoundException {

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found: " + username
                        )
                );

        // =====================================================
        // CHECK STATUS
        // =====================================================

        if (user.getStatus() == null
                || !"ACTIVE".equalsIgnoreCase(
                        user.getStatus())) {

            throw new UsernameNotFoundException(
                    "User account is inactive"
            );
        }

        // =====================================================
        // CHECK ROLE
        // =====================================================

        if (user.getRole() == null
                || user.getRole().getName() == null
                || user.getRole().getName().isBlank()) {

            throw new UsernameNotFoundException(
                    "User role is not assigned"
            );
        }

        // =====================================================
        // ROLE
        // =====================================================

        String databaseRole =
                user.getRole().getName();

        String authority;

        if (databaseRole.startsWith("ROLE_")) {

            authority = databaseRole;

        } else {

            authority = "ROLE_" + databaseRole;
        }

        // =====================================================
        // SPRING SECURITY USER
        // =====================================================

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(
                        new SimpleGrantedAuthority(authority)
                )
        );
    }
}