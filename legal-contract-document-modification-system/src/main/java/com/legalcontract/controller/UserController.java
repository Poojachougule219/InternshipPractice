
package com.legalcontract.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.legalcontract.entity.User;
import com.legalcontract.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // =========================================================
    // GET ALL USERS
    // =========================================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {

        return ResponseEntity.ok(
                userService.getAllUsers()
        );
    }

    // =========================================================
    // GET USER BY ID
    // =========================================================

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUserById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                userService.getUserById(id)
        );
    }

    // =========================================================
    // CREATE USER
    // =========================================================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> createUser(
            @RequestBody User user) {

        if (user.getRole() == null
                || user.getRole().getName() == null
                || user.getRole().getName().isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .build();
        }

        User savedUser = userService.createUser(
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getFullName(),
                user.getRole().getName()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedUser);
    }

    // =========================================================
    // UPDATE USER
    // =========================================================

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @RequestBody User user) {

        // -----------------------------------------------------
        // CHECK ROLE
        // -----------------------------------------------------

        if (user.getRole() == null
                || user.getRole().getName() == null
                || user.getRole().getName().isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .build();
        }

        // -----------------------------------------------------
        // UPDATE USER
        // -----------------------------------------------------

        User updatedUser = userService.updateUser(
                id,
                user,
                user.getRole().getName()
        );

        return ResponseEntity.ok(updatedUser);
    }

    // =========================================================
    // DELETE USER
    // =========================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(
            @PathVariable Long id) {

        userService.deleteUser(id);

        return ResponseEntity.ok(
                "User deleted successfully"
        );
    }

    // =========================================================
    // DEACTIVATE USER
    // =========================================================

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deactivateUser(
            @PathVariable Long id) {

        userService.deactivateUser(id);

        return ResponseEntity.ok(
                "User deactivated successfully"
        );
    }

    // =========================================================
    // ACTIVATE USER
    // =========================================================

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> activateUser(
            @PathVariable Long id) {

        userService.activateUser(id);

        return ResponseEntity.ok(
                "User activated successfully"
        );
    }
}
