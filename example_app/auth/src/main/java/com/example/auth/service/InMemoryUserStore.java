package com.example.auth.service;

import com.example.auth.domain.User;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryUserStore {
    private final ConcurrentHashMap<String, User> byEmail = new ConcurrentHashMap<>();

    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(byEmail.get(normalizeEmail(email)));
    }

    public User save(User user) {
        byEmail.put(normalizeEmail(user.email()), user);
        return user;
    }

    public boolean existsByEmail(String email) {
        return byEmail.containsKey(normalizeEmail(email));
    }

    public Optional<User> findById(String userId) {
        if (userId == null || userId.isBlank()) {
            return Optional.empty();
        }
        return byEmail.values().stream()
                .filter(user -> userId.equals(user.id()))
                .findFirst();
    }

    public User updatePassword(String userId, String newPasswordHash) {
        User existing = findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        User updated = new User(existing.id(), existing.email(), newPasswordHash);
        byEmail.put(normalizeEmail(updated.email()), updated);
        return updated;
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}

