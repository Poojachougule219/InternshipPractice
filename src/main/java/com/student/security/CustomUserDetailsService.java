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



        Student student = studentRepository
                .findByEmailAndStatusAndIsDeleted(
                        email,
                        StudentStatus.ACTIVE,
                        "false"
                )
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found : " + email
                        )
                );



        System.out.println("USER FOUND : "
                + student.getEmail());


        System.out.println("DB PASSWORD : "
                + student.getPassword());


        System.out.println("ROLE : "
                + student.getRole().getName());


        System.out.println("================================");



        return new CustomUserDetails(student);
    }

}