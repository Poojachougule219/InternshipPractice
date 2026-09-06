package com.student.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.student.entity.Student;
import com.student.enums.StudentStatus;
import com.student.repository.StudentRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private StudentRepository studentRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        System.out.println("================================");
        System.out.println("LOGIN EMAIL : " + email);

        /*
         * Find active and non-deleted user.
         *
         * Repository method returns Optional<Student>,
         * so use orElseThrow() to get Student.
         */
        Student student = studentRepository
                .findByEmailAndStatusAndIsDeleted(
                        email,
                        StudentStatus.ACTIVE,
                        "false"
                )
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found or inactive : " + email
                        )
                );

        System.out.println(
                "USER FOUND : " + student.getEmail()
        );

        System.out.println(
                "DB PASSWORD : " + student.getPassword()
        );

        if (student.getRole() != null) {

            System.out.println(
                    "ROLE : " + student.getRole().getName()
            );

        } else {

            System.out.println(
                    "ROLE : NO ROLE ASSIGNED"
            );
        }

        System.out.println("================================");

        return new CustomUserDetails(student);
    }
}