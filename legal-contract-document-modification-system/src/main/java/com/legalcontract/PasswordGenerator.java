package com.legalcontract;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {


public static void main(String[] args) {

    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    String password = "lawyer123";

    String encodedPassword = encoder.encode(password);

    System.out.println("Original Password: " + password);
    System.out.println("BCrypt Password: " + encodedPassword);
}


}
