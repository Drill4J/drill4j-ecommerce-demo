package com.example.auth.api.model;

public record AuthResponse(
        String userId,
        String email,
        String token
) {}

