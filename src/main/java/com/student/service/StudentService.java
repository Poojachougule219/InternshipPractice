package com.student.service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.student.entity.Role;
import com.student.entity.Student;
import com.student.enums.StudentStatus;
import com.student.exception.EmailAlreadyExistsException;
import com.student.exception.ResourceNotFoundException;
import com.student.repository.RoleRepository;
import com.student.repository.StudentRepository;

@Service
public class StudentService {

    @Autowired
    private StudentRepository repository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditLogService auditLogService;


 // =====================================================
 // ADD STUDENT
 // =====================================================

 public Student addStudent(
         Student student,
         String ipAddress,
         Authentication authentication) {

     // =================================================
     // ENCODE PASSWORD
     // =================================================

     if (student.getPassword() != null
             && !student.getPassword().startsWith("$2a$")
             && !student.getPassword().startsWith("$2b$")
             && !student.getPassword().startsWith("$2y$")) {

         student.setPassword(
                 passwordEncoder.encode(
                         student.getPassword()
                 )
         );
     }


     // =================================================
     // LOAD ROLE
     // =================================================

     if (student.getRole() != null
             && student.getRole().getId() != null) {

         Role role =
                 roleRepository.findById(
                         student.getRole().getId()
                 ).orElseThrow(() ->
                         new RuntimeException(
                                 "Role not found"
                         ));

         student.setRole(role);
     }


     // =================================================
     // DEFAULT STATUS
     // =================================================

     student.setStatus(
             StudentStatus.ACTIVE
     );

     student.setIsDeleted(
             "false"
     );


     // =================================================
     // SAVE STUDENT
     // =================================================

     Student savedStudent =
             repository.save(student);


     // =================================================
     // GET LOGGED-IN USER
     // THIS IS THE PERSON WHO PERFORMED THE ACTION
     // =================================================

     String performedBy = "SYSTEM";

     if (authentication != null
             && authentication.getName() != null
             && !authentication.getName().trim().isEmpty()) {

         performedBy =
                 authentication.getName();
     }


     // =================================================
     // GET LOGGED-IN USER ROLE
     // =================================================

     String performedByRole = "ROLE_ADMIN";

     if (authentication != null
             && authentication.getAuthorities() != null) {

         performedByRole =
                 authentication.getAuthorities()
                         .stream()
                         .map(authority ->
                                 authority.getAuthority())
                         .filter(role ->
                                 role.equals("ROLE_ADMIN")
                                 || role.equals("ROLE_STUDENT"))
                         .findFirst()
                         .orElse("ROLE_ADMIN");
     }


     // =================================================
     // ENTITY NAME
     // THIS IS THE USER WHO WAS CREATED
     // =================================================

     String entityName;

     if (savedStudent.getRole() != null
             && savedStudent.getRole().getName() != null
             && "ROLE_ADMIN".equalsIgnoreCase(
                     savedStudent.getRole().getName())) {

         entityName = "ADMIN";

     } else if (savedStudent.getRole() != null
             && savedStudent.getRole().getName() != null
             && "ROLE_STUDENT".equalsIgnoreCase(
                     savedStudent.getRole().getName())) {

         entityName = "STUDENT";

     } else {

         entityName = "USER";
     }


     // =================================================
     // DESCRIPTION
     // =================================================

     String message;

     if ("STUDENT".equals(entityName)) {

         message =
                 "Student created successfully";

     } else if ("ADMIN".equals(entityName)) {

         message =
                 "Admin created successfully";

     } else {

         message =
                 "User created successfully";
     }


     // =================================================
     // AUDIT LOG
     // =================================================
     // performedBy       = logged-in admin
     // performedByRole   = ROLE_ADMIN
     // entityId          = created student's ID
     // entityName        = STUDENT
     // =================================================

     auditLogService.createLog(
             savedStudent.getId(),
             performedBy,
             performedByRole,
             "CREATE",
             entityName,
             savedStudent.getId(),
             message,
             ipAddress
     );


     return savedStudent;
 }
 
 

    // =====================================================
    // GET ALL ACTIVE USERS
    // =====================================================

    public List<Student> getAllStudents() {

        return repository.findByStatusAndIsDeleted(
                StudentStatus.ACTIVE,
                "false"
        );
    }


    // =====================================================
    // GET USER BY ID
    // =====================================================

    public Student getStudentById(Long id) {

        return repository.findByIdAndIsDeleted(
                id,
                "false"
        ).orElseThrow(() ->
                new ResourceNotFoundException(
                        "Student not found with id : " + id
                ));
    }


    // =====================================================
    // UPDATE USER
    // =====================================================

    public Student updateStudent(
            Long id,
            Student student,
            String ipAddress) {

        Student existing =
                getStudentById(id);


        if (!existing.getEmail().equals(
                student.getEmail())) {

            if (repository.existsByEmailAndStatusAndIsDeleted(
                    student.getEmail(),
                    StudentStatus.ACTIVE,
                    "false")) {

                throw new EmailAlreadyExistsException(
                        "Email already exists"
                );
            }
        }


        existing.setName(
                student.getName()
        );

        existing.setAge(
                student.getAge()
        );

        existing.setDepartment(
                student.getDepartment()
        );

        existing.setEmail(
                student.getEmail()
        );

        existing.setContactNo(
                student.getContactNo()
        );

        existing.setCity(
                student.getCity()
        );

        existing.setAddress(
                student.getAddress()
        );


        Student updatedStudent =
                repository.save(existing);


        // =================================================
        // ROLE
        // =================================================

        String roleName = "ROLE_STUDENT";

        if (updatedStudent.getRole() != null
                && updatedStudent.getRole().getName() != null) {

            roleName =
                    updatedStudent.getRole().getName();
        }


        // =================================================
        // ENTITY
        // =================================================

        String entityName =
                "ROLE_ADMIN".equalsIgnoreCase(roleName)
                        ? "ADMIN"
                        : "STUDENT";


        // =================================================
        // AUDIT
        // =================================================

        auditLogService.createLog(
                updatedStudent.getId(),
                updatedStudent.getEmail(),
                roleName,
                "UPDATE",
                entityName,
                updatedStudent.getId(),
                "Student updated successfully",
                ipAddress
        );


        return updatedStudent;
    }


