package com.example.auth.api.model;

public record VerifyResponse(
        String userId,
        String email
) {}

