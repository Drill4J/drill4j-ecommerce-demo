package com.example.auth.service;

import com.example.auth.web.BadRequestException;

/**
 * Password rules for change-password (C10) and later signup (C11).
 * Accepts passwords such as {@code P@ssw0rd_123!}.
 */
public final class PasswordPolicy {
    private PasswordPolicy() {}

    public static void validate(String password) {
        if (password == null || password.length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters");
        }
        if (!password.chars().anyMatch(Character::isUpperCase)) {
            throw new BadRequestException("Password must contain an uppercase letter");
        }
        if (!password.chars().anyMatch(Character::isLowerCase)) {
            throw new BadRequestException("Password must contain a lowercase letter");
        }
        if (!password.chars().anyMatch(Character::isDigit)) {
            throw new BadRequestException("Password must contain a digit");
        }
        if (!password.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch))) {
            throw new BadRequestException("Password must contain a special character");
        }
    }
}
