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

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}

