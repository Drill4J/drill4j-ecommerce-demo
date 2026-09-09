package com.example.ecommerce.auth;

import com.example.ecommerce.web.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final AuthClient authClient;

    public AuthInterceptor(AuthClient authClient) {
        this.authClient = authClient;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.isBlank()) {
            throw new UnauthorizedException("Missing Authorization header");
        }
        if (!authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Authorization header must be a Bearer token");
        }

        try {
            AuthPrincipal principal = authClient.verify(authHeader);
            request.setAttribute(AuthRequestAttributes.PRINCIPAL, principal);
            return true;
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 401) {
                throw new UnauthorizedException("Invalid or expired token");
            }
            throw new UnauthorizedException("Auth service unavailable");
        }
    }
}