    // =====================================================
    // DELETE STUDENT
    // =====================================================

    public void deleteStudent(Long id, String ipAddress) {

        Student student =
                getStudentById(id);

        student.setIsDeleted("true");

        student.setStatus(
                StudentStatus.INACTIVE
        );


        Student deletedStudent =
                repository.save(student);


        String roleName = "ROLE_STUDENT";

        if (deletedStudent.getRole() != null
                && deletedStudent.getRole().getName() != null) {

            roleName =
                    deletedStudent.getRole().getName();
        }


        String entityName;

        if ("ROLE_ADMIN".equalsIgnoreCase(roleName)) {

            entityName = "ADMIN";

        } else if ("ROLE_STUDENT".equalsIgnoreCase(roleName)) {

            entityName = "STUDENT";

        } else {

            entityName = "USER";
        }


        auditLogService.createLog(
                deletedStudent.getId(),
                deletedStudent.getEmail(),
                roleName,
                "DELETE",
                entityName,
                deletedStudent.getId(),
                "Student deleted successfully",
                ipAddress
        );
    }


 // =====================================================
 // REGISTER STUDENT / ADMIN
 // =====================================================

 public Student signup(
         Student student,
         String roleId,
         MultipartFile profilePhoto,
         String ipAddress,
         Authentication authentication)
         throws IOException {

     // =================================================
     // CHECK EMAIL
     // =================================================

     if (repository.findByEmailAndStatusAndIsDeleted(
             student.getEmail(),
             StudentStatus.ACTIVE,
             "false"
     ).isPresent()) {

         throw new EmailAlreadyExistsException(
                 "Email already exists"
         );
     }


     // =================================================
     // CHECK ROLE
     // =================================================

     if (roleId == null
             || roleId.trim().isEmpty()) {

         throw new RuntimeException(
                 "Role is required"
         );
     }


     Long roleIdValue;

     try {

         roleIdValue =
                 Long.parseLong(roleId);

     } catch (NumberFormatException e) {

         throw new RuntimeException(
                 "Invalid role"
         );
     }


     // =================================================
     // LOAD ROLE
     // =================================================

     Role role =
             roleRepository.findById(
                     roleIdValue
             ).orElseThrow(() ->
                     new RuntimeException(
                             "Role not found"
                     ));


     student.setRole(role);


     // =================================================
     // PROFILE PHOTO
     // =================================================

     if (profilePhoto != null
             && !profilePhoto.isEmpty()) {

         String contentType =
                 profilePhoto.getContentType();


         if (contentType == null
                 || !(contentType.equalsIgnoreCase(
                             "image/jpeg")
                 || contentType.equalsIgnoreCase(
                             "image/jpg")
                 || contentType.equalsIgnoreCase(
                             "image/png"))) {

             throw new RuntimeException(
                     "Only JPG, JPEG and PNG images are allowed."
             );
         }


         long maxFileSize =
                 5L * 1024L * 1024L;


         if (profilePhoto.getSize()
                 > maxFileSize) {

             throw new RuntimeException(
                     "Profile photo must be less than 5 MB."
             );
         }


         // =================================================
         // FOLDER
         // =================================================

         String folderName;

         if ("ROLE_ADMIN".equalsIgnoreCase(
                 role.getName())) {

             folderName =
                     "Admin photos";

         } else if ("ROLE_STUDENT".equalsIgnoreCase(
                 role.getName())) {

             folderName =
                     "Student photos";

         } else {

             throw new RuntimeException(
                     "Invalid role for profile photo"
             );
         }


         String uploadDirectory =
                 new File(
                         "src/main/resources/static/uploads/Profile/"
                                 + folderName
                 ).getAbsolutePath();


         File directory =
                 new File(uploadDirectory);


         if (!directory.exists()) {

             if (!directory.mkdirs()) {

                 throw new IOException(
                         "Unable to create upload directory"
                 );
             }
         }


         // =================================================
         // EXTENSION
         // =================================================

         String originalFileName =
                 profilePhoto.getOriginalFilename();

         String extension = "";


         if (originalFileName != null
                 && originalFileName.contains(".")) {

             extension =
                     originalFileName.substring(
                             originalFileName.lastIndexOf(".")
                     ).toLowerCase();
         }


         // =================================================
         // UNIQUE NAME
         // =================================================

         String fileName =
                 UUID.randomUUID()
                         .toString()
                         + extension;


         File saveFile =
                 new File(
                         directory,
                         fileName
                 );


         profilePhoto.transferTo(
                 saveFile
         );


         student.setProfilePhoto(
                 fileName
         );
     }


     // =================================================
     // PASSWORD
     // =================================================

     student.setPassword(
             passwordEncoder.encode(
                     student.getPassword()
             )
     );


     student.setStatus(
             StudentStatus.ACTIVE
     );

     student.setIsDeleted(
             "false"
     );


     // =================================================
     // SAVE
     // =================================================

     Student savedStudent =
             repository.save(student);


     // =================================================
     // CREATED USER ROLE
     // =================================================

     String createdUserRole =
             "ROLE_STUDENT";

     if (savedStudent.getRole() != null
             && savedStudent.getRole().getName() != null) {

         createdUserRole =
                 savedStudent.getRole().getName();
     }


     // =================================================
     // WHO PERFORMED THE ACTION
     // =================================================

     String performedBy =
             "SYSTEM";

     String performedByRole =
             "SYSTEM";


     if (authentication != null) {

         // Logged-in user's email
         if (authentication.getName() != null
                 && !authentication.getName().trim().isEmpty()) {

             performedBy =
                     authentication.getName();
         }


         // Logged-in user's role
         if (authentication.getAuthorities() != null) {

             performedByRole =
                     authentication.getAuthorities()
                             .stream()
                             .map(authority ->
                                     authority.getAuthority())
                             .filter(roleName ->
                                     roleName != null
                                     && roleName.startsWith("ROLE_"))
                             .findFirst()
                             .orElse("SYSTEM");
         }
     }


     // =================================================
     // ENTITY NAME
     // =================================================

     String entityName;

     if ("ROLE_ADMIN".equalsIgnoreCase(
             createdUserRole)) {

         entityName =
                 "ADMIN";

     } else if ("ROLE_STUDENT".equalsIgnoreCase(
             createdUserRole)) {

         entityName =
                 "STUDENT";

     } else {

         entityName =
                 "USER";
     }


     // =================================================
     // MESSAGE
     // =================================================

     String registrationMessage;

     if ("ROLE_ADMIN".equalsIgnoreCase(
             createdUserRole)) {

         registrationMessage =
                 "New admin registered successfully";

     } else if ("ROLE_STUDENT".equalsIgnoreCase(
             createdUserRole)) {

         registrationMessage =
                 "New student registered successfully";

     } else {

         registrationMessage =
                 "New user registered successfully";
     }


     // =================================================
     // AUDIT LOG
     // =================================================
     //
     // IMPORTANT:
     //
     // username  = logged-in user who performed action
     // role      = logged-in user's role
     // entity    = user who was created
     //
     // =================================================

     auditLogService.createLog(
             savedStudent.getId(),
             performedBy,
             performedByRole,
             "REGISTRATION",
             entityName,
             savedStudent.getId(),
             registrationMessage,
             ipAddress
     );


     return savedStudent;
 }

