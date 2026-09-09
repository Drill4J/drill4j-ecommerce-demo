package com.example.auth.domain;

public record User(
        String id,
        String email,
        String passwordHash
) {}

