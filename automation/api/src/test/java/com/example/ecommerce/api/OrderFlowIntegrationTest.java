package com.example.ecommerce.api;

import com.example.ecommerce.auth.AuthClient;
import com.example.ecommerce.auth.AuthPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderFlowIntegrationTest {

    private static final AuthPrincipal PRINCIPAL = new AuthPrincipal("u-1", "user@example.com");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthClient authClient;

    @Test
    void shouldAddItemAndPlaceCodOrder() throws Exception {
        when(authClient.verify(anyString())).thenReturn(PRINCIPAL);

        mockMvc.perform(post("/api/v1/basket/items")
                        .header("Authorization", "Bearer test")
                        .contentType("application/json")
                        .content("""
                                {
                                  "productId": "p-1001",
                                  "quantity": 2
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalItems").value(2));

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer test")
                        .contentType("application/json")
                        .content("""
                                {
                                  "shippingInfo": {
                                    "fullName": "Jane Doe",
                                    "phoneNumber": "+1-555-100-100",
                                    "addressLine": "Main street 10",
                                    "city": "Austin",
                                    "postalCode": "73301"
                                  }
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PLACED_COD"))
                .andExpect(jsonPath("$.paymentMethod").value("CASH_ON_DELIVERY"));
    }

    @Test
    void shouldUpdateBasketQuantity() throws Exception {
        when(authClient.verify(anyString())).thenReturn(PRINCIPAL);

        mockMvc.perform(post("/api/v1/basket/items")
                        .header("Authorization", "Bearer test")
                        .contentType("application/json")
                        .content("""
                                {
                                  "productId": "p-1002",
                                  "quantity": 1
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(patch("/api/v1/basket/items/p-1002")
                        .header("Authorization", "Bearer test")
                        .contentType("application/json")
                        .content("""
                                {
                                  "quantity": 3
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(3));

        mockMvc.perform(get("/api/v1/basket")
                        .header("Authorization", "Bearer test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productId").value("p-1002"))
                .andExpect(jsonPath("$.items[0].quantity").value(3));
    }
}
