package com.example.ecommerce.api;

import com.example.ecommerce.auth.AuthClient;
import com.example.ecommerce.auth.AuthPrincipal;
import com.example.ecommerce.domain.Product;
import com.example.ecommerce.service.WishlistService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WishlistController.class)
@Tag("api")
class WishlistControllerWebMvcTest {

    private static final AuthPrincipal PRINCIPAL = new AuthPrincipal("u-1", "user@example.com");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WishlistService wishlistService;

    @MockBean
    private AuthClient authClient;

    @Test
    void wishlistCrudHappyPathShouldSucceed() throws Exception {
        Product mouse = product("p-1001", "Wireless Mouse", "24.99", true);
        when(authClient.verify(anyString())).thenReturn(PRINCIPAL);
        when(wishlistService.add(eq(PRINCIPAL.userId()), eq("p-1001"))).thenReturn(mouse);
        when(wishlistService.list(PRINCIPAL.userId())).thenReturn(List.of(mouse));
        doNothing().when(wishlistService).remove(eq(PRINCIPAL.userId()), eq("p-1001"));

        mockMvc.perform(post("/api/v1/wishlist/items")
                        .header("Authorization", "Bearer test")
                        .contentType("application/json")
                        .content("""
                                {
                                  "productId": "p-1001"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value("p-1001"))
                .andExpect(jsonPath("$.productName").value("Wireless Mouse"))
                .andExpect(jsonPath("$.unitPrice").value(24.99))
                .andExpect(jsonPath("$.inStock").value(true));

        mockMvc.perform(get("/api/v1/wishlist")
                        .header("Authorization", "Bearer test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value("p-1001"))
                .andExpect(jsonPath("$[0].productName").value("Wireless Mouse"))
                .andExpect(jsonPath("$[0].unitPrice").value(24.99));

        mockMvc.perform(delete("/api/v1/wishlist/items/p-1001")
                        .header("Authorization", "Bearer test"))
                .andExpect(status().isNoContent());

        verify(wishlistService).add(PRINCIPAL.userId(), "p-1001");
        verify(wishlistService).list(PRINCIPAL.userId());
        verify(wishlistService).remove(PRINCIPAL.userId(), "p-1001");
    }

    private static Product product(String id, String name, String price, boolean inStock) {
        return new Product(
                id,
                name,
                "Description",
                new BigDecimal(price),
                "image-url",
                "Category",
                inStock
        );
    }
}
