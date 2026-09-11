package com.legalcontract.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.legalcontract.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // =========================================================
    // FIND USER BY USERNAME
    // =========================================================

    Optional<User> findByUsername(String username);

    // =========================================================
    // FIND USER BY EMAIL
    // =========================================================

    Optional<User> findByEmail(String email);

    // =========================================================
    // CHECK USERNAME
    // =========================================================

    boolean existsByUsername(String username);

    // =========================================================
    // CHECK EMAIL
    // =========================================================

    boolean existsByEmail(String email);

    // =========================================================
    // FIND USERS BY STATUS
    // =========================================================

    List<User> findByStatus(String status);

    // =========================================================
    // FIND USERS EXCLUDING DELETED USERS
    // =========================================================

    List<User> findByStatusNot(String status);

    // =========================================================
    // FIND USERS BY ROLE AND STATUS
    // =========================================================

    List<User> findByRole_NameAndStatus(
            String roleName,
            String status
    );

    // =========================================================
    // ROLE BASED COUNTS
    // =========================================================

    long countByRole_Name(String roleName);

    long countByRole_NameAndStatus(
            String roleName,
            String status
    );
}