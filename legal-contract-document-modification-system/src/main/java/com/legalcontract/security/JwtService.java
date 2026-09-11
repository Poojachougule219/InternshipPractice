package com.legalcontract.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    // =========================================================
    // GET SIGNING KEY
    // =========================================================

    private SecretKey getSigningKey() {

        return Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    // =========================================================
    // GENERATE JWT TOKEN
    // =========================================================

    public String generateToken(
            UserDetails userDetails) {

        String role =
                userDetails.getAuthorities()
                        .stream()
                        .findFirst()
                        .map(authority ->
                                authority.getAuthority())
                        .orElse("");

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + expiration
                        )
                )
                .signWith(getSigningKey())
                .compact();
    }

    // =========================================================
    // EXTRACT USERNAME
    // =========================================================

    public String extractUsername(
            String token) {

        return extractAllClaims(token)
                .getSubject();
    }

    // =========================================================
    // VALIDATE TOKEN
    // =========================================================

    public boolean isTokenValid(
            String token,
            UserDetails userDetails) {

        String username =
                extractUsername(token);

        return username.equals(
                userDetails.getUsername()
        )
                && !isTokenExpired(token);
    }

    // =========================================================
    // CHECK TOKEN EXPIRATION
    // =========================================================

    private boolean isTokenExpired(
            String token) {

        return extractAllClaims(token)
                .getExpiration()
                .before(new Date());
    }

    // =========================================================
    // EXTRACT ALL CLAIMS
    // =========================================================

    private Claims extractAllClaims(
            String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}