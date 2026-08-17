package com.student.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.student.entity.PasswordResetToken;

import org.springframework.transaction.annotation.Transactional;


@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long>{


    Optional<PasswordResetToken> findByEmail(String email);


    @Transactional
    void deleteByEmail(String email);

}