package com.student.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.student.entity.Student;
import com.student.enums.StudentStatus;


public interface StudentRepository extends JpaRepository<Student, Long> {


    Optional<Student> findByEmailAndStatusAndIsDeleted(
            String email,
            StudentStatus status,
            String isDeleted);



    Optional<Student> findByEmailAndIsDeleted(
            String email,
            String isDeleted);



    boolean existsByEmailAndStatusAndIsDeleted(
            String email,
            StudentStatus status,
            String isDeleted);



    Optional<Student> findByIdAndIsDeleted(
            Long id,
            String isDeleted);




    // ===============================
    // STATUS + DELETE FILTER
    // ===============================

    List<Student> findByStatusAndIsDeleted(
            StudentStatus status,
            String isDeleted);


    Page<Student> findByStatusAndIsDeleted(
            StudentStatus status,
            String isDeleted,
            Pageable pageable);




    // ===============================
    // SEARCH
    // ===============================

    List<Student> findByNameContainingIgnoreCaseAndStatusAndIsDeleted(
            String name,
            StudentStatus status,
            String isDeleted);



    Page<Student> findByNameContainingIgnoreCaseAndStatusAndIsDeleted(
            String name,
            StudentStatus status,
            String isDeleted,
            Pageable pageable);



    List<Student> findByEmailContainingIgnoreCaseAndStatusAndIsDeleted(
            String email,
            StudentStatus status,
            String isDeleted);



    List<Student> findByDepartmentContainingIgnoreCaseAndStatusAndIsDeleted(
            String department,
            StudentStatus status,
            String isDeleted);



    Page<Student> findByDepartmentContainingIgnoreCaseAndStatusAndIsDeleted(
            String department,
            StudentStatus status,
            String isDeleted,
            Pageable pageable);



    List<Student> findByCityContainingIgnoreCaseAndStatusAndIsDeleted(
            String city,
            StudentStatus status,
            String isDeleted);




    // ===============================
    // ROLE BASED SEARCH
    // ===============================

    Page<Student> findByRole_NameAndStatusAndIsDeleted(
            String roleName,
            StudentStatus status,
            String isDeleted,
            Pageable pageable);



    List<Student> findByRole_NameAndStatusAndIsDeleted(
            String roleName,
            StudentStatus status,
            String isDeleted);



    Page<Student> findByRole_NameAndNameContainingIgnoreCaseAndStatusAndIsDeleted(
            String roleName,
            String name,
            StudentStatus status,
            String isDeleted,
            Pageable pageable);



    Page<Student> findByRole_NameAndDepartmentContainingIgnoreCaseAndStatusAndIsDeleted(
            String roleName,
            String department,
            StudentStatus status,
            String isDeleted,
            Pageable pageable);




    // ===============================
    // COUNT
    // ===============================

    long countByStatusAndIsDeleted(
            StudentStatus status,
            String isDeleted);



    long countByRole_NameAndStatusAndIsDeleted(
            String roleName,
            StudentStatus status,
            String isDeleted);




    // ===============================
    // RECENT REGISTRATIONS
    // ===============================

    @Query("""
            SELECT COUNT(s)
            FROM Student s
            WHERE s.status = com.student.enums.StudentStatus.ACTIVE
            AND s.isDeleted = 'false'
            AND s.createdDate >= :date
            """)
    long countRecentStudents(
            @Param("date") LocalDateTime date);




    List<Student> findTop10ByStatusAndIsDeletedOrderByCreatedDateDesc(
            StudentStatus status,
            String isDeleted);



    Page<Student> findByStatusAndIsDeletedOrderByCreatedDateDesc(
            StudentStatus status,
            String isDeleted,
            Pageable pageable);




    // ===============================
    // DASHBOARD CHARTS
    // ===============================

    @Query("""
            SELECT COUNT(DISTINCT s.department)
            FROM Student s
            WHERE s.status = com.student.enums.StudentStatus.ACTIVE
            AND s.isDeleted = 'false'
            """)
    long countDistinctDepartments();




    @Query("""
            SELECT s.department, COUNT(s)
            FROM Student s
            WHERE s.status = com.student.enums.StudentStatus.ACTIVE
            AND s.isDeleted = 'false'
            GROUP BY s.department
            """)
    List<Object[]> getStudentCountByDepartment();




    @Query("""
            SELECT s.city, COUNT(s)
            FROM Student s
            WHERE s.status = com.student.enums.StudentStatus.ACTIVE
            AND s.isDeleted = 'false'
            GROUP BY s.city
            """)
    List<Object[]> getStudentCountByCity();




    // ===============================
    // ADMINS
    // ===============================

    @Query("""
            SELECT s
            FROM Student s
            WHERE s.role.name = 'ROLE_ADMIN'
            AND s.isDeleted = 'false'
            """)
    List<Student> findAllAdmins();



    List<Student> findByRole_NameAndIsDeleted(
            String roleName,
            String isDeleted);
    
    
    
    long countByIsDeleted(String isDeleted);

    long countByRole_NameAndIsDeleted(
            String roleName,
            String isDeleted);
    
    
    Optional<Student> findByEmail(String email);

}