package com.example.auth.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {
    private final SecretKey key;
    private final long ttlSeconds;

    public JwtService(
            @Value("${auth.jwt.secret}") String secret,
            @Value("${auth.jwt.ttl-seconds}") long ttlSeconds
    ) {
        this.key = Keys.hmacShaKeyFor(toHmacSha256KeyBytes(secret));
        this.ttlSeconds = ttlSeconds;
    }

    public String issueToken(String userId, String email) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(ttlSeconds);

        return Jwts.builder()
                .subject(email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claims(Map.of("userId", userId, "email", email))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public JwtPrincipal verifyToken(String token) throws JwtException {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String userId = claims.get("userId", String.class);
        String email = claims.get("email", String.class);
        if (userId == null || userId.isBlank() || email == null || email.isBlank()) {
            throw new JwtException("Missing required claims");
        }

        return new JwtPrincipal(userId, email);
    }

    private static byte[] toHmacSha256KeyBytes(String secret) {
        byte[] raw = (secret == null ? "" : secret).getBytes(StandardCharsets.UTF_8);
        if (raw.length >= 32) {
            return raw;
        }
        try {
            return MessageDigest.getInstance("SHA-256").digest(raw);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public record JwtPrincipal(String userId, String email) {}
}

