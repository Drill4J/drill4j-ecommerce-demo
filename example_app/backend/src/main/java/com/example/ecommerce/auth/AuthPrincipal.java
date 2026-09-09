package com.example.ecommerce.auth;

public record AuthPrincipal(
        String userId,
        String email
) {}

