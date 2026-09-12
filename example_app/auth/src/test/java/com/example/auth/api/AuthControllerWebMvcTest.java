package com.example.auth.api;

import com.example.auth.service.AuthService;
import com.example.auth.service.JwtService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Boilerplate API (WebMvc) harness for the auth service.
 * Incremental tracks (C10, C11, …) add Cover-only cases here.
 */
@WebMvcTest(AuthController.class)
@Tag("api")
class AuthControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @Test
    void signUpShouldReturnCreatedForValidPayload() throws Exception {
        when(authService.signUp(eq("user@example.com"), eq("P@ssw0rd_123!")))
                .thenReturn(new AuthService.AuthResult("u-1", "user@example.com", "jwt-token"));

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType("application/json")
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "P@ssw0rd_123!"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("u-1"))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }
}
