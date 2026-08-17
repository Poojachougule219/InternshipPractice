package com.student.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.student.entity.Student;
import com.student.repository.StudentRepository;
import com.student.service.AuditLogService;

@RestController
@RequestMapping("/api/students")
public class StudentRestController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private PasswordEncoder passwordEncoder;


    // ============================================================
    // GET ALL STUDENTS
    // ADMIN + STUDENT CAN VIEW
    // GET /api/students
    // ============================================================
    @GetMapping
    public ResponseEntity<List<Student>> getAllStudents() {

        List<Student> students = studentRepository.findAll()
                .stream()
                .filter(student ->
                        !"true".equalsIgnoreCase(student.getIsDeleted()))
                .filter(student ->
                        student.getRole() != null
                        && "ROLE_STUDENT".equalsIgnoreCase(
                                student.getRole().getName()))
                .toList();

        return ResponseEntity.ok(students);
    }


    // ============================================================
    // GET STUDENT BY ID
    // ADMIN + STUDENT CAN VIEW
    // GET /api/students/{id}
    // ============================================================
    @GetMapping("/{id}")
    public ResponseEntity<?> getStudentById(@PathVariable Long id) {

        return studentRepository.findById(id)
                .map(student -> {

                    if ("true".equalsIgnoreCase(
                            student.getIsDeleted())) {

                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Student not found with ID: " + id);
                    }

                    if (student.getRole() == null
                            || !"ROLE_STUDENT".equalsIgnoreCase(
                                    student.getRole().getName())) {

                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Student not found with ID: " + id);
                    }

                    return ResponseEntity.ok(student);
                })
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Student not found with ID: " + id));
    }


    // ============================================================
    // CREATE STUDENT
    // ADMIN ONLY
    // POST /api/students
    // ============================================================
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> createStudent(
            @RequestBody Student student,
            Authentication authentication) {

        boolean emailExists = studentRepository.findAll()
                .stream()
                .anyMatch(existingStudent ->
                        existingStudent.getEmail()
                                .equalsIgnoreCase(student.getEmail())
                        && !"true".equalsIgnoreCase(
                                existingStudent.getIsDeleted()));

        if (emailExists) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Student already exists with email: "
                            + student.getEmail());
        }

        student.setIsDeleted("false");

        Student savedStudent = studentRepository.save(student);

        auditLogService.createLog(
                savedStudent.getId(),
                getUsername(authentication),
                getRole(authentication),
                "CREATE",
                "STUDENT",
                savedStudent.getId(),
                "Student created successfully",
                "127.0.0.1"
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedStudent);
    }


    // ============================================================
    // UPDATE STUDENT
    // ADMIN ONLY
    // PUT /api/students/{id}
    // ============================================================
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateStudent(
            @PathVariable Long id,
            @RequestBody Student updatedStudent,
            Authentication authentication) {

        return studentRepository.findById(id)
                .map(existingStudent -> {

                    if ("true".equalsIgnoreCase(
                            existingStudent.getIsDeleted())) {

                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Student not found with ID: " + id);
                    }

                    // Make sure the ID belongs to a STUDENT
                    if (existingStudent.getRole() == null
                            || !"ROLE_STUDENT".equalsIgnoreCase(
                                    existingStudent.getRole().getName())) {

                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Student not found with ID: " + id);
                    }

                    existingStudent.setName(updatedStudent.getName());
                    existingStudent.setAge(updatedStudent.getAge());
                    existingStudent.setDepartment(
                            updatedStudent.getDepartment());
                    existingStudent.setEmail(updatedStudent.getEmail());
                    existingStudent.setCity(updatedStudent.getCity());
                    existingStudent.setContactNo(
                            updatedStudent.getContactNo());
                    existingStudent.setAddress(
                            updatedStudent.getAddress());

                    if (updatedStudent.getProfilePhoto() != null) {
                        existingStudent.setProfilePhoto(
                                updatedStudent.getProfilePhoto());
                    }

                    if (updatedStudent.getPassword() != null
                            && !updatedStudent.getPassword().isBlank()) {

                        existingStudent.setPassword(
                                updatedStudent.getPassword());
                    }

                    if (updatedStudent.getStatus() != null) {
                        existingStudent.setStatus(
                                updatedStudent.getStatus());
                    }

                    Student savedStudent =
                            studentRepository.save(existingStudent);

                    auditLogService.createLog(
                            savedStudent.getId(),
                            getUsername(authentication),
                            getRole(authentication),
                            "UPDATE",
                            "STUDENT",
                            savedStudent.getId(),
                            "Student updated successfully",
                            "127.0.0.1"
                    );

                    return ResponseEntity.ok(savedStudent);
                })
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Student not found with ID: " + id));
    }


    // ============================================================
    // CHANGE PASSWORD
    // STUDENT CAN CHANGE ONLY OWN PASSWORD
    //
    // PUT /api/students/change-password
    // ============================================================
    @PreAuthorize("hasRole('STUDENT')")
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
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
        // GET LOGGED-IN STUDENT EMAIL
        // ========================================================

        String loggedInEmail =
                authentication.getName();


        // ========================================================
        // VALIDATE REQUEST
        // ========================================================

        if (request.getOldPassword() == null
                || request.getOldPassword().isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body("Current password is required");
        }

        if (request.getNewPassword() == null
                || request.getNewPassword().isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body("New password is required");
        }

        if (request.getConfirmPassword() == null
                || request.getConfirmPassword().isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body("Confirm password is required");
        }


        // ========================================================
        // CHECK NEW PASSWORD
        // ========================================================

        if (!request.getNewPassword()
                .equals(request.getConfirmPassword())) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "New password and confirm password do not match"
                    );
        }


        // ========================================================
        // FIND LOGGED-IN STUDENT
        // ========================================================

        Student student =
                studentRepository.findAll()
                        .stream()
                        .filter(s ->
                                s.getEmail() != null
                                && s.getEmail()
                                        .equalsIgnoreCase(
                                                loggedInEmail))
                        .filter(s ->
                                !"true".equalsIgnoreCase(
                                        s.getIsDeleted()))
                        .filter(s ->
                                s.getRole() != null
                                && "ROLE_STUDENT"
                                        .equalsIgnoreCase(
                                                s.getRole().getName()))
                        .findFirst()
                        .orElse(null);


        // ========================================================
        // STUDENT NOT FOUND
        // ========================================================

        if (student == null) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Student profile not found");
        }


        // ========================================================
        // CHECK OLD PASSWORD
        // ========================================================

        if (!passwordEncoder.matches(
                request.getOldPassword(),
                student.getPassword())) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Current password is incorrect");
        }


        // ========================================================
        // CHECK NEW PASSWORD SAME AS OLD PASSWORD
        // ========================================================

        if (passwordEncoder.matches(
                request.getNewPassword(),
                student.getPassword())) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "New password must be different from current password"
                    );
        }


        // ========================================================
        // CHANGE PASSWORD
        // ========================================================

        student.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );


        // ========================================================
        // SAVE STUDENT
        // ========================================================

        Student savedStudent =
                studentRepository.save(student);


        // ========================================================
        // AUDIT LOG
        // ========================================================

        auditLogService.createLog(
                savedStudent.getId(),
                getUsername(authentication),
                getRole(authentication),
                "PASSWORD_CHANGE",
                "STUDENT",
                savedStudent.getId(),
                "Student changed password successfully",
                "127.0.0.1"
        );


        // ========================================================
        // RESPONSE
        // ========================================================

        return ResponseEntity.ok(
                "Password changed successfully"
        );
    }


    // ============================================================
    // SOFT DELETE STUDENT
    // ADMIN ONLY
    // DELETE /api/students/{id}
    // ============================================================
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudent(
            @PathVariable Long id,
            Authentication authentication) {

        return studentRepository.findById(id)
                .map(student -> {

                    if ("true".equalsIgnoreCase(
                            student.getIsDeleted())) {

                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Student already deleted with ID: "
                                        + id);
                    }

                    if (student.getRole() == null
                            || !"ROLE_STUDENT".equalsIgnoreCase(
                                    student.getRole().getName())) {

                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Student not found with ID: " + id);
                    }

                    // SOFT DELETE
                    student.setIsDeleted("true");

                    Student deletedStudent =
                            studentRepository.save(student);

                    auditLogService.createLog(
                            deletedStudent.getId(),
                            getUsername(authentication),
                            getRole(authentication),
                            "DELETE",
                            "STUDENT",
                            deletedStudent.getId(),
                            "Student deleted successfully",
                            "127.0.0.1"
                    );

                    return ResponseEntity.ok(
                            "Student deleted successfully with ID: " + id);
                })
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Student not found with ID: " + id));
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


    // ============================================================
    // CHANGE PASSWORD REQUEST DTO
    // ============================================================

    public static class ChangePasswordRequest {

        private String oldPassword;
        private String newPassword;
        private String confirmPassword;


        public String getOldPassword() {
            return oldPassword;
        }

        public void setOldPassword(String oldPassword) {
            this.oldPassword = oldPassword;
        }


        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }


        public String getConfirmPassword() {
            return confirmPassword;
        }

        public void setConfirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
        }
    }
}