package com.student.security;

import java.security.Key;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    // Secret key (must be at least 32 bytes)
	private static final String SECRET_KEY =
	        "12345678901234567890123456789012";
    

	private Key getSignKey() {

	    return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
	}
	
	
    
    // Generate JWT Token
    public String generateToken(String email) {

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30)) // 30 minutes
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Extract Email
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    
    public boolean isTokenValid(String token, String email) {

        Claims claims = extractClaims(token);

        return claims.getSubject().equals(email)
                && !claims.getExpiration().before(new Date());
    }

    
    // Extract Claims
    private Claims extractClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}