    // =====================================================
    // GET USER BY EMAIL
    // =====================================================

    public Student getStudentByEmail(
            String email) {

        return repository
                .findByEmailAndStatusAndIsDeleted(
                        email,
                        StudentStatus.ACTIVE,
                        "false"
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Student not found with email : "
                                        + email
                        ));
    }


    // =====================================================
    // SAVE STUDENT
    // =====================================================

    public Student saveStudent(
            Student student) {

        if (student.getPassword() != null
                && !student.getPassword().startsWith("$2a$")
                && !student.getPassword().startsWith("$2b$")
                && !student.getPassword().startsWith("$2y$")) {

            student.setPassword(
                    passwordEncoder.encode(
                            student.getPassword()
                    )
            );
        }


        if (student.getStatus() == null) {

            student.setStatus(
                    StudentStatus.ACTIVE
            );
        }


        student.setIsDeleted(
                "false"
        );


        return repository.save(student);
    }


    
 // =====================================================
 // UPDATE PROFILE + PHOTO
 // =====================================================

 public void updateProfile(
         String email,
         Student updatedStudent,
         MultipartFile profilePhoto,
         String ipAddress)
         throws IOException {

     Student student =
             repository.findByEmailAndStatusAndIsDeleted(
                     email,
                     StudentStatus.ACTIVE,
                     "false"
             )
             .orElseThrow(() ->
                     new ResourceNotFoundException(
                             "Student not found"
                     ));

     // =================================================
     // CHECK WHETHER PROFILE DETAILS ARE CHANGED
     // =================================================

     boolean profileChanged = false;

     if (!java.util.Objects.equals(
             student.getName(),
             updatedStudent.getName())) {

         profileChanged = true;
     }

     if (!java.util.Objects.equals(
             student.getAge(),
             updatedStudent.getAge())) {

         profileChanged = true;
     }

     if (!java.util.Objects.equals(
             student.getDepartment(),
             updatedStudent.getDepartment())) {

         profileChanged = true;
     }

     if (!java.util.Objects.equals(
             student.getCity(),
             updatedStudent.getCity())) {

         profileChanged = true;
     }

     if (!java.util.Objects.equals(
             student.getContactNo(),
             updatedStudent.getContactNo())) {

         profileChanged = true;
     }

     if (!java.util.Objects.equals(
             student.getAddress(),
             updatedStudent.getAddress())) {

         profileChanged = true;
     }

     // =================================================
     // UPDATE PROFILE DETAILS
     // =================================================

     student.setName(
             updatedStudent.getName()
     );

     student.setAge(
             updatedStudent.getAge()
     );

     student.setDepartment(
             updatedStudent.getDepartment()
     );

     student.setCity(
             updatedStudent.getCity()
     );

     student.setContactNo(
             updatedStudent.getContactNo()
     );

     student.setAddress(
             updatedStudent.getAddress()
     );

     // =================================================
     // CHECK WHETHER PHOTO IS UPLOADED
     // =================================================

     boolean photoChanged =
             profilePhoto != null
                     && !profilePhoto.isEmpty();

     // =================================================
     // NEW PHOTO
     // =================================================

     if (photoChanged) {

         String contentType =
                 profilePhoto.getContentType();

         if (contentType == null
                 || !(contentType.equalsIgnoreCase(
                             "image/jpeg")
                 || contentType.equalsIgnoreCase(
                             "image/jpg")
                 || contentType.equalsIgnoreCase(
                             "image/png"))) {

             throw new RuntimeException(
                     "Only JPG, JPEG and PNG images are allowed."
             );
         }

         long maxFileSize =
                 5L * 1024L * 1024L;

         if (profilePhoto.getSize()
                 > maxFileSize) {

             throw new RuntimeException(
                     "Profile photo must be less than 5 MB."
             );
         }

         // =================================================
         // FOLDER
         // =================================================

         String folderName;

         if (student.getRole() != null
                 && "ROLE_ADMIN".equalsIgnoreCase(
                         student.getRole().getName())) {

             folderName =
                     "Admin photos";

         } else if (student.getRole() != null
                 && "ROLE_STUDENT".equalsIgnoreCase(
                         student.getRole().getName())) {

             folderName =
                     "Student photos";

         } else {

             throw new RuntimeException(
                     "Invalid user role."
             );
         }

         String uploadDirectory =
                 new File(
                         "src/main/resources/static/uploads/Profile/"
                                 + folderName
                 ).getAbsolutePath();

         File directory =
                 new File(uploadDirectory);

         if (!directory.exists()) {

             if (!directory.mkdirs()) {

                 throw new IOException(
                         "Unable to create upload directory"
                 );
             }
         }

         // =================================================
         // DELETE OLD PHOTO
         // =================================================

         String oldPhoto =
                 student.getProfilePhoto();

         if (oldPhoto != null
                 && !oldPhoto.trim().isEmpty()) {

             File oldFile =
                     new File(
                             directory,
                             oldPhoto
                     );

             if (oldFile.exists()) {

                 oldFile.delete();
             }
         }

         // =================================================
         // GET EXTENSION
         // =================================================

         String originalFileName =
                 profilePhoto.getOriginalFilename();

         String extension = "";

         if (originalFileName != null
                 && originalFileName.contains(".")) {

             extension =
                     originalFileName.substring(
                             originalFileName.lastIndexOf(".")
                     ).toLowerCase();
         }

         // =================================================
         // CREATE UNIQUE FILE NAME
         // =================================================

         String fileName =
                 UUID.randomUUID()
                         .toString()
                         + extension;

         File saveFile =
                 new File(
                         directory,
                         fileName
                 );

         profilePhoto.transferTo(
                 saveFile
         );

         student.setProfilePhoto(
                 fileName
         );
     }

     // =================================================
     // SAVE
     // =================================================

     Student updated =
             repository.save(student);

     // =================================================
     // ROLE
     // =================================================

     String roleName = "ROLE_STUDENT";

     if (updated.getRole() != null
             && updated.getRole().getName() != null) {

         roleName =
                 updated.getRole().getName();
     }

     // =================================================
     // ENTITY NAME
     // =================================================

     String entityName =
             "ROLE_ADMIN".equalsIgnoreCase(roleName)
                     ? "ADMIN"
                     : "STUDENT";

     // =================================================
     // PROFILE UPDATE AUDIT
     // =================================================

     if (profileChanged) {

         String profileMessage =
                 "ROLE_ADMIN".equalsIgnoreCase(roleName)
                         ? "Admin profile updated successfully"
                         : "Student profile updated successfully";

         auditLogService.createLog(
                 updated.getId(),
                 updated.getEmail(),
                 roleName,
                 "PROFILE_UPDATE",
                 entityName,
                 updated.getId(),
                 profileMessage,
                 ipAddress
         );
     }

     // =================================================
     // PHOTO UPDATE AUDIT
     // =================================================

     if (photoChanged) {

         String photoMessage =
                 "ROLE_ADMIN".equalsIgnoreCase(roleName)
                         ? "Admin profile photo updated successfully"
                         : "Student profile photo updated successfully";

         auditLogService.createLog(
                 updated.getId(),
                 updated.getEmail(),
                 roleName,
                 "PHOTO_UPDATE",
                 entityName,
                 updated.getId(),
                 photoMessage,
                 ipAddress
         );
     }
 }
 
 

    // =====================================================
    // DASHBOARD COUNTS
    // =====================================================

    public long getTotalUsers() {

        return repository.countByIsDeleted(
                "false"
        );
    }


    public long getTotalStudents() {

        return repository
                .countByRole_NameAndIsDeleted(
                        "ROLE_STUDENT",
                        "false"
                );
    }


    public long getTotalAdmins() {

        return repository
                .countByRole_NameAndIsDeleted(
                        "ROLE_ADMIN",
                        "false"
                );
    }


    public long getActiveCount() {

        return repository
                .countByStatusAndIsDeleted(
                        StudentStatus.ACTIVE,
                        "false"
                );
    }


    public long getActiveStudents() {

        return repository
                .countByRole_NameAndStatusAndIsDeleted(
                        "ROLE_STUDENT",
                        StudentStatus.ACTIVE,
                        "false"
                );
    }


    public long getActiveAdmins() {

        return repository
                .countByRole_NameAndStatusAndIsDeleted(
                        "ROLE_ADMIN",
                        StudentStatus.ACTIVE,
                        "false"
                );
    }


    // =====================================================
    // DEPARTMENT
    // =====================================================

    public long getDepartmentCount() {

        return repository.countDistinctDepartments();
    }


    public List<Object[]> getStudentCountByDepartment() {

        return repository.getStudentCountByDepartment();
    }


    // =====================================================
    // CITY
    // =====================================================

    public List<Object[]> getStudentCountByCity() {

        return repository.getStudentCountByCity();

    }

    
    
 // =====================================================
 // CHANGE PASSWORD
 // =====================================================

 public String changePassword(
         String email,
         String currentPassword,
         String newPassword,
         String ipAddress) {

     Student student =
             repository.findByEmailAndStatusAndIsDeleted(
                     email,
                     StudentStatus.ACTIVE,
                     "false"
             )
             .orElse(null);

     if (student == null) {
         return null;
     }

     // =================================================
     // CHECK CURRENT PASSWORD
     // =================================================

     if (!passwordEncoder.matches(
             currentPassword,
             student.getPassword())) {

         return null;
     }

     // =================================================
     // UPDATE PASSWORD
     // =================================================

     student.setPassword(
             passwordEncoder.encode(newPassword)
     );

     Student updatedStudent =
             repository.save(student);

     // =================================================
     // ROLE
     // =================================================

     String roleName = "ROLE_STUDENT";

     if (updatedStudent.getRole() != null
             && updatedStudent.getRole().getName() != null) {

         roleName =
                 updatedStudent.getRole().getName();
     }

     // =================================================
     // MESSAGE
     // =================================================

     String message;

     if ("ROLE_ADMIN".equalsIgnoreCase(roleName)) {

         message =
                 "Admin password changed successfully";

     } else {

         message =
                 "Student password changed successfully";
     }

     // =================================================
     // ENTITY NAME
     // =================================================

     String entityName;

     if ("ROLE_ADMIN".equalsIgnoreCase(roleName)) {

         entityName = "ADMIN";

     } else if ("ROLE_STUDENT".equalsIgnoreCase(roleName)) {

         entityName = "STUDENT";

     } else {

         entityName = "USER";
     }

     // =================================================
     // AUDIT LOG
     // =================================================

     auditLogService.createLog(
             updatedStudent.getId(),
             updatedStudent.getEmail(),
             roleName,
             "PASSWORD_CHANGE",
             entityName,
             updatedStudent.getId(),
             message,
             ipAddress
     );

     return message;
 }
 
 
 

    // =====================================================
    // SEARCH BY NAME
    // =====================================================

    public List<Student> searchByName(
            String name) {

        return repository
                .findByNameContainingIgnoreCaseAndStatusAndIsDeleted(
                        name,
                        StudentStatus.ACTIVE,
                        "false"
                );
    }


    // =====================================================
    // SEARCH BY EMAIL
    // =====================================================

    public List<Student> searchByEmail(
            String email) {

        return repository
                .findByEmailContainingIgnoreCaseAndStatusAndIsDeleted(
                        email,
                        StudentStatus.ACTIVE,
                        "false"
                );
    }


    // =====================================================
    // SEARCH BY DEPARTMENT
    // =====================================================

    public List<Student> searchByDepartment(
            String department) {

        return repository
                .findByDepartmentContainingIgnoreCaseAndStatusAndIsDeleted(
                        department,
                        StudentStatus.ACTIVE,
                        "false"
                );
    }


    // =====================================================
    // SEARCH BY CITY
    // =====================================================

    public List<Student> searchByCity(
            String city) {

        return repository
                .findByCityContainingIgnoreCaseAndStatusAndIsDeleted(
                        city,
                        StudentStatus.ACTIVE,
                        "false"
                );
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
                direction.equalsIgnoreCase("asc")
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();


        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        sort
                );


        return repository.findByStatusAndIsDeleted(
                StudentStatus.ACTIVE,
                "false",
                pageable
        );
    }


    // =====================================================
    // FILTER + PAGINATION + SORTING
    // =====================================================

    public Page<Student> getStudentsByFilter(
            String name,
            String status,
            int page,
            int size,
            String sortBy,
            String direction) {


        Sort sort =
                direction.equalsIgnoreCase("asc")
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();


        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        sort
                );


        StudentStatus studentStatus =
                null;


        if (status != null
                && !status.isEmpty()) {

            studentStatus =
                    StudentStatus.valueOf(
                            status.toUpperCase()
                    );
        }


        if (name != null
                && !name.isEmpty()
                && studentStatus != null) {

            return repository
                    .findByNameContainingIgnoreCaseAndStatusAndIsDeleted(
                            name,
                            studentStatus,
                            "false",
                            pageable
                    );
        }


        if (name != null
                && !name.isEmpty()) {

            return repository
                    .findByNameContainingIgnoreCaseAndStatusAndIsDeleted(
                            name,
                            StudentStatus.ACTIVE,
                            "false",
                            pageable
                    );
        }


        if (studentStatus != null) {

            return repository.findByStatusAndIsDeleted(
                    studentStatus,
                    "false",
                    pageable
            );
        }


        return repository.findByStatusAndIsDeleted(
                StudentStatus.ACTIVE,
                "false",
                pageable
        );
    }


    // =====================================================
    // MANAGE STUDENTS ONLY
    // =====================================================

    public Page<Student> searchOnlyStudents(
            String keyword,
            String department,
            int page,
            int size,
            String sortField,
            String sortDir) {


        Sort sort =
                sortDir.equalsIgnoreCase("asc")
                        ? Sort.by(sortField).ascending()
                        : Sort.by(sortField).descending();


        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        sort
                );


        if (keyword != null
                && !keyword.trim().isEmpty()) {

            return repository
                    .findByRole_NameAndNameContainingIgnoreCaseAndStatusAndIsDeleted(
                            "ROLE_STUDENT",
                            keyword,
                            StudentStatus.ACTIVE,
                            "false",
                            pageable
                    );
        }


        if (department != null
                && !department.trim().isEmpty()) {

            return repository
                    .findByRole_NameAndDepartmentContainingIgnoreCaseAndStatusAndIsDeleted(
                            "ROLE_STUDENT",
                            department,
                            StudentStatus.ACTIVE,
                            "false",
                            pageable
                    );
        }


        return repository
                .findByRole_NameAndStatusAndIsDeleted(
                        "ROLE_STUDENT",
                        StudentStatus.ACTIVE,
                        "false",
                        pageable
                );
    }


    // =====================================================
    // SIMPLE LIST - ADMINS
    // =====================================================

    public List<Student> getAllAdmins() {

        return repository
                .findByRole_NameAndStatusAndIsDeleted(
                        "ROLE_ADMIN",
                        StudentStatus.ACTIVE,
                        "false"
                );
    }


    // =====================================================
    // SIMPLE LIST - STUDENTS
    // =====================================================

    public List<Student> getAllStudentsOnly() {

        return repository
                .findByRole_NameAndStatusAndIsDeleted(
                        "ROLE_STUDENT",
                        StudentStatus.ACTIVE,
                        "false"
                );
    }


    // =====================================================
    // RECENT REGISTRATION COUNT
    // =====================================================

    public long getRecentStudentCount() {

        LocalDateTime date =
                LocalDateTime.now()
                        .minusDays(7);


        return repository.countRecentStudents(
                date
        );
    }


    // =====================================================
    // RECENT REGISTRATION LIST
    // =====================================================

    public List<Student> getRecentStudents() {

        return repository
                .findTop10ByStatusAndIsDeletedOrderByCreatedDateDesc(
                        StudentStatus.ACTIVE,
                        "false"
                );
    }


 // =====================================================
 // SOFT DELETE USER
 // ADMIN CANNOT DELETE ANOTHER ADMIN
 // SHOW PARTICULAR ADMIN NAME IN ERROR MESSAGE
 // WITH IP ADDRESS + AUDIT LOG
 // =====================================================

 @Transactional
 public void softDelete(
         Long id,
         String ipAddress,
         Authentication authentication) {

     // =================================================
     // FIND USER
     // =================================================

     Student student =
             repository.findById(id)
                     .orElseThrow(() ->
                             new ResourceNotFoundException(
                                     "Student not found with id : "
                                             + id
                             ));


     // =================================================
     // GET LOGGED-IN USER EMAIL
     // =================================================

     String loggedInEmail = null;

     if (authentication != null) {

         loggedInEmail =
                 authentication.getName();
     }


     // =================================================
     // PREVENT ADMIN FROM DELETING ANOTHER ADMIN
     // =================================================

     if (student.getRole() != null
             && "ROLE_ADMIN".equalsIgnoreCase(
                     student.getRole().getName())) {

         // ---------------------------------------------
         // If selected admin is NOT the logged-in admin
         // ---------------------------------------------

         if (loggedInEmail == null
                 || !student.getEmail()
                         .equalsIgnoreCase(
                                 loggedInEmail
                         )) {

             throw new RuntimeException(
                     "You cannot delete admin profile: "
                             + student.getName()
                             + "."
             );
         }
     }


     // =================================================
     // SOFT DELETE
     // =================================================

     student.setIsDeleted(
             "true"
     );

     student.setStatus(
             StudentStatus.INACTIVE
     );


     // =================================================
     // SAVE
     // =================================================

     Student deletedStudent =
             repository.save(student);


     // =================================================
     // GET ROLE
     // =================================================

     String roleName =
             "ROLE_STUDENT";

     if (deletedStudent.getRole() != null
             && deletedStudent.getRole().getName() != null) {

         roleName =
                 deletedStudent.getRole().getName();
     }


     // =================================================
     // ENTITY NAME
     // =================================================

     String entityName;

     if ("ROLE_ADMIN".equalsIgnoreCase(
             roleName)) {

         entityName =
                 "ADMIN";

     } else if ("ROLE_STUDENT".equalsIgnoreCase(
             roleName)) {

         entityName =
                 "STUDENT";

     } else {

         entityName =
                 "USER";
     }


     // =================================================
     // DELETE MESSAGE
     // =================================================

     String message;

     if ("ROLE_ADMIN".equalsIgnoreCase(
             roleName)) {

         message =
                 "Admin deleted successfully";

     } else if ("ROLE_STUDENT".equalsIgnoreCase(
             roleName)) {

         message =
                 "Student deleted successfully";

     } else {

         message =
                 "User deleted successfully";
     }


     // =================================================
     // WHO PERFORMED THE ACTION
     // =================================================

     String performedBy =
             "SYSTEM";

     if (authentication != null
             && authentication.getName() != null) {

         performedBy =
                 authentication.getName();
     }


     // =================================================
     // AUDIT LOG
     // =================================================

     auditLogService.createLog(
             deletedStudent.getId(),
             performedBy,
             roleName,
             "DELETE",
             entityName,
             deletedStudent.getId(),
             message,
             ipAddress
     );
 }
 
 

    // =====================================================
    // GET ALL USERS
    // SEARCH + PAGINATION + SORTING
    // =====================================================

    public Page<Student> getAllUsers(
            String keyword,
            String role,
            int page,
            int size,
            String sortField,
            String sortDir) {


        Sort sort =
                sortDir.equalsIgnoreCase("asc")
                        ? Sort.by(sortField).ascending()
                        : Sort.by(sortField).descending();


        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        sort
                );


        if (keyword != null
                && !keyword.trim().isEmpty()
                && role != null
                && !role.isBlank()) {

            return repository
                    .findByRole_NameAndNameContainingIgnoreCaseAndStatusAndIsDeleted(
                            role,
                            keyword,
                            StudentStatus.ACTIVE,
                            "false",
                            pageable
                    );
        }


        if (keyword != null
                && !keyword.trim().isEmpty()) {

            return repository
                    .findByNameContainingIgnoreCaseAndStatusAndIsDeleted(
                            keyword,
                            StudentStatus.ACTIVE,
                            "false",
                            pageable
                    );
        }


        if (role != null
                && !role.isBlank()) {

            return repository
                    .findByRole_NameAndStatusAndIsDeleted(
                            role,
                            StudentStatus.ACTIVE,
                            "false",
                            pageable
                    );
        }


        return repository.findByStatusAndIsDeleted(
                StudentStatus.ACTIVE,
                "false",
                pageable
        );
    }


    // =====================================================
    // GET ALL ADMINS
    // SEARCH + PAGINATION + SORTING
    // =====================================================

    public Page<Student> getAllAdmins(
            String keyword,
            int page,
            int size,
            String sortField,
            String sortDir) {


        Sort sort =
                sortDir.equalsIgnoreCase("asc")
                        ? Sort.by(sortField).ascending()
                        : Sort.by(sortField).descending();


        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        sort
                );


        if (keyword != null
                && !keyword.trim().isEmpty()) {

            return repository
                    .findByRole_NameAndNameContainingIgnoreCaseAndStatusAndIsDeleted(
                            "ROLE_ADMIN",
                            keyword,
                            StudentStatus.ACTIVE,
                            "false",
                            pageable
                    );
        }


        return repository
                .findByRole_NameAndStatusAndIsDeleted(
                        "ROLE_ADMIN",
                        StudentStatus.ACTIVE,
                        "false",
                        pageable
                );
    }


    // =====================================================
    // GET ALL STUDENTS
    // SEARCH + PAGINATION + SORTING
    // =====================================================

    public Page<Student> getAllStudentsOnly(
            String keyword,
            int page,
            int size,
            String sortField,
            String sortDir) {


        Sort sort =
                sortDir.equalsIgnoreCase("asc")
                        ? Sort.by(sortField).ascending()
                        : Sort.by(sortField).descending();


        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        sort
                );


        if (keyword != null
                && !keyword.trim().isEmpty()) {

            return repository
                    .findByRole_NameAndNameContainingIgnoreCaseAndStatusAndIsDeleted(
                            "ROLE_STUDENT",
                            keyword,
                            StudentStatus.ACTIVE,
                            "false",
                            pageable
                    );
        }


        return repository
                .findByRole_NameAndStatusAndIsDeleted(
                        "ROLE_STUDENT",
                        StudentStatus.ACTIVE,
                        "false",
                        pageable
                );
    }


    // =====================================================
    // RECENT REGISTRATIONS
    // SEARCH + PAGINATION + SORTING
    // =====================================================

    public Page<Student> getRecentStudents(
            String keyword,
            int page,
            int size,
            String sortField,
            String sortDir) {


        // =================================================
        // SORT
        // =================================================

        Sort sort =
                sortDir.equalsIgnoreCase("asc")
                        ? Sort.by(sortField).ascending()
                        : Sort.by(sortField).descending();


        // =================================================
        // PAGE
        // =================================================

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        sort
                );


        // =================================================
        // SEARCH
        // =================================================

        if (keyword != null
                && !keyword.trim().isEmpty()) {

            return repository
                    .findByNameContainingIgnoreCaseAndStatusAndIsDeleted(
                            keyword,
                            StudentStatus.ACTIVE,
                            "false",
                            pageable
                    );
        }


        // =================================================
        // ALL ACTIVE USERS
        // =================================================

        return repository.findByStatusAndIsDeleted(
                StudentStatus.ACTIVE,
                "false",
                pageable
        );
    }
    
    
 // =====================================================
 // FIND STUDENT BY EMAIL
 // =====================================================

 public Optional<Student> findByEmail(String email) {

     return repository.findByEmail(email);
 }
 
 
 
 
