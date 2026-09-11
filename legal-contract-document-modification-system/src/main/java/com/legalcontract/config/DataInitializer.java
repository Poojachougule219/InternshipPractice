package com.legalcontract.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.legalcontract.entity.Role;
import com.legalcontract.entity.User;
import com.legalcontract.repository.RoleRepository;
import com.legalcontract.repository.UserRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initializeData(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            // -------------------------
            // ADMIN ROLE
            // -------------------------

            Role adminRole = roleRepository
                    .findByName("ROLE_ADMIN")
                    .orElseGet(() -> {

                        Role role = new Role();

                        role.setName("ROLE_ADMIN");
                        role.setDescription(
                                "System Administrator");

                        return roleRepository.save(role);
                    });

            // -------------------------
            // USER ROLE
            // -------------------------

            Role userRole = roleRepository
                    .findByName("ROLE_USER")
                    .orElseGet(() -> {

                        Role role = new Role();

                        role.setName("ROLE_USER");
                        role.setDescription(
                                "Normal System User");

                        return roleRepository.save(role);
                    });

            // -------------------------
            // ADMIN USER
            // -------------------------

            if (!userRepository.existsByUsername("admin")) {

                User admin = new User();

                admin.setUsername("admin");
                admin.setEmail("admin@legalcontract.com");
                admin.setFullName("System Administrator");

                admin.setPassword(
                    passwordEncoder.encode("Admin@123")
                );

                admin.setRole(adminRole);
                admin.setStatus("ACTIVE");

                userRepository.save(admin);
            }

            // -------------------------
            // NORMAL USER
            // -------------------------

            if (!userRepository.existsByUsername("user")) {

                User user = new User();

                user.setUsername("user");
                user.setEmail("user@legalcontract.com");
                user.setFullName("Normal User");

                user.setPassword(
                    passwordEncoder.encode("User@123")
                );

                user.setRole(userRole);
                user.setStatus("ACTIVE");

                userRepository.save(user);
            }
        };
    }
}