package com.example.auth.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Tag("api")
class AuthChangePasswordIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void changePasswordShouldReturn200AndAllowSignInWithNewPassword() throws Exception {
        String email = "c10-api@example.com";
        String currentPassword = "old-password";
        String newPassword = "P@ssw0rd_123!";

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType("application/json")
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, currentPassword)))
                .andExpect(status().isCreated());

        MvcResult signInResult = mockMvc.perform(post("/api/v1/auth/signin")
                        .contentType("application/json")
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, currentPassword)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        JsonNode signInBody = objectMapper.readTree(signInResult.getResponse().getContentAsString());
        String token = signInBody.get("token").asText();

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {
                                  "currentPassword": "%s",
                                  "newPassword": "%s"
                                }
                                """.formatted(currentPassword, newPassword)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));

        mockMvc.perform(post("/api/v1/auth/signin")
                        .contentType("application/json")
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, newPassword)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value(email));
    }
}
