package com.student.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.student.entity.RefreshToken;
import com.student.entity.Student;
import com.student.enums.StudentStatus;
import com.student.exception.ResourceNotFoundException;
import com.student.repository.RefreshTokenRepository;
import com.student.repository.StudentRepository;


@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {


    private final long refreshTokenDurationMs =
            7 * 24 * 60 * 60 * 1000;



    @Autowired
    private RefreshTokenRepository refreshTokenRepository;



    @Autowired
    private StudentRepository studentRepository;





    @Override
    public RefreshToken createRefreshToken(String email) {


        Student student = studentRepository
                .findByEmailAndStatusAndIsDeleted(
                        email,
                        StudentStatus.ACTIVE,
                        "false")
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Active student not found"));




        RefreshToken refreshToken =
                refreshTokenRepository
                .findByStudent(student)
                .orElse(new RefreshToken());



        refreshToken.setStudent(student);

        refreshToken.setToken(
                UUID.randomUUID().toString());

        refreshToken.setExpiryDate(
                Instant.now()
                .plusMillis(refreshTokenDurationMs));



        return refreshTokenRepository.save(refreshToken);
    }







    @Override
    public RefreshToken verifyExpiration(
            RefreshToken token) {


        if(token.getExpiryDate()
                .compareTo(Instant.now()) < 0) {


            refreshTokenRepository.delete(token);


            throw new RuntimeException(
                    "Refresh Token Expired. Please login again.");
        }



        return token;
    }







    @Override
    public RefreshToken findByToken(String token) {


        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Refresh Token not found"));
    }







    @Override
    @Transactional
    public void deleteByStudentId(Long studentId) {


        Student student =
                studentRepository.findByIdAndIsDeleted(
                        studentId,
                        "false")
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Student not found"));



        refreshTokenRepository.deleteByStudent(student);
    }

}