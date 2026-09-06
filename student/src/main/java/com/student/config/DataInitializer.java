package com.student.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.student.entity.Role;
import com.student.repository.RoleRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initRoles(RoleRepository roleRepository) {
        return args -> {

            if (roleRepository.count() == 0) {

                Role admin = new Role();
                admin.setName("ROLE_ADMIN");

                Role student = new Role();
                student.setName("ROLE_STUDENT");

                roleRepository.save(admin);
                roleRepository.save(student);
            }
        };
    }
}