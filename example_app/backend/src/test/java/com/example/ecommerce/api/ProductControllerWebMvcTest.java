package com.example.ecommerce.api;

import com.example.ecommerce.auth.AuthClient;
import com.example.ecommerce.domain.Product;
import com.example.ecommerce.service.ProductService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("api")
@WebMvcTest(ProductController.class)
class ProductControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private AuthClient authClient;

    @Test
    void listProductsShouldReturnFilteredBodyForKnownCategory() throws Exception {
        when(productService.listProducts(eq(0), eq(10), isNull(), eq("Gaming")))
                .thenReturn(List.of(
                        new Product("p-1001", "Pad", "Desc", new BigDecimal("19.99"), "img", "Gaming", true)
                ));

        mockMvc.perform(get("/api/v1/products")
                        .param("page", "0")
                        .param("size", "10")
                        .param("category", "Gaming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("p-1001"))
                .andExpect(jsonPath("$[0].category").value("Gaming"))
                .andExpect(jsonPath("$[0].name").value("Pad"));
    }

    @Test
    void listFeaturedProductsShouldReturnNonEmptyPayload() throws Exception {
        when(productService.listFeatured())
                .thenReturn(List.of(
                        new Product("p-1001", "Pad", "Desc", new BigDecimal("19.99"), "img", "Gaming", true, true),
                        new Product("p-1002", "Headset", "Desc", new BigDecimal("49.99"), "img", "Audio", true, true)
                ));

        mockMvc.perform(get("/api/v1/products/featured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$[0].id").value("p-1001"))
                .andExpect(jsonPath("$[1].id").value("p-1002"));
    }
}
