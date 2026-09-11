package com.student.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.student.entity.RefreshToken;
import com.student.entity.Student;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // Find refresh token by token string
    Optional<RefreshToken> findByToken(String token);

    // Find refresh token by student
    Optional<RefreshToken> findByStudent(Student student);

    // Delete refresh token by student
    void deleteByStudent(Student student);
}