package com.student.service;

import com.student.entity.RefreshToken;


public interface RefreshTokenService {

    RefreshToken createRefreshToken(String email);

    RefreshToken verifyExpiration(RefreshToken token);

    RefreshToken findByToken(String token);

    void deleteByStudentId(Long studentId);
}