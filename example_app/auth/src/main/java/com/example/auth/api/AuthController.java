package com.example.auth.api;

import com.example.auth.api.model.AuthResponse;
import com.example.auth.api.model.SignInRequest;
import com.example.auth.api.model.SignUpRequest;
import com.example.auth.api.model.VerifyResponse;
import com.example.auth.service.AuthService;
import com.example.auth.service.JwtService;
import com.example.auth.web.UnauthorizedException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signUp(@Valid @RequestBody SignUpRequest req) {
        var result = authService.signUp(req.email(), req.password());

        return ResponseEntity.status(201)
                .header("Set-Cookie", accessTokenCookie(result.token()).toString())
                .body(new AuthResponse(result.userId(), result.email(), result.token()));
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> signIn(@Valid @RequestBody SignInRequest req) {
        var result = authService.signIn(req.email(), req.password());

        return ResponseEntity.ok()
                .header("Set-Cookie", accessTokenCookie(result.token()).toString())
                .body(new AuthResponse(result.userId(), result.email(), result.token()));
    }

    @PostMapping("/verify")
    public VerifyResponse verify(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        String token = extractBearerToken(authorizationHeader);
        try {
            var principal = jwtService.verifyToken(token);
            return new VerifyResponse(principal.userId(), principal.email());
        } catch (Exception ex) {
            throw new UnauthorizedException("Invalid or expired token");
        }
    }

    private ResponseCookie accessTokenCookie(String token) {
        return ResponseCookie.from("access_token", token)
                .path("/")
                .maxAge(Duration.ofDays(1))
                .sameSite("Lax")
                .build();
    }

    private static String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new UnauthorizedException("Missing Authorization header");
        }
        String prefix = "Bearer ";
        if (!authorizationHeader.startsWith(prefix) || authorizationHeader.length() <= prefix.length()) {
            throw new UnauthorizedException("Authorization header must be a Bearer token");
        }
        return authorizationHeader.substring(prefix.length()).trim();
    }
}

