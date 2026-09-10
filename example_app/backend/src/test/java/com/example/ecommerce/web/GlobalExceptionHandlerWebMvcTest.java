package com.example.ecommerce.web;

import jakarta.validation.Valid;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("api")
class GlobalExceptionHandlerWebMvcTest {

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new TestErrorController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void shouldMapNotFoundExceptionTo404() throws Exception {
        mockMvc.perform(get("/test-errors/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("missing resource"));
    }

    @Test
    void shouldMapBadRequestExceptionTo400() throws Exception {
        mockMvc.perform(get("/test-errors/bad-request"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("bad input"));
    }

    @Test
    void shouldMapConstraintViolationTo400() throws Exception {
        mockMvc.perform(get("/test-errors/constraint-violation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("must be numeric"));
    }

    @Test
    void shouldMapMethodArgumentNotValidTo400WithFieldNames() throws Exception {
        mockMvc.perform(post("/test-errors/validation")
                        .contentType("application/json")
                        .content("""
                                {
                                  "fullName": "",
                                  "city": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    String body = result.getResponse().getContentAsString();
                    assertThat(body).contains("Validation failed for:");
                    assertThat(body).contains("fullName");
                    assertThat(body).contains("city");
                });
    }

    @Validated
    @RestController
    @RequestMapping("/test-errors")
    static class TestErrorController {

        @GetMapping("/not-found")
        String notFound() {
            throw new NotFoundException("missing resource");
        }

        @GetMapping("/bad-request")
        String badRequest() {
            throw new BadRequestException("bad input");
        }

        @GetMapping("/constraint-violation")
        String constraintViolation() {
            throw new ConstraintViolationException("must be numeric", null);
        }

        @PostMapping("/validation")
        String validation(@Valid @RequestBody ValidationRequest request) {
            return request.fullName();
        }
    }

    record ValidationRequest(
            @NotBlank String fullName,
            @NotBlank String city
    ) {
    }
}