//=====================================================
//ADMIN UPDATE USER
//ADMIN CAN EDIT STUDENT
//ADMIN CANNOT EDIT ANOTHER ADMIN
//WITH AUDIT LOG
//=====================================================

@Transactional
public void updateUserByAdmin(
      Long id,
      Student updatedStudent,
      String ipAddress,
      Authentication authentication) {

  // =================================================
  // FIND EXISTING USER
  // =================================================

  Student existingUser =
          repository.findById(id)
                  .orElseThrow(() ->
                          new ResourceNotFoundException(
                                  "User not found with ID: " + id
                          ));


  // =================================================
  // GET LOGGED-IN USER EMAIL
  // =================================================

  String loggedInEmail = null;

  if (authentication != null
          && authentication.getName() != null) {

      loggedInEmail =
              authentication.getName();
  }


  // =================================================
  // PREVENT ADMIN FROM UPDATING ANOTHER ADMIN
  // =================================================

  if (existingUser.getRole() != null
          && "ROLE_ADMIN".equalsIgnoreCase(
                  existingUser.getRole().getName())) {

      if (loggedInEmail == null
              || !existingUser.getEmail()
                      .equalsIgnoreCase(
                              loggedInEmail
                      )) {

          throw new RuntimeException(
                  "You cannot update another admin profile."
          );
      }
  }


  // =================================================
  // STORE OLD VALUES
  // =================================================

  String oldName =
          existingUser.getName();

  Integer oldAge =
          existingUser.getAge();

  String oldContactNo =
          existingUser.getContactNo();

  String oldDepartment =
          existingUser.getDepartment();

  String oldCity =
          existingUser.getCity();

  String oldAddress =
          existingUser.getAddress();

  StudentStatus oldStatus =
          existingUser.getStatus();


  // =================================================
  // CHECK WHETHER ANYTHING CHANGED
  // =================================================

  boolean changed = false;


  if (!java.util.Objects.equals(
          oldName,
          updatedStudent.getName())) {

      changed = true;
  }


  if (!java.util.Objects.equals(
          oldAge,
          updatedStudent.getAge())) {

      changed = true;
  }


  if (!java.util.Objects.equals(
          oldContactNo,
          updatedStudent.getContactNo())) {

      changed = true;
  }


  if (!java.util.Objects.equals(
          oldDepartment,
          updatedStudent.getDepartment())) {

      changed = true;
  }


  if (!java.util.Objects.equals(
          oldCity,
          updatedStudent.getCity())) {

      changed = true;
  }


  if (!java.util.Objects.equals(
          oldAddress,
          updatedStudent.getAddress())) {

      changed = true;
  }


  if (!java.util.Objects.equals(
          oldStatus,
          updatedStudent.getStatus())) {

      changed = true;
  }


  // =================================================
  // UPDATE USER DETAILS
  // =================================================

  existingUser.setName(
          updatedStudent.getName()
  );

  existingUser.setAge(
          updatedStudent.getAge()
  );

  existingUser.setContactNo(
          updatedStudent.getContactNo()
  );

  existingUser.setDepartment(
          updatedStudent.getDepartment()
  );

  existingUser.setCity(
          updatedStudent.getCity()
  );

  existingUser.setAddress(
          updatedStudent.getAddress()
  );


  // =================================================
  // UPDATE STATUS
  // =================================================

  if (updatedStudent.getStatus() != null) {

      existingUser.setStatus(
              updatedStudent.getStatus()
      );
  }


  // =================================================
  // DO NOT CHANGE ROLE
  // =================================================

  // Role remains unchanged.


  // =================================================
  // SAVE
  // =================================================

  Student updatedUser =
          repository.save(existingUser);


  // =================================================
  // GET TARGET USER ROLE
  // =================================================

  String targetRole = "ROLE_STUDENT";

  if (updatedUser.getRole() != null
          && updatedUser.getRole().getName() != null) {

      targetRole =
              updatedUser.getRole().getName();
  }


  // =================================================
  // ENTITY NAME
  // =================================================

  String entityName;

  if ("ROLE_ADMIN".equalsIgnoreCase(
          targetRole)) {

      entityName = "ADMIN";

  } else if ("ROLE_STUDENT".equalsIgnoreCase(
          targetRole)) {

      entityName = "STUDENT";

  } else {

      entityName = "USER";
  }


  // =================================================
  // WHO PERFORMED THE ACTION
  // =================================================

  String performedBy = "SYSTEM";

  if (authentication != null
          && authentication.getName() != null
          && !authentication.getName().trim().isEmpty()) {

      performedBy =
              authentication.getName();
  }


  // =================================================
  // GET ACTOR ROLE
  // =================================================

  String performedByRole = "ROLE_ADMIN";

  if (authentication != null
          && authentication.getAuthorities() != null) {

      performedByRole =
              authentication.getAuthorities()
                      .stream()
                      .map(authority ->
                              authority.getAuthority())
                      .filter(role ->
                              role.equals("ROLE_ADMIN")
                              || role.equals("ROLE_STUDENT"))
                      .findFirst()
                      .orElse("ROLE_ADMIN");
  }


  // =================================================
  // AUDIT LOG
  // =================================================

  if (changed) {

      String message;

      if ("ROLE_ADMIN".equalsIgnoreCase(
              targetRole)) {

          message =
                  "Admin updated admin "
                  + updatedUser.getEmail()
                  + " successfully";

      } else if ("ROLE_STUDENT".equalsIgnoreCase(
              targetRole)) {

          message =
                  "Admin updated student "
                  + updatedUser.getEmail()
                  + " successfully";

      } else {

          message =
                  "Admin updated user "
                  + updatedUser.getEmail()
                  + " successfully";
      }


      auditLogService.createLog(
              updatedUser.getId(),
              performedBy,
              performedByRole,
              "UPDATE",
              entityName,
              updatedUser.getId(),
              message,
              ipAddress
      );
  }
}

 
}