package com.student.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.student.entity.Student;
import com.student.enums.StudentStatus;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    // =====================================================
    // BASIC SEARCH
    // =====================================================

    List<Student> findByNameContainingIgnoreCase(
            String name
    );

    List<Student> findByEmailContainingIgnoreCase(
            String email
    );

    List<Student> findByDepartmentContainingIgnoreCase(
            String department
    );

    List<Student> findByCityContainingIgnoreCase(
            String city
    );


    // =====================================================
    // LOGIN
    // =====================================================

    Student findByEmailAndPassword(
            String email,
            String password
    );


    // =====================================================
    // EMAIL
    // =====================================================

    boolean existsByEmail(String email);

    Optional<Student> findByEmail(
            String email
    );

    Optional<Student> findByEmailAndIsDeleted(
            String email,
            String isDeleted
    );


    // =====================================================
    // LOGIN
    // EMAIL + STATUS + SOFT DELETE
    // Used by CustomUserDetailsService
    // =====================================================

    Optional<Student> findByEmailAndStatusAndIsDeleted(
            String email,
            StudentStatus status,
            String isDeleted
    );


    // =====================================================
    // FIND BY ID + SOFT DELETE
    // Used by AuthService
    // =====================================================

    Optional<Student> findByIdAndIsDeleted(
            Long id,
            String isDeleted
    );


    // =====================================================
    // SEARCH + PAGINATION
    // =====================================================

    Page<Student>
    findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String name,
            String email,
            Pageable pageable
    );


    // =====================================================
    // SEARCH + IS DELETED + PAGINATION
    // =====================================================

    Page<Student>
    findByIsDeletedAndNameContainingIgnoreCaseOrIsDeletedAndEmailContainingIgnoreCase(
            String isDeleted1,
            String name,
            String isDeleted2,
            String email,
            Pageable pageable
    );


    // =====================================================
    // IS DELETED
    // =====================================================

    Page<Student> findByIsDeleted(
            String isDeleted,
            Pageable pageable
    );

    List<Student> findByIsDeleted(
            String isDeleted
    );

    List<Student> findByIsDeleted(
            String isDeleted,
            Sort sort
    );


    // =====================================================
    // ROLE
    // =====================================================

    Page<Student> findByRole_Name(
            String roleName,
            Pageable pageable
    );

    List<Student> findByRole_Name(
            String roleName
    );


    // =====================================================
    // ROLE + IS DELETED
    // =====================================================

    Page<Student> findByRole_NameAndIsDeleted(
            String roleName,
            String isDeleted,
            Pageable pageable
    );


    // =====================================================
    // ROLE + IS DELETED + SEARCH
    // =====================================================

    Page<Student>
    findByRole_NameAndIsDeletedAndNameContainingIgnoreCase(
            String roleName,
            String isDeleted,
            String name,
            Pageable pageable
    );


    // =====================================================
    // COUNTS
    // =====================================================

    long countByRole_Name(
            String roleName
    );

    long countByIsDeleted(
            String isDeleted
    );

    long countByRole_NameAndIsDeleted(
            String roleName,
            String isDeleted
    );


    // =====================================================
    // STATUS COUNTS
    // =====================================================

    long countByStatus(
            StudentStatus status
    );

    long countByRole_NameAndStatus(
            String roleName,
            StudentStatus status
    );


    // =====================================================
    // DEPARTMENT COUNT
    // =====================================================

    @Query("""
            SELECT COUNT(DISTINCT s.department)
            FROM Student s
            WHERE s.department IS NOT NULL
            AND s.department <> ''
            """)
    long countDistinctDepartment();


    // =====================================================
    // RECENT STUDENT COUNT
    // =====================================================

    long countByCreatedDateAfter(
            LocalDateTime date
    );


    // =====================================================
    // DASHBOARD
    // STUDENTS BY DEPARTMENT
    // =====================================================

    @Query("""
            SELECT s.department, COUNT(s)
            FROM Student s
            WHERE s.department IS NOT NULL
            AND s.department <> ''
            GROUP BY s.department
            """)
    List<Object[]> countStudentsByDepartment();


    // =====================================================
    // DASHBOARD
    // STUDENTS BY CITY
    // =====================================================

    @Query("""
            SELECT s.city, COUNT(s)
            FROM Student s
            WHERE s.city IS NOT NULL
            AND s.city <> ''
            GROUP BY s.city
            """)
    List<Object[]> countStudentsByCity();


    // =====================================================
    // USERS SEARCH
    // NAME / EMAIL / DEPARTMENT / CITY
    // =====================================================

    @Query("""
            SELECT s
            FROM Student s
            WHERE
                LOWER(s.name) LIKE
                    LOWER(CONCAT('%', :keyword, '%'))

                OR LOWER(s.email) LIKE
                    LOWER(CONCAT('%', :keyword, '%'))

                OR LOWER(s.department) LIKE
                    LOWER(CONCAT('%', :keyword, '%'))

                OR LOWER(s.city) LIKE
                    LOWER(CONCAT('%', :keyword, '%'))
            """)
    Page<Student> findByKeyword(
            @Param("keyword") String keyword,
            Pageable pageable
    );


    // =====================================================
    // USERS SEARCH + ROLE
    // =====================================================

    @Query("""
            SELECT s
            FROM Student s
            WHERE
                (
                    LOWER(s.name) LIKE
                        LOWER(CONCAT('%', :keyword, '%'))

                    OR LOWER(s.email) LIKE
                        LOWER(CONCAT('%', :keyword, '%'))

                    OR LOWER(s.department) LIKE
                        LOWER(CONCAT('%', :keyword, '%'))

                    OR LOWER(s.city) LIKE
                        LOWER(CONCAT('%', :keyword, '%'))
                )
                AND LOWER(s.role.name) = LOWER(:role)
            """)
    Page<Student> findByKeywordAndRole(
            @Param("keyword") String keyword,
            @Param("role") String role,
            Pageable pageable
    );


    // =====================================================
    // ADMIN SEARCH
    // =====================================================

    @Query("""
            SELECT s
            FROM Student s
            WHERE s.role.name = 'ROLE_ADMIN'
            AND
            (
                LOWER(s.name) LIKE
                    LOWER(CONCAT('%', :keyword, '%'))

                OR LOWER(s.email) LIKE
                    LOWER(CONCAT('%', :keyword, '%'))

                OR LOWER(s.department) LIKE
                    LOWER(CONCAT('%', :keyword, '%'))

                OR LOWER(s.city) LIKE
                    LOWER(CONCAT('%', :keyword, '%'))
            )
            """)
    Page<Student> findAdminsByKeyword(
            @Param("keyword") String keyword,
            Pageable pageable
    );


    // =====================================================
    // STUDENT SEARCH
    // =====================================================

    @Query("""
            SELECT s
            FROM Student s
            WHERE s.role.name = 'ROLE_STUDENT'
            AND
            (
                LOWER(s.name) LIKE
                    LOWER(CONCAT('%', :keyword, '%'))

                OR LOWER(s.email) LIKE
                    LOWER(CONCAT('%', :keyword, '%'))

                OR LOWER(s.department) LIKE
                    LOWER(CONCAT('%', :keyword, '%'))

                OR LOWER(s.city) LIKE
                    LOWER(CONCAT('%', :keyword, '%'))
            )
            """)
    Page<Student> findStudentsByKeyword(
            @Param("keyword") String keyword,
            Pageable pageable
    );


    // =====================================================
    // RECENT STUDENTS
    // PAGINATION
    // =====================================================

    @Query("""
            SELECT s
            FROM Student s
            WHERE s.createdDate IS NOT NULL
            ORDER BY s.createdDate DESC
            """)
    Page<Student> findRecentStudents(
            Pageable pageable
    );


    // =====================================================
    // RECENT STUDENTS
    // SEARCH + PAGINATION
    // =====================================================

    @Query("""
            SELECT s
            FROM Student s
            WHERE
            (
                LOWER(s.name) LIKE
                    LOWER(CONCAT('%', :keyword, '%'))

                OR LOWER(s.email) LIKE
                    LOWER(CONCAT('%', :keyword, '%'))

                OR LOWER(s.department) LIKE
                    LOWER(CONCAT('%', :keyword, '%'))

                OR LOWER(s.city) LIKE
                    LOWER(CONCAT('%', :keyword, '%'))
            )
            AND s.createdDate IS NOT NULL
            """)
    Page<Student> findRecentStudentsByKeyword(
            @Param("keyword") String keyword,
            Pageable pageable
    );


    // =====================================================
    // RECENT STUDENTS
    // EXPORT
    // =====================================================

    @Query("""
            SELECT s
            FROM Student s
            WHERE s.createdDate IS NOT NULL
            ORDER BY s.createdDate DESC
            """)
    List<Student> findRecentStudents();

}