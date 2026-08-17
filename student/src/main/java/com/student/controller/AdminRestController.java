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
                studentRepository.findAll()
                        .stream()
                        .filter(student ->
                                !"true".equalsIgnoreCase(
                                        student.getIsDeleted()))
                        .filter(student ->
                                student.getRole() != null
                                && "ROLE_ADMIN".equalsIgnoreCase(
                                        student.getRole().getName()))
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

        Student admin =
                optionalAdmin.get();


        // ========================================================
        // CHECK SOFT DELETE
        // ========================================================

        if ("true".equalsIgnoreCase(
                admin.getIsDeleted())) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Admin not found with ID: " + id);
        }


        // ========================================================
        // MAKE SURE ID BELONGS TO ADMIN
        // ========================================================

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


        // ========================================================
        // CHECK EMAIL
        // ========================================================

        boolean emailExists =
                studentRepository.findAll()
                        .stream()
                        .anyMatch(existingAdmin ->
                                existingAdmin.getEmail() != null
                                && admin.getEmail() != null
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


        // ========================================================
        // GET ADMIN ROLE
        // ========================================================

        Role adminRole =
                roleRepository.findById(1L)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "ROLE_ADMIN not found"
                                ));

        admin.setRole(adminRole);


        // ========================================================
        // PASSWORD
        // ========================================================

        if (admin.getPassword() != null
                && !admin.getPassword().isBlank()) {

            admin.setPassword(
                    passwordEncoder.encode(
                            admin.getPassword()
                    )
            );
        }


        // ========================================================
        // DEFAULT STATUS
        // ========================================================

        admin.setStatus(
                StudentStatus.ACTIVE
        );


        // ========================================================
        // DEFAULT SOFT DELETE
        // ========================================================

        admin.setIsDeleted(
                "false"
        );


        // ========================================================
        // SAVE ADMIN
        // ========================================================

        Student savedAdmin =
                studentRepository.save(admin);


        // ========================================================
        // AUDIT LOG
        // ========================================================

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


        // ========================================================
        // RESPONSE
        // ========================================================

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedAdmin);
    }


    // ============================================================
    // CHANGE OWN PASSWORD
    // ADMIN ONLY
    // PUT /api/admins/change-password
    //
    // ADMIN CAN CHANGE ONLY HIS/HER OWN PASSWORD
    // ============================================================

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/change-password")
    public ResponseEntity<?> changeOwnPassword(
            @RequestBody ChangePasswordRequest request,
            Authentication authentication) {


        // ========================================================
        // AUTHENTICATION CHECK
        // ========================================================

        if (authentication == null
                || !authentication.isAuthenticated()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Authentication required");
        }


        // ========================================================
        // REQUEST BODY CHECK
        // ========================================================

        if (request == null) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Request body is required");
        }


        // ========================================================
        // CURRENT PASSWORD VALIDATION
        // ========================================================

        if (request.getCurrentPassword() == null
                || request.getCurrentPassword().isBlank()) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Current password is required");
        }


        // ========================================================
        // NEW PASSWORD VALIDATION
        // ========================================================

        if (request.getNewPassword() == null
                || request.getNewPassword().isBlank()) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("New password is required");
        }


        // ========================================================
        // CONFIRM PASSWORD VALIDATION
        // ========================================================

        if (request.getConfirmPassword() == null
                || request.getConfirmPassword().isBlank()) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Confirm password is required");
        }


        // ========================================================
        // CHECK PASSWORD MATCH
        // ========================================================

        if (!request.getNewPassword()
                .equals(request.getConfirmPassword())) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            "New password and confirm password do not match"
                    );
        }


        // ========================================================
        // PASSWORD LENGTH
        // ========================================================

        if (request.getNewPassword().length() < 6) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            "New password must contain at least 6 characters"
                    );
        }


        // ========================================================
        // GET LOGGED-IN ADMIN EMAIL
        //
        // IMPORTANT:
        // We DO NOT accept admin ID from request.
        //
        // authentication.getName() identifies the currently
        // logged-in admin.
        // ========================================================

        String email = authentication.getName();


        // ========================================================
        // FIND LOGGED-IN ADMIN
        // ========================================================

        Optional<Student> optionalAdmin =
                studentRepository.findAll()
                        .stream()
                        .filter(admin ->
                                admin.getEmail() != null
                                && admin.getEmail()
                                        .equalsIgnoreCase(email))
                        .filter(admin ->
                                !"true".equalsIgnoreCase(
                                        admin.getIsDeleted()))
                        .filter(admin ->
                                admin.getRole() != null
                                && "ROLE_ADMIN".equalsIgnoreCase(
                                        admin.getRole().getName()))
                        .findFirst();


        // ========================================================
        // ADMIN NOT FOUND
        // ========================================================

        if (optionalAdmin.isEmpty()) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Logged-in admin not found");
        }


        Student admin =
                optionalAdmin.get();


        // ========================================================
        // VERIFY CURRENT PASSWORD
        // ========================================================

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                admin.getPassword())) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Current password is incorrect");
        }


        // ========================================================
        // NEW PASSWORD MUST BE DIFFERENT
        // ========================================================

        if (passwordEncoder.matches(
                request.getNewPassword(),
                admin.getPassword())) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            "New password must be different from current password"
                    );
        }


        // ========================================================
        // ENCODE NEW PASSWORD
        // ========================================================

        admin.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );


        // ========================================================
        // SAVE ADMIN
        // ========================================================

        Student savedAdmin =
                studentRepository.save(admin);


        // ========================================================
        // AUDIT LOG
        // ========================================================

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


        // ========================================================
        // RESPONSE
        // ========================================================

        return ResponseEntity
                .ok("Password changed successfully");
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


        // ========================================================
        // FIND ADMIN
        // ========================================================

        Optional<Student> optionalAdmin =
                studentRepository.findById(id);

        if (optionalAdmin.isEmpty()) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Admin not found with ID: " + id);
        }

        Student existingAdmin =
                optionalAdmin.get();


        // ========================================================
        // CHECK SOFT DELETE
        // ========================================================

        if ("true".equalsIgnoreCase(
                existingAdmin.getIsDeleted())) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Admin not found with ID: " + id);
        }


        // ========================================================
        // MAKE SURE ID BELONGS TO ADMIN
        // ========================================================

        if (existingAdmin.getRole() == null
                || !"ROLE_ADMIN".equalsIgnoreCase(
                        existingAdmin.getRole().getName())) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Admin not found with ID: " + id);
        }


        // ========================================================
        // ADMIN CAN UPDATE ONLY OWN PROFILE
        // ========================================================

        if (authentication == null
                || existingAdmin.getEmail() == null
                || !existingAdmin.getEmail()
                        .equalsIgnoreCase(
                                authentication.getName())) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "You can update only your own admin profile"
                    );
        }


        // ========================================================
        // CHECK EMAIL
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
                                + updatedAdmin.getEmail()
                        );
            }

            existingAdmin.setEmail(
                    updatedAdmin.getEmail()
            );
        }


        // ========================================================
        // UPDATE ADMIN NAME
        // ========================================================

        if (updatedAdmin.getName() != null) {

            existingAdmin.setName(
                    updatedAdmin.getName()
            );
        }


        // ========================================================
        // UPDATE AGE
        // age is int, therefore no null check
        // ========================================================

        existingAdmin.setAge(
                updatedAdmin.getAge()
        );


        // ========================================================
        // UPDATE DEPARTMENT
        // ========================================================

        if (updatedAdmin.getDepartment() != null) {

            existingAdmin.setDepartment(
                    updatedAdmin.getDepartment()
            );
        }


        // ========================================================
        // UPDATE CITY
        // ========================================================

        if (updatedAdmin.getCity() != null) {

            existingAdmin.setCity(
                    updatedAdmin.getCity()
            );
        }


        // ========================================================
        // UPDATE CONTACT
        // ========================================================

        if (updatedAdmin.getContactNo() != null) {

            existingAdmin.setContactNo(
                    updatedAdmin.getContactNo()
            );
        }


        // ========================================================
        // UPDATE ADDRESS
        // ========================================================

        if (updatedAdmin.getAddress() != null) {

            existingAdmin.setAddress(
                    updatedAdmin.getAddress()
            );
        }


        // ========================================================
        // UPDATE PROFILE PHOTO
        // ========================================================

        if (updatedAdmin.getProfilePhoto() != null
                && !updatedAdmin.getProfilePhoto().isBlank()) {

            existingAdmin.setProfilePhoto(
                    updatedAdmin.getProfilePhoto()
            );
        }


        // ========================================================
        // UPDATE PASSWORD
        // ========================================================

        if (updatedAdmin.getPassword() != null
                && !updatedAdmin.getPassword().isBlank()) {

            existingAdmin.setPassword(
                    passwordEncoder.encode(
                            updatedAdmin.getPassword()
                    )
            );
        }


        // ========================================================
        // UPDATE STATUS
        // ========================================================

        if (updatedAdmin.getStatus() != null) {

            existingAdmin.setStatus(
                    updatedAdmin.getStatus()
            );
        }


        // ========================================================
        // ROLE MUST REMAIN ADMIN
        // ========================================================

        existingAdmin.setRole(
                existingAdmin.getRole()
        );


        // ========================================================
        // SAVE
        // ========================================================

        Student savedAdmin =
                studentRepository.save(existingAdmin);


        // ========================================================
        // AUDIT LOG
        // ========================================================

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


        // ========================================================
        // RESPONSE
        // ========================================================

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


        // ========================================================
        // FIND ADMIN
        // ========================================================

        Optional<Student> optionalAdmin =
                studentRepository.findById(id);

        if (optionalAdmin.isEmpty()) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Admin not found with ID: " + id);
        }

        Student admin =
                optionalAdmin.get();


        // ========================================================
        // CHECK SOFT DELETE
        // ========================================================

        if ("true".equalsIgnoreCase(
                admin.getIsDeleted())) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Admin already deleted with ID: " + id);
        }


        // ========================================================
        // MAKE SURE ID BELONGS TO ADMIN
        // ========================================================

        if (admin.getRole() == null
                || !"ROLE_ADMIN".equalsIgnoreCase(
                        admin.getRole().getName())) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Admin not found with ID: " + id);
        }


        // ========================================================
        // ADMIN CAN DELETE ONLY OWN PROFILE
        // ========================================================

        if (authentication == null
                || admin.getEmail() == null
                || !admin.getEmail()
                        .equalsIgnoreCase(
                                authentication.getName())) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "You can delete only your own admin profile"
                    );
        }


        // ========================================================
        // SOFT DELETE
        // ========================================================

        admin.setIsDeleted("true");

        admin.setStatus(
                StudentStatus.INACTIVE
        );


        Student deletedAdmin =
                studentRepository.save(admin);


        // ========================================================
        // AUDIT LOG
        // ========================================================

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


        // ========================================================
        // RESPONSE
        // ========================================================

        return ResponseEntity.ok(
                "Admin deleted successfully with ID: " + id
        );
    }


    // ============================================================
    // GET LOGGED-IN USERNAME
    // ============================================================

    private String getUsername(
            Authentication authentication) {

        if (authentication == null) {
            return "SYSTEM";
        }

        return authentication.getName();
    }


    // ============================================================
    // GET LOGGED-IN ROLE
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