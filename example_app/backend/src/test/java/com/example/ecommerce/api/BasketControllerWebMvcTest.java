package com.example.ecommerce.api;

import com.example.ecommerce.auth.AuthClient;
import com.example.ecommerce.auth.AuthPrincipal;
import com.example.ecommerce.domain.BasketItem;
import com.example.ecommerce.domain.Product;
import com.example.ecommerce.service.BasketService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BasketController.class)
@Tag("api")
class BasketControllerWebMvcTest {

    private static final AuthPrincipal PRINCIPAL = new AuthPrincipal("u-1", "user@example.com");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BasketService basketService;

    @MockBean
    private AuthClient authClient;

    @Test
    void getBasketShouldReturnBasketPayload() throws Exception {
        Product mouse = product("p-1001", "Wireless Mouse", "24.99");
        when(authClient.verify(anyString())).thenReturn(PRINCIPAL);
        when(basketService.getBasketItems(PRINCIPAL.userId())).thenReturn(List.of(new BasketItem(mouse, 2)));
        when(basketService.totalItems(PRINCIPAL.userId())).thenReturn(2);
        when(basketService.calculateSubtotal(PRINCIPAL.userId())).thenReturn(new BigDecimal("49.98"));

        mockMvc.perform(get("/api/v1/basket")
                        .header("Authorization", "Bearer test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productId").value("p-1001"))
                .andExpect(jsonPath("$.items[0].productName").value("Wireless Mouse"))
                .andExpect(jsonPath("$.items[0].unitPrice").value(24.99))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].lineTotal").value(49.98))
                .andExpect(jsonPath("$.totalItems").value(2))
                .andExpect(jsonPath("$.subtotal").value(49.98));
    }

    @Test
    void addBasketItemWithinStockLimitShouldReturnCreated() throws Exception {
        int quantityWithinLimit = 5;
        Product keyboard = product("p-1002", "Mechanical Keyboard", "79.00");
        when(authClient.verify(anyString())).thenReturn(PRINCIPAL);
        doNothing().when(basketService).addItem(eq(PRINCIPAL.userId()), eq("p-1002"), eq(quantityWithinLimit));
        when(basketService.getBasketItems(PRINCIPAL.userId()))
                .thenReturn(List.of(new BasketItem(keyboard, quantityWithinLimit)));
        when(basketService.totalItems(PRINCIPAL.userId())).thenReturn(quantityWithinLimit);
        when(basketService.calculateSubtotal(PRINCIPAL.userId())).thenReturn(new BigDecimal("395.00"));

        mockMvc.perform(post("/api/v1/basket/items")
                        .header("Authorization", "Bearer test")
                        .contentType("application/json")
                        .content("""
                                {
                                  "productId": "p-1002",
                                  "quantity": 5
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items[0].productId").value("p-1002"))
                .andExpect(jsonPath("$.items[0].quantity").value(quantityWithinLimit))
                .andExpect(jsonPath("$.items[0].lineTotal").value(395.00))
                .andExpect(jsonPath("$.totalItems").value(quantityWithinLimit))
                .andExpect(jsonPath("$.subtotal").value(395.00));

        verify(basketService).addItem(PRINCIPAL.userId(), "p-1002", quantityWithinLimit);
    }

    @Test
    void updateBasketItemShouldReturnUpdatedBasket() throws Exception {
        Product keyboard = product("p-1002", "Mechanical Keyboard", "79.00");
        when(authClient.verify(anyString())).thenReturn(PRINCIPAL);
        doNothing().when(basketService).updateItemQuantity(eq(PRINCIPAL.userId()), eq("p-1002"), eq(1));
        when(basketService.getBasketItems(PRINCIPAL.userId())).thenReturn(List.of(new BasketItem(keyboard, 1)));
        when(basketService.totalItems(PRINCIPAL.userId())).thenReturn(1);
        when(basketService.calculateSubtotal(PRINCIPAL.userId())).thenReturn(new BigDecimal("79.00"));

        mockMvc.perform(patch("/api/v1/basket/items/p-1002")
                        .header("Authorization", "Bearer test")
                        .contentType("application/json")
                        .content("""
                                {
                                  "quantity": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productId").value("p-1002"))
                .andExpect(jsonPath("$.items[0].quantity").value(1))
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.subtotal").value(79.00));

        verify(basketService).updateItemQuantity(PRINCIPAL.userId(), "p-1002", 1);
    }

    @Test
    void removeBasketItemShouldReturnNoContent() throws Exception {
        when(authClient.verify(anyString())).thenReturn(PRINCIPAL);
        doNothing().when(basketService).removeItem(PRINCIPAL.userId(), "p-1001");

        mockMvc.perform(delete("/api/v1/basket/items/p-1001")
                        .header("Authorization", "Bearer test"))
                .andExpect(status().isNoContent());

        verify(basketService).removeItem(PRINCIPAL.userId(), "p-1001");
    }

    private static Product product(String id, String name, String price) {
        return new Product(
                id,
                name,
                "Description",
                new BigDecimal(price),
                "image-url",
                "Category",
                true
        );
    }
}
