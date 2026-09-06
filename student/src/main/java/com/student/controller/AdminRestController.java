package com.student.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.student.dto.ChangePasswordRequest;
import com.student.entity.Role;
import com.student.entity.Student;
import com.student.enums.StudentStatus;
import com.student.repository.RoleRepository;
import com.student.repository.StudentRepository;
import com.student.service.AuditLogService;

@RestController
@RequestMapping("/api/admins")
public class AdminRestController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditLogService auditLogService;


    // ============================================================
    // GET ALL ADMINS
    // ADMIN ONLY
    // GET /api/admins
    // ============================================================

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<Student>> getAllAdmins() {

        List<Student> admins =
                studentRepository.findByRole_Name("ROLE_ADMIN")
                        .stream()
                        .filter(student ->
                                !"true".equalsIgnoreCase(
                                        student.getIsDeleted()))
                        .toList();

        return ResponseEntity.ok(admins);
    }


    // ============================================================
    // GET ADMIN BY ID
    // ADMIN ONLY
    // GET /api/admins/{id}
    // ============================================================

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getAdminById(
            @PathVariable Long id) {

        Optional<Student> optionalAdmin =
                studentRepository.findById(id);

        if (optionalAdmin.isEmpty()) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Admin not found with ID: " + id);
        }

        Student admin = optionalAdmin.get();

        // Check soft delete
        if ("true".equalsIgnoreCase(
                admin.getIsDeleted())) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Admin not found with ID: " + id);
        }

        // Make sure ID belongs to admin
        if (admin.getRole() == null
                || !"ROLE_ADMIN".equalsIgnoreCase(
                        admin.getRole().getName())) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Admin not found with ID: " + id);
        }

        return ResponseEntity.ok(admin);
    }


    // ============================================================
    // CREATE ADMIN
    // ADMIN ONLY
    // POST /api/admins
    // ============================================================

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> createAdmin(
            @RequestBody Student admin,
            Authentication authentication) {

        if (admin == null) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Admin data is required");
        }

        if (admin.getEmail() == null
                || admin.getEmail().isBlank()) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Email is required");
        }

        // Check email
        boolean emailExists =
                studentRepository.findAll()
                        .stream()
                        .anyMatch(existingAdmin ->
                                existingAdmin.getEmail() != null
                                && existingAdmin.getEmail()
                                        .equalsIgnoreCase(
                                                admin.getEmail())
                                && !"true".equalsIgnoreCase(
                                        existingAdmin.getIsDeleted()));

        if (emailExists) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(
                            "Admin already exists with email: "
                            + admin.getEmail()
                    );
        }

        // Get ROLE_ADMIN
        Role adminRole =
                roleRepository.findById(1L)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "ROLE_ADMIN not found"));

        admin.setRole(adminRole);

        // Password
        if (admin.getPassword() != null
                && !admin.getPassword().isBlank()) {

            admin.setPassword(
                    passwordEncoder.encode(
                            admin.getPassword()));
        }

        // Default status
        admin.setStatus(StudentStatus.ACTIVE);

        // Default soft delete
        admin.setIsDeleted("false");

        // Save
        Student savedAdmin =
                studentRepository.save(admin);

        // Audit log
        auditLogService.createLog(
                savedAdmin.getId(),
                getUsername(authentication),
                getRole(authentication),
                "CREATE",
                "ADMIN",
                savedAdmin.getId(),
                "Admin created successfully",
                "127.0.0.1"
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedAdmin);
    }


    // ============================================================
    // CHANGE OWN PASSWORD
    // ADMIN ONLY
    // PUT /api/admins/change-password
    // ============================================================

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/change-password")
    public ResponseEntity<?> changeOwnPassword(
            @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Authentication required");
        }

        if (request == null) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Request body is required");
        }

        if (request.getCurrentPassword() == null
                || request.getCurrentPassword().isBlank()) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Current password is required");
        }

        if (request.getNewPassword() == null
                || request.getNewPassword().isBlank()) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("New password is required");
        }

        if (request.getConfirmPassword() == null
                || request.getConfirmPassword().isBlank()) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Confirm password is required");
        }

        if (!request.getNewPassword()
                .equals(request.getConfirmPassword())) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            "New password and confirm password do not match");
        }

        if (request.getNewPassword().length() < 6) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            "New password must contain at least 6 characters");
        }

        String email = authentication.getName();

        Optional<Student> optionalAdmin =
                studentRepository.findByEmail(email);

        if (optionalAdmin.isEmpty()) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Logged-in admin not found");
        }

        Student admin = optionalAdmin.get();

        if ("true".equalsIgnoreCase(
                admin.getIsDeleted())) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Logged-in admin not found");
        }

        if (admin.getRole() == null
                || !"ROLE_ADMIN".equalsIgnoreCase(
                        admin.getRole().getName())) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("User is not an admin");
        }

        // Verify current password
        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                admin.getPassword())) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Current password is incorrect");
        }

        // New password must be different
        if (passwordEncoder.matches(
                request.getNewPassword(),
                admin.getPassword())) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            "New password must be different from current password");
        }

        // Encode new password
        admin.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()));

        Student savedAdmin =
                studentRepository.save(admin);

        // Audit
        auditLogService.createLog(
                savedAdmin.getId(),
                getUsername(authentication),
                getRole(authentication),
                "PASSWORD_CHANGE",
                "ADMIN",
                savedAdmin.getId(),
                "Admin changed password successfully",
                "127.0.0.1"
        );

        return ResponseEntity.ok(
                "Password changed successfully");
    }


    // ============================================================
    // UPDATE ADMIN
    // ADMIN CAN UPDATE ONLY OWN PROFILE
    // PUT /api/admins/{id}
    // ============================================================

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAdmin(
            @PathVariable Long id,
            @RequestBody Student updatedAdmin,
            Authentication authentication) {

        Optional<Student> optionalAdmin =
                studentRepository.findById(id);

        if (optionalAdmin.isEmpty()) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Admin not found with ID: " + id);
        }

        Student existingAdmin =
                optionalAdmin.get();

        // Soft delete check
        if ("true".equalsIgnoreCase(
                existingAdmin.getIsDeleted())) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Admin not found with ID: " + id);
        }

        // Role check
        if (existingAdmin.getRole() == null
                || !"ROLE_ADMIN".equalsIgnoreCase(
                        existingAdmin.getRole().getName())) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Admin not found with ID: " + id);
        }

        // Only own profile
        if (authentication == null
                || existingAdmin.getEmail() == null
                || !existingAdmin.getEmail()
                        .equalsIgnoreCase(
                                authentication.getName())) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "You can update only your own admin profile");
        }

        // ========================================================
        // EMAIL
        // ========================================================

        if (updatedAdmin.getEmail() != null
                && !updatedAdmin.getEmail().isBlank()
                && !existingAdmin.getEmail()
                        .equalsIgnoreCase(
                                updatedAdmin.getEmail())) {

            boolean emailExists =
                    studentRepository.findAll()
                            .stream()
                            .anyMatch(otherAdmin ->
                                    otherAdmin.getId() != null
                                    && !otherAdmin.getId()
                                            .equals(id)
                                    && otherAdmin.getEmail() != null
                                    && otherAdmin.getEmail()
                                            .equalsIgnoreCase(
                                                    updatedAdmin
                                                            .getEmail())
                                    && !"true".equalsIgnoreCase(
                                            otherAdmin
                                                    .getIsDeleted()));

            if (emailExists) {

                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(
                                "Admin already exists with email: "
                                + updatedAdmin.getEmail());
            }

            existingAdmin.setEmail(
                    updatedAdmin.getEmail());
        }

        // ========================================================
        // NAME
        // ========================================================

        if (updatedAdmin.getName() != null) {

            existingAdmin.setName(
                    updatedAdmin.getName());
        }

        // ========================================================
        // AGE
        // ========================================================

        existingAdmin.setAge(
                updatedAdmin.getAge());

        // ========================================================
        // DEPARTMENT
        // ========================================================

        if (updatedAdmin.getDepartment() != null) {

            existingAdmin.setDepartment(
                    updatedAdmin.getDepartment());
        }

        // ========================================================
        // CITY
        // ========================================================

        if (updatedAdmin.getCity() != null) {

            existingAdmin.setCity(
                    updatedAdmin.getCity());
        }

        // ========================================================
        // CONTACT
        // ========================================================

        if (updatedAdmin.getContactNo() != null) {

            existingAdmin.setContactNo(
                    updatedAdmin.getContactNo());
        }

        // ========================================================
        // ADDRESS
        // ========================================================

        if (updatedAdmin.getAddress() != null) {

            existingAdmin.setAddress(
                    updatedAdmin.getAddress());
        }

        // ========================================================
        // PROFILE PHOTO
        //
        // Student.profilePhoto is byte[]
        // ========================================================

        if (updatedAdmin.getProfilePhoto() != null
                && updatedAdmin.getProfilePhoto().length > 0) {

            existingAdmin.setProfilePhoto(
                    updatedAdmin.getProfilePhoto());
        }

        // ========================================================
        // PASSWORD
        // ========================================================

        if (updatedAdmin.getPassword() != null
                && !updatedAdmin.getPassword().isBlank()) {

            existingAdmin.setPassword(
                    passwordEncoder.encode(
                            updatedAdmin.getPassword()));
        }

        // ========================================================
        // STATUS
        // ========================================================

        if (updatedAdmin.getStatus() != null) {

            existingAdmin.setStatus(
                    updatedAdmin.getStatus());
        }

        // Do not allow changing role
        existingAdmin.setRole(
                existingAdmin.getRole());

        // Save
        Student savedAdmin =
                studentRepository.save(existingAdmin);

        // Audit
        auditLogService.createLog(
                savedAdmin.getId(),
                getUsername(authentication),
                getRole(authentication),
                "UPDATE",
                "ADMIN",
                savedAdmin.getId(),
                "Admin updated successfully",
                "127.0.0.1"
        );

        return ResponseEntity.ok(savedAdmin);
    }


    // ============================================================
    // DELETE ADMIN
    // ADMIN CAN DELETE ONLY OWN PROFILE
    // DELETE /api/admins/{id}
    // ============================================================

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAdmin(
            @PathVariable Long id,
            Authentication authentication) {

        Optional<Student> optionalAdmin =
                studentRepository.findById(id);

        if (optionalAdmin.isEmpty()) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Admin not found with ID: " + id);
        }

        Student admin =
                optionalAdmin.get();

        // Soft delete check
        if ("true".equalsIgnoreCase(
                admin.getIsDeleted())) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            "Admin already deleted with ID: "
                            + id);
        }

        // Role check
        if (admin.getRole() == null
                || !"ROLE_ADMIN".equalsIgnoreCase(
                        admin.getRole().getName())) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Admin not found with ID: " + id);
        }

        // Only own profile
        if (authentication == null
                || admin.getEmail() == null
                || !admin.getEmail()
                        .equalsIgnoreCase(
                                authentication.getName())) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "You can delete only your own admin profile");
        }

        // Soft delete
        admin.setIsDeleted("true");

        admin.setStatus(
                StudentStatus.INACTIVE);

        Student deletedAdmin =
                studentRepository.save(admin);

        // Audit
        auditLogService.createLog(
                deletedAdmin.getId(),
                getUsername(authentication),
                getRole(authentication),
                "DELETE",
                "ADMIN",
                deletedAdmin.getId(),
                "Admin deleted successfully",
                "127.0.0.1"
        );

        return ResponseEntity.ok(
                "Admin deleted successfully with ID: "
                + id);
    }


    // ============================================================
    // GET USERNAME
    // ============================================================

    private String getUsername(
            Authentication authentication) {

        if (authentication == null) {
            return "SYSTEM";
        }

        return authentication.getName();
    }


    // ============================================================
    // GET ROLE
    // ============================================================

    private String getRole(
            Authentication authentication) {

        if (authentication == null) {
            return "SYSTEM";
        }

        return authentication.getAuthorities()
                .stream()
                .findFirst()
                .map(authority ->
                        authority.getAuthority())
                .orElse("UNKNOWN");
    }
}