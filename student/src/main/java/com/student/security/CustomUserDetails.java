package com.student.security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.student.entity.Student;

public class CustomUserDetails implements UserDetails {

    private static final long serialVersionUID = 1L;

    private final Student student;

    private String email;
    private String password;
    private String role;
    private boolean enabled;

    public CustomUserDetails(Student student) {

        this.student = student;

        this.email = student.getEmail();
        this.password = student.getPassword();

        if (student.getRole() != null) {
            this.role = student.getRole().getName();
        }

        this.enabled = true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        if (role == null || role.isEmpty()) {
            return Collections.emptyList();
        }

        return Collections.singletonList(
                new SimpleGrantedAuthority(role)
        );
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    // ⭐ Add this method
    public Student getStudent() {
        return student;
    }
}