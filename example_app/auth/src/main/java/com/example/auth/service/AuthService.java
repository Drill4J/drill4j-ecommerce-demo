package com.example.auth.service;

import com.example.auth.domain.User;
import com.example.auth.web.BadRequestException;
import com.example.auth.web.ConflictException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {
    private final InMemoryUserStore userStore;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(InMemoryUserStore userStore, JwtService jwtService) {
        this.userStore = userStore;
        this.jwtService = jwtService;
    }

    public AuthResult signUp(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail.isBlank()) {
            throw new BadRequestException("Email is required");
        }
        if (password == null || password.isBlank()) {
            throw new BadRequestException("Password is required");
        }
        PasswordPolicy.validate(password);
        if (userStore.existsByEmail(normalizedEmail)) {
            throw new ConflictException("User with this email is already registered");
        }

        var user = new User(UUID.randomUUID().toString(), normalizedEmail, passwordEncoder.encode(password));
        userStore.save(user);

        String token = jwtService.issueToken(user.id(), user.email());
        return new AuthResult(user.id(), user.email(), token);
    }

    public AuthResult signIn(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail.isBlank()) {
            throw new BadRequestException("Email is required");
        }
        if (password == null || password.isBlank()) {
            throw new BadRequestException("Password is required");
        }

        var user = userStore.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.passwordHash())) {
            throw new BadRequestException("Invalid email or password");
        }

        String token = jwtService.issueToken(user.id(), user.email());
        return new AuthResult(user.id(), user.email(), token);
    }

    public void changePassword(String userId, String email, String currentPassword, String newPassword) {
        if (userId == null || userId.isBlank()) {
            throw new BadRequestException("User id is required");
        }
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new BadRequestException("Current password is required");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new BadRequestException("New password is required");
        }
        if (currentPassword.equals(newPassword)) {
            throw new BadRequestException("New password must be different from the current password");
        }
        PasswordPolicy.validate(newPassword);

        var user = userStore.findById(userId)
                .or(() -> userStore.findByEmail(normalizeEmail(email)))
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.passwordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }

        userStore.updatePassword(user.id(), passwordEncoder.encode(newPassword));
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    public record AuthResult(String userId, String email, String token) {}
}

