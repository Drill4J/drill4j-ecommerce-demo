package com.example.ecommerce.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class AuthClient {
    private final RestClient restClient;

    public AuthClient(@Value("${auth.base-url}") String authBaseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(authBaseUrl)
                .build();
    }

    public AuthPrincipal verify(String bearerToken) throws RestClientResponseException {
        VerifyResponse response = restClient.post()
                .uri("/auth/verify")
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(VerifyResponse.class);

        if (response == null) {
            throw new RestClientResponseException(
                    "Auth service returned empty response",
                    502,
                    "Bad Gateway",
                    null,
                    null,
                    null
            );
        }

        return new AuthPrincipal(response.userId(), response.email());
    }

    public record VerifyResponse(String userId, String email) {}
}

