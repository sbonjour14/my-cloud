package com.github.sbonjour.my_cloud.security;

import com.github.sbonjour.my_cloud.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

/**
 * Service for handling JSON Web Tokens (JWT) for user authentication.
 *
 * This service provides methods to generate JWT tokens for users, extract user IDs
 * from tokens, and validate the tokens. It uses a secret key for signing the tokens
 * and supports token expiration.
 */

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationMillis;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String generateToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    public UUID extractUserId(String token) {
        String subject = extractAllClaims(token).getSubject();
        return UUID.fromString(subject);
    }

    /**
     * Validates the provided JWT token.
     *
     * This method checks if the token is well-formed, signed with the correct key,
     * and not expired. It returns true if the token is valid, false otherwise.
     *
     * @param token the JWT token to validate
     * @return true if the token is valid, false otherwise
     */

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extracts all claims from the provided JWT token.
     *
     * This method parses the token and retrieves the claims contained within it.
     * It throws an exception if the token is invalid or cannot be parsed.
     * 
     *
     * @param token the JWT token from which to extract claims
     * @return the Claims object containing the token's claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}