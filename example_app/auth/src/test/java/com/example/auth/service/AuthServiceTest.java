package com.example.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Boilerplate unit-test harness for the auth service.
 * Incremental tracks (C10, C11, …) add Cover-only cases here.
 */
@Tag("unit")
class AuthServiceTest {

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                new InMemoryUserStore(),
                new JwtService("unit-test-secret-at-least-32-chars!!", 3600)
        );
    }

    @Test
    void signUpShouldReturnTokenForNewUser() {
        AuthService.AuthResult result = authService.signUp(" boiLerplate@example.com ", "pass1234");

        assertThat(result.email()).isEqualTo("boilerplate@example.com");
        assertThat(result.userId()).isNotBlank();
        assertThat(result.token()).isNotBlank();
    }

    @Test
    void changePasswordShouldSucceedWithValidCurrentPassword() {
        AuthService.AuthResult signedUp = authService.signUp("c10-unit@example.com", "old-password");

        authService.changePassword(
                signedUp.userId(),
                signedUp.email(),
                "old-password",
                "P@ssw0rd_123!"
        );

        AuthService.AuthResult signedIn = authService.signIn("c10-unit@example.com", "P@ssw0rd_123!");
        assertThat(signedIn.userId()).isEqualTo(signedUp.userId());
        assertThat(signedIn.token()).isNotBlank();
    }
}
