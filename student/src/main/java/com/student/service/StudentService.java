package com.student.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.student.entity.AuditLog;
import com.student.entity.Role;
import com.student.entity.Student;
import com.student.enums.StudentStatus;
import com.student.exception.EmailAlreadyExistsException;
import com.student.exception.ResourceNotFoundException;
import com.student.repository.AuditLogRepository;
import com.student.repository.RoleRepository;
import com.student.repository.StudentRepository;

@Service
public class StudentService {

    @Autowired
    private StudentRepository repo;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditLogRepository auditLogRepository;


    // =====================================================
    // ADD STUDENT
    // =====================================================

    public Student addStudent(Student student) {
        return repo.save(student);
    }


    // =====================================================
    // GET ALL STUDENTS
    // =====================================================

    public List<Student> getAllStudents() {
        return repo.findAll();
    }


    // =====================================================
    // GET STUDENT BY ID
    // =====================================================

    public Student getStudentById(Long id) {
        return repo.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Student not found with id : " + id
                        )
                );
    }


    // =====================================================
    // FIND STUDENT BY EMAIL
    // USED BY StudentPageController
    // =====================================================

    public Optional<Student> findByEmail(String email) {
        return repo.findByEmail(email);
    }


    // =====================================================
    // GET STUDENT BY EMAIL
    // =====================================================

    public Student getStudentByEmail(String email) {
        return repo.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Student not found with email : " + email
                        )
                );
    }


    // =====================================================
    // UPDATE STUDENT
    // =====================================================

    public Student updateStudent(
            Long id,
            Student updateStudent) {

        Student student = getStudentById(id);

        if (updateStudent.getName() != null) {
            student.setName(updateStudent.getName());
        }

        student.setAge(updateStudent.getAge());

        if (updateStudent.getDepartment() != null) {
            student.setDepartment(
                    updateStudent.getDepartment()
            );
        }

        if (updateStudent.getEmail() != null) {
            student.setEmail(
                    updateStudent.getEmail()
            );
        }

        if (updateStudent.getCity() != null) {
            student.setCity(
                    updateStudent.getCity()
            );
        }

        if (updateStudent.getPassword() != null) {
            student.setPassword(
                    updateStudent.getPassword()
            );
        }

        if (updateStudent.getContactNo() != null) {
            student.setContactNo(
                    updateStudent.getContactNo()
            );
        }

        if (updateStudent.getAddress() != null) {
            student.setAddress(
                    updateStudent.getAddress()
            );
        }

        if (updateStudent.getStatus() != null) {
            student.setStatus(
                    updateStudent.getStatus()
            );
        }

        if (updateStudent.getProfilePhoto() != null) {
            student.setProfilePhoto(
                    updateStudent.getProfilePhoto()
            );
        }

        return repo.save(student);
    }


    // =====================================================
    // DELETE STUDENT
    // =====================================================

    public void deleteStudent(Long id) {
        Student student = getStudentById(id);
        repo.delete(student);
    }


    // =====================================================
    // SEARCH BY NAME
    // =====================================================

    public List<Student> searchByName(String name) {
        return repo.findByNameContainingIgnoreCase(name);
    }


    // =====================================================
    // SEARCH BY EMAIL
    // =====================================================

    public List<Student> searchByEmail(String email) {
        return repo.findByEmailContainingIgnoreCase(email);
    }


    // =====================================================
    // SEARCH BY DEPARTMENT
    // =====================================================

    public List<Student> searchByDepartment(
            String department) {

        return repo.findByDepartmentContainingIgnoreCase(
                department
        );
    }


    // =====================================================
    // SEARCH BY CITY
    // =====================================================

    public List<Student> searchByCity(String city) {
        return repo.findByCityContainingIgnoreCase(city);
    }


    // =====================================================
    // PAGINATION + SORTING
    // =====================================================

    public Page<Student> getStudents(
            int page,
            int size,
            String sortBy,
            String direction) {

        Sort sort =
                direction != null
                        && direction.equalsIgnoreCase("desc")
                        ? Sort.by(sortBy).descending()
                        : Sort.by(sortBy).ascending();

        Pageable pageable =
                PageRequest.of(page, size, sort);

        return repo.findAll(pageable);
    }


    // =====================================================
    // LOGIN
    // =====================================================

    public Student login(
            String email,
            String password) {

        return repo.findByEmailAndPassword(
                email,
                password
        );
    }


    // =====================================================
    // SIGNUP - SIMPLE VERSION
    // =====================================================

    public Student signup(Student student) {

        if (repo.existsByEmail(student.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "Email already exists"
            );
        }

        if (student.getPassword() == null
                || student.getPassword().isBlank()) {

            throw new IllegalArgumentException(
                    "Password is required"
            );
        }

        student.setPassword(
                passwordEncoder.encode(
                        student.getPassword()
                )
        );

        if (student.getStatus() == null) {
            student.setStatus(
                    StudentStatus.ACTIVE
            );
        }

        if (student.getIsDeleted() == null) {
            student.setIsDeleted("false");
        }

        return repo.save(student);
    }


    // =====================================================
    // SIGNUP
    // USED BY LoginController
    // =====================================================

    public Student signup(
            Student student,
            String roleId,
            MultipartFile profilePhoto,
            String ipAddress,
            Authentication authentication) {

        // -------------------------------------------------
        // CHECK EMAIL
        // -------------------------------------------------

        if (student.getEmail() == null
                || student.getEmail().isBlank()) {

            throw new IllegalArgumentException(
                    "Email is required"
            );
        }

        boolean emailExists =
                repo.existsByEmail(
                        student.getEmail()
                );

        if (emailExists) {
            throw new EmailAlreadyExistsException(
                    "Email already exists"
            );
        }


        // -------------------------------------------------
        // CHECK PASSWORD
        // -------------------------------------------------

        if (student.getPassword() == null
                || student.getPassword().isBlank()) {

            throw new IllegalArgumentException(
                    "Password is required"
            );
        }


        // -------------------------------------------------
        // PASSWORD ENCRYPTION
        // -------------------------------------------------

        student.setPassword(
                passwordEncoder.encode(
                        student.getPassword()
                )
        );


        // -------------------------------------------------
        // ROLE
        // -------------------------------------------------

        if (roleId != null
                && !roleId.isBlank()) {

            Long id;

            try {
                id = Long.parseLong(roleId);

            } catch (NumberFormatException e) {

                throw new IllegalArgumentException(
                        "Invalid role ID"
                );
            }

            Role role =
                    roleRepository.findById(id)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Role not found with id : "
                                                    + id
                                    )
                            );

            student.setRole(role);
        }


        // -------------------------------------------------
        // DEFAULT STATUS
        // -------------------------------------------------

        student.setStatus(
                StudentStatus.ACTIVE
        );


        // -------------------------------------------------
        // DEFAULT SOFT DELETE
        // -------------------------------------------------

        student.setIsDeleted("false");


        // -------------------------------------------------
        // PROFILE PHOTO
        // -------------------------------------------------

        boolean photoUploaded = false;

        if (profilePhoto != null
                && !profilePhoto.isEmpty()) {

            try {

                student.setProfilePhoto(
                        profilePhoto.getBytes()
                );

                photoUploaded = true;

            } catch (Exception e) {

                throw new RuntimeException(
                        "Unable to upload profile photo",
                        e
                );
            }
        }


        // -------------------------------------------------
        // SAVE
        // -------------------------------------------------

        Student savedStudent =
                repo.save(student);


        // -------------------------------------------------
        // AUDIT LOG - REGISTER
        // -------------------------------------------------

        String roleName = null;

        if (savedStudent.getRole() != null) {

            roleName =
                    savedStudent.getRole().getName();
        }

        saveAuditLog(

                "REGISTER",

                "New user registered: "
                        + savedStudent.getName()
                        + " (" + savedStudent.getEmail() + ")",

                savedStudent.getId(),

                savedStudent.getName(),

                savedStudent.getId(),

                savedStudent.getEmail(),

                roleName,

                ipAddress
        );


        // -------------------------------------------------
        // AUDIT LOG - PROFILE PHOTO
        // -------------------------------------------------

        if (photoUploaded) {

            saveAuditLog(

                    "PROFILE_PHOTO_UPDATE",

                    "Profile photo uploaded during registration",

                    savedStudent.getId(),

                    savedStudent.getName(),

                    savedStudent.getId(),

                    savedStudent.getEmail(),

                    roleName,

                    ipAddress
            );
        }

        return savedStudent;
    }


    // =====================================================
    // TOTAL USERS
    // =====================================================

    public long getTotalUsers() {
        return repo.count();
    }


    // =====================================================
    // TOTAL STUDENTS
    // =====================================================

    public long getTotalStudents() {

        return repo.countByRole_Name(
                "ROLE_STUDENT"
        );
    }


    // =====================================================
    // TOTAL ADMINS
    // =====================================================

    public long getTotalAdmins() {

        return repo.countByRole_Name(
                "ROLE_ADMIN"
        );
    }


    // =====================================================
    // ACTIVE USERS
    // =====================================================

    public long getActiveCount() {

        return repo.countByStatus(
                StudentStatus.ACTIVE
        );
    }


    // =====================================================
    // ACTIVE STUDENTS
    // =====================================================

    public long getActiveStudents() {

        return repo.countByRole_NameAndStatus(
                "ROLE_STUDENT",
                StudentStatus.ACTIVE
        );
    }


    // =====================================================
    // ACTIVE ADMINS
    // =====================================================

    public long getActiveAdmins() {

        return repo.countByRole_NameAndStatus(
                "ROLE_ADMIN",
                StudentStatus.ACTIVE
        );
    }


    // =====================================================
    // DEPARTMENT COUNT
    // =====================================================

    public long getDepartmentCount() {

        return repo.countDistinctDepartment();
    }


    // =====================================================
    // RECENT STUDENT COUNT
    // =====================================================

    public long getRecentStudentCount() {

        LocalDateTime date =
                LocalDateTime.now().minusDays(7);

        return repo.countByCreatedDateAfter(date);
    }


    // =====================================================
    // STUDENT COUNT BY DEPARTMENT
    // =====================================================

    public List<Object[]> getStudentCountByDepartment() {

        return repo.countStudentsByDepartment();
    }


    // =====================================================
    // STUDENT COUNT BY CITY
    // =====================================================

    public List<Object[]> getStudentCountByCity() {

        return repo.countStudentsByCity();
    }


    // =====================================================
    // GET ALL USERS
    // SEARCH + ROLE + PAGINATION + SORTING
    // =====================================================

    public Page<Student> getAllUsers(
            String keyword,
            String role,
            int page,
            int size,
            String sortField,
            String sortDir) {

        Sort sort =
                sortDir != null
                        && sortDir.equalsIgnoreCase("desc")
                        ? Sort.by(sortField).descending()
                        : Sort.by(sortField).ascending();

        Pageable pageable =
                PageRequest.of(page, size, sort);


        if (keyword != null
                && !keyword.trim().isEmpty()
                && role != null
                && !role.trim().isEmpty()) {

            return repo.findByKeywordAndRole(
                    keyword.trim(),
                    role,
                    pageable
            );
        }


        if (keyword != null
                && !keyword.trim().isEmpty()) {

            return repo.findByKeyword(
                    keyword.trim(),
                    pageable
            );
        }


        if (role != null
                && !role.trim().isEmpty()) {

            return repo.findByRole_Name(
                    role,
                    pageable
            );
        }


        return repo.findAll(pageable);
    }


    // =====================================================
    // GET ALL ADMINS
    // =====================================================

    public Page<Student> getAllAdmins(
            String keyword,
            int page,
            int size,
            String sortField,
            String sortDir) {

        Sort sort =
                sortDir != null
                        && sortDir.equalsIgnoreCase("desc")
                        ? Sort.by(sortField).descending()
                        : Sort.by(sortField).ascending();

        Pageable pageable =
                PageRequest.of(page, size, sort);


        if (keyword != null
                && !keyword.trim().isEmpty()) {

            return repo.findAdminsByKeyword(
                    keyword.trim(),
                    pageable
            );
        }


        return repo.findByRole_Name(
                "ROLE_ADMIN",
                pageable
        );
    }


    // =====================================================
    // GET ALL STUDENTS ONLY
    // =====================================================

    public Page<Student> getAllStudentsOnly(
            String keyword,
            int page,
            int size,
            String sortField,
            String sortDir) {

        Sort sort =
                sortDir != null
                        && sortDir.equalsIgnoreCase("desc")
                        ? Sort.by(sortField).descending()
                        : Sort.by(sortField).ascending();

        Pageable pageable =
                PageRequest.of(page, size, sort);


        if (keyword != null
                && !keyword.trim().isEmpty()) {

            return repo.findStudentsByKeyword(
                    keyword.trim(),
                    pageable
            );
        }


        return repo.findByRole_Name(
                "ROLE_STUDENT",
                pageable
        );
    }


    // =====================================================
    // GET ALL STUDENTS ONLY - EXPORT
    // =====================================================

    public List<Student> getAllStudentsOnly() {

        return repo.findByRole_Name(
                "ROLE_STUDENT"
        );
    }


    // =====================================================
    // GET ALL ADMINS - EXPORT
    // =====================================================

    public List<Student> getAllAdmins() {

        return repo.findByRole_Name(
                "ROLE_ADMIN"
        );
    }


    // =====================================================
    // GET RECENT STUDENTS
    // PAGINATION + SEARCH + SORTING
    // =====================================================

    public Page<Student> getRecentStudents(
            String keyword,
            int page,
            int size,
            String sortField,
            String sortDir) {

        Sort sort =
                sortDir != null
                        && sortDir.equalsIgnoreCase("desc")
                        ? Sort.by(sortField).descending()
                        : Sort.by(sortField).ascending();

        Pageable pageable =
                PageRequest.of(page, size, sort);


        if (keyword != null
                && !keyword.trim().isEmpty()) {

            return repo.findRecentStudentsByKeyword(
                    keyword.trim(),
                    pageable
            );
        }


        return repo.findRecentStudents(pageable);
    }


    // =====================================================
    // GET RECENT STUDENTS - EXPORT
    // =====================================================

    public List<Student> getRecentStudents() {

        return repo.findRecentStudents();
    }


    // =====================================================
    // SOFT DELETE
    // ADMIN DELETE STUDENT
    // =====================================================

    public void softDelete(
            Long id,
            String ipAddress,
            Authentication authentication) {

        Student student =
                getStudentById(id);

        student.setIsDeleted("true");

        student.setStatus(
                StudentStatus.INACTIVE
        );

        repo.save(student);


        // -------------------------------------------------
        // AUDIT LOG
        // -------------------------------------------------

        saveAuditLog(

                "DELETE_STUDENT",

                "Admin deleted student profile",

                student.getId(),

                student.getName(),

                student.getId(),

                getAuthenticationUsername(
                        authentication,
                        student.getEmail()
                ),

                getAuthenticationRole(authentication),

                ipAddress
        );
    }


    // =====================================================
    // DELETE OWN PROFILE
    // USED BY ADMIN / STUDENT
    // =====================================================

    public void deleteOwnProfile(
            String email,
            String ipAddress,
            Authentication authentication) {

        Student student =
                repo.findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                )
                        );


        student.setIsDeleted("true");

        student.setStatus(
                StudentStatus.INACTIVE
        );

        repo.save(student);


        String role =
                getStudentRole(student);


        // -------------------------------------------------
        // ADMIN DELETE OWN PROFILE
        // -------------------------------------------------

        if ("ROLE_ADMIN".equalsIgnoreCase(role)) {

            saveAuditLog(

                    "DELETE_OWN_PROFILE",

                    "Admin deleted own profile",

                    student.getId(),

                    student.getName(),

                    student.getId(),

                    getAuthenticationUsername(
                            authentication,
                            student.getEmail()
                    ),

                    role,

                    ipAddress
            );

        }

        // -------------------------------------------------
        // STUDENT DELETE OWN PROFILE
        // -------------------------------------------------

        else {

            saveAuditLog(

                    "DELETE_OWN_PROFILE",

                    "Student deleted own profile",

                    student.getId(),

                    student.getName(),

                    student.getId(),

                    getAuthenticationUsername(
                            authentication,
                            student.getEmail()
                    ),

                    role,

                    ipAddress
            );
        }
    }


    // =====================================================
    // UPDATE USER BY ADMIN
    // =====================================================

    public Student updateUserByAdmin(
            Long id,
            Student updatedStudent,
            String ipAddress,
            Authentication authentication) {

        Student existingStudent =
                getStudentById(id);

        boolean profilePhotoChanged = false;


        // -------------------------------------------------
        // NAME
        // -------------------------------------------------

        if (updatedStudent.getName() != null) {

            existingStudent.setName(
                    updatedStudent.getName()
            );
        }


        // -------------------------------------------------
        // AGE
        // -------------------------------------------------

        existingStudent.setAge(
                updatedStudent.getAge()
        );


        // -------------------------------------------------
        // CONTACT
        // -------------------------------------------------

        if (updatedStudent.getContactNo() != null) {

            existingStudent.setContactNo(
                    updatedStudent.getContactNo()
            );
        }


        // -------------------------------------------------
        // DEPARTMENT
        // -------------------------------------------------

        if (updatedStudent.getDepartment() != null) {

            existingStudent.setDepartment(
                    updatedStudent.getDepartment()
            );
        }


        // -------------------------------------------------
        // CITY
        // -------------------------------------------------

        if (updatedStudent.getCity() != null) {

            existingStudent.setCity(
                    updatedStudent.getCity()
            );
        }


        // -------------------------------------------------
        // ADDRESS
        // -------------------------------------------------

        if (updatedStudent.getAddress() != null) {

            existingStudent.setAddress(
                    updatedStudent.getAddress()
            );
        }


        // -------------------------------------------------
        // STATUS
        // -------------------------------------------------

        if (updatedStudent.getStatus() != null) {

            existingStudent.setStatus(
                    updatedStudent.getStatus()
            );
        }


        // -------------------------------------------------
        // PROFILE PHOTO
        // -------------------------------------------------

        if (updatedStudent.getProfilePhoto() != null) {

            existingStudent.setProfilePhoto(
                    updatedStudent.getProfilePhoto()
            );

            profilePhotoChanged = true;
        }


        // -------------------------------------------------
        // SAVE
        // -------------------------------------------------

        Student savedStudent =
                repo.save(existingStudent);


        // -------------------------------------------------
        // AUDIT LOG - ADMIN UPDATE
        // -------------------------------------------------

        saveAuditLog(

                "UPDATE_USER",

                "Admin updated user profile successfully",

                savedStudent.getId(),

                savedStudent.getName(),

                savedStudent.getId(),

                getAuthenticationUsername(
                        authentication,
                        savedStudent.getEmail()
                ),

                getAuthenticationRole(authentication),

                ipAddress
        );


        // -------------------------------------------------
        // AUDIT LOG - PROFILE PHOTO
        // -------------------------------------------------

        if (profilePhotoChanged) {

            saveAuditLog(

                    "PROFILE_PHOTO_UPDATE",

                    "Admin uploaded photo successfully",

                    savedStudent.getId(),

                    savedStudent.getName(),

                    savedStudent.getId(),

                    getAuthenticationUsername(
                            authentication,
                            savedStudent.getEmail()
                    ),

                    getAuthenticationRole(authentication),

                    ipAddress
            );
        }


        return savedStudent;
    }


    // =====================================================
    // UPDATE PROFILE
    // USED BY STUDENT AND ADMIN FOR OWN PROFILE
    // =====================================================

    public Student updateProfile(
            String email,
            Student updatedStudent,
            MultipartFile profilePhoto,
            String ipAddress) {

        Student student =
                repo.findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Student not found with email : "
                                                + email
                                )
                        );


        boolean profilePhotoChanged = false;


        // -------------------------------------------------
        // NAME
        // -------------------------------------------------

        if (updatedStudent.getName() != null) {

            student.setName(
                    updatedStudent.getName()
            );
        }


        // -------------------------------------------------
        // AGE
        // -------------------------------------------------

        student.setAge(
                updatedStudent.getAge()
        );


        // -------------------------------------------------
        // CONTACT
        // -------------------------------------------------

        if (updatedStudent.getContactNo() != null) {

            student.setContactNo(
                    updatedStudent.getContactNo()
            );
        }


        // -------------------------------------------------
        // DEPARTMENT
        // -------------------------------------------------

        if (updatedStudent.getDepartment() != null) {

            student.setDepartment(
                    updatedStudent.getDepartment()
            );
        }


        // -------------------------------------------------
        // CITY
        // -------------------------------------------------

        if (updatedStudent.getCity() != null) {

            student.setCity(
                    updatedStudent.getCity()
            );
        }


        // -------------------------------------------------
        // ADDRESS
        // -------------------------------------------------

        if (updatedStudent.getAddress() != null) {

            student.setAddress(
                    updatedStudent.getAddress()
            );
        }


        // -------------------------------------------------
        // PROFILE PHOTO
        // Student.profilePhoto = byte[]
        // -------------------------------------------------

        if (profilePhoto != null
                && !profilePhoto.isEmpty()) {

            try {

                student.setProfilePhoto(
                        profilePhoto.getBytes()
                );

                profilePhotoChanged = true;

            } catch (Exception e) {

                throw new RuntimeException(
                        "Unable to upload profile photo",
                        e
                );
            }
        }


        // -------------------------------------------------
        // SAVE
        // -------------------------------------------------

        Student savedStudent =
                repo.save(student);


        // -------------------------------------------------
        // GET ROLE
        // -------------------------------------------------

        String role =
                getStudentRole(savedStudent);


        // -------------------------------------------------
        // AUDIT LOG - OWN PROFILE UPDATE
        // -------------------------------------------------

        String profileDescription;

        if ("ROLE_ADMIN".equalsIgnoreCase(role)) {

            profileDescription =
                    "Admin updated profile successfully";

        } else {

            profileDescription =
                    "Student updated profile successfully";
        }


        saveAuditLog(

                "UPDATE_PROFILE",

                profileDescription,

                savedStudent.getId(),

                savedStudent.getName(),

                savedStudent.getId(),

                savedStudent.getEmail(),

                role,

                ipAddress
        );


        // -------------------------------------------------
        // AUDIT LOG - OWN PROFILE PHOTO
        // -------------------------------------------------

        if (profilePhotoChanged) {

            String photoDescription;

            if ("ROLE_ADMIN".equalsIgnoreCase(role)) {

                photoDescription =
                        "Admin uploaded photo successfully";

            } else {

                photoDescription =
                        "Student uploaded photo successfully";
            }


            saveAuditLog(

                    "PROFILE_PHOTO_UPDATE",

                    photoDescription,

                    savedStudent.getId(),

                    savedStudent.getName(),

                    savedStudent.getId(),

                    savedStudent.getEmail(),

                    role,

                    ipAddress
            );
        }


        return savedStudent;
    }


    // =====================================================
    // CHANGE PASSWORD
    // =====================================================

    public String changePassword(
            String email,
            String oldPassword,
            String newPassword,
            String ipAddress) {

        Student student =
                repo.findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Student not found"
                                )
                        );


        // -------------------------------------------------
        // CHECK OLD PASSWORD
        // -------------------------------------------------

        if (student.getPassword() == null
                || !passwordEncoder.matches(
                        oldPassword,
                        student.getPassword())) {

            return null;
        }


        // -------------------------------------------------
        // CHECK NEW PASSWORD IS DIFFERENT
        // -------------------------------------------------

        if (passwordEncoder.matches(
                newPassword,
                student.getPassword())) {

            return "New password must be different from current password";
        }


        // -------------------------------------------------
        // SAVE NEW PASSWORD
        // -------------------------------------------------

        student.setPassword(
                passwordEncoder.encode(newPassword)
        );

        Student savedStudent =
                repo.save(student);


        // -------------------------------------------------
        // GET ROLE
        // -------------------------------------------------

        String role =
                getStudentRole(savedStudent);


        // -------------------------------------------------
        // AUDIT LOG - PASSWORD CHANGE
        // -------------------------------------------------

        String passwordDescription;

        if ("ROLE_ADMIN".equalsIgnoreCase(role)) {

            passwordDescription =
                    "Admin changed password successfully";

        } else {

            passwordDescription =
                    "Student changed password successfully";
        }


        saveAuditLog(

                "PASSWORD_CHANGE",

                passwordDescription,

                savedStudent.getId(),

                savedStudent.getName(),

                savedStudent.getId(),

                savedStudent.getEmail(),

                role,

                ipAddress
        );


        // -------------------------------------------------
        // RETURN MESSAGE
        // -------------------------------------------------

        if (savedStudent.getRole() != null
                && savedStudent.getRole().getName() != null
                && "ROLE_ADMIN".equalsIgnoreCase(
                        savedStudent.getRole().getName())) {

            return "Admin password changed successfully";
        }


        return "Student password changed successfully";
    }


    // =====================================================
    // SAVE AUDIT LOG
    // CENTRAL METHOD
    // =====================================================

    private void saveAuditLog(
            String action,
            String description,
            Long entityId,
            String entityName,
            Long studentId,
            String username,
            String role,
            String ipAddress) {

        try {

            AuditLog auditLog =
                    new AuditLog();

            auditLog.setAction(action);

            auditLog.setDescription(
                    description
            );

            auditLog.setEntityId(
                    entityId
            );

            auditLog.setEntityName(
                    entityName
            );

            auditLog.setStudentId(
                    studentId
            );

            auditLog.setUsername(
                    username
            );

            auditLog.setRole(
                    role
            );

            auditLog.setIpAddress(
                    ipAddress
            );

            auditLog.setCreatedAt(
                    LocalDateTime.now()
            );

            auditLogRepository.save(
                    auditLog
            );

        } catch (Exception e) {

            System.err.println(
                    "Unable to save audit log: "
                            + e.getMessage()
            );
        }
    }


    // =====================================================
    // GET STUDENT ROLE
    // =====================================================

    private String getStudentRole(
            Student student) {

        if (student == null
                || student.getRole() == null
                || student.getRole().getName() == null) {

            return null;
        }

        return student.getRole().getName();
    }


    // =====================================================
    // GET AUTHENTICATION USERNAME
    // =====================================================

    private String getAuthenticationUsername(
            Authentication authentication,
            String defaultUsername) {

        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getName() != null) {

            return authentication.getName();
        }

        return defaultUsername;
    }


    // =====================================================
    // GET AUTHENTICATION ROLE
    // =====================================================

    private String getAuthenticationRole(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getAuthorities() == null) {

            return null;
        }

        return authentication.getAuthorities()
                .stream()
                .findFirst()
                .map(authority ->
                        authority.getAuthority())
                .orElse(null);
    }

}