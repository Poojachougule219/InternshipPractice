package com.student.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    // =====================================================
    // SECRET KEY
    // =====================================================

    private static final String SECRET_KEY =
            "12345678901234567890123456789012";


    // =====================================================
    // GET SIGNING KEY
    // =====================================================

    private Key getSignKey() {

        return Keys.hmacShaKeyFor(
                SECRET_KEY.getBytes(StandardCharsets.UTF_8)
        );
    }


    // =====================================================
    // GENERATE JWT TOKEN
    // =====================================================

    public String generateToken(String email) {

        return Jwts.builder()

                .subject(email)

                .issuedAt(new Date())

                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                + 1000L * 60 * 30
                        )
                )

                .signWith(getSignKey())

                .compact();
    }


    // =====================================================
    // EXTRACT USERNAME / EMAIL
    // =====================================================

    public String extractUsername(String token) {

        return extractClaims(token)
                .getSubject();
    }


    // =====================================================
    // CHECK TOKEN VALID
    // =====================================================

    public boolean isTokenValid(
            String token,
            String email) {

        try {

            Claims claims =
                    extractClaims(token);

            return claims
                    .getSubject()
                    .equals(email)

                    && !claims
                            .getExpiration()
                            .before(new Date());

        } catch (Exception e) {

            return false;
        }
    }


    // =====================================================
    // EXTRACT CLAIMS
    // =====================================================

    private Claims extractClaims(String token) {

        return Jwts.parser()

                .verifyWith(
                        (javax.crypto.SecretKey) getSignKey()
                )

                .build()

                .parseSignedClaims(token)

                .getPayload();
    }
}