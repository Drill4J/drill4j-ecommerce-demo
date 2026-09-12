package com.example.ecommerce.api;

import com.example.ecommerce.auth.AuthClient;
import com.example.ecommerce.auth.AuthPrincipal;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Tag;
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
@Tag("api")
class OrderFlowIntegrationTest {

    private static final AuthPrincipal PRINCIPAL = new AuthPrincipal("u-1", "user@example.com");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    void previewAndPlaceWithPromoCodeShouldReduceTotal() throws Exception {
        AuthPrincipal promoUser = new AuthPrincipal("u-promo", "promo@example.com");
        when(authClient.verify(anyString())).thenReturn(promoUser);

        mockMvc.perform(post("/api/v1/basket/items")
                        .header("Authorization", "Bearer test")
                        .contentType("application/json")
                        .content("""
                                {
                                  "productId": "p-1001",
                                  "quantity": 2
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/checkout/preview")
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
                                  },
                                  "promoCode": "SAVE10"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.promoCode").value("SAVE10"))
                .andExpect(jsonPath("$.discountAmount").value(5.00))
                .andExpect(jsonPath("$.deliveryFee").value(5.00))
                .andExpect(jsonPath("$.totalAmount").value(49.98));

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
                                  },
                                  "promoCode": "SAVE10"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.promoCode").value("SAVE10"))
                .andExpect(jsonPath("$.discountAmount").value(5.00))
                .andExpect(jsonPath("$.deliveryFee").value(5.00))
                .andExpect(jsonPath("$.totalAmount").value(49.98));
    }

    @Test
    void previewCheckoutShouldIncludeDeliveryFeeForKnownCity() throws Exception {
        AuthPrincipal feeUser = new AuthPrincipal("u-fee", "fee@example.com");
        when(authClient.verify(anyString())).thenReturn(feeUser);

        mockMvc.perform(post("/api/v1/basket/items")
                        .header("Authorization", "Bearer test")
                        .contentType("application/json")
                        .content("""
                                {
                                  "productId": "p-1001",
                                  "quantity": 2
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/checkout/preview")
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
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shippingInfo.city").value("Austin"))
                .andExpect(jsonPath("$.deliveryFee").value(5.00))
                .andExpect(jsonPath("$.totalAmount").value(54.98));
    }

    @Test
    void shouldCreateListAndPlaceOrderUsingSavedAddress() throws Exception {
        AuthPrincipal addressUser = new AuthPrincipal("u-addr", "addr@example.com");
        when(authClient.verify(anyString())).thenReturn(addressUser);

        String createResponse = mockMvc.perform(post("/api/v1/addresses")
                        .header("Authorization", "Bearer test")
                        .contentType("application/json")
                        .content("""
                                {
                                  "fullName": "Jane Doe",
                                  "phoneNumber": "+1-555-100-100",
                                  "addressLine": "Main street 10",
                                  "city": "Austin",
                                  "postalCode": "73301",
                                  "isDefault": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.city").value("Austin"))
                .andExpect(jsonPath("$.isDefault").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode created = objectMapper.readTree(createResponse);
        String addressId = created.get("id").asText();

        mockMvc.perform(get("/api/v1/addresses")
                        .header("Authorization", "Bearer test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(addressId))
                .andExpect(jsonPath("$[0].fullName").value("Jane Doe"));

        mockMvc.perform(post("/api/v1/basket/items")
                        .header("Authorization", "Bearer test")
                        .contentType("application/json")
                        .content("""
                                {
                                  "productId": "p-1001",
                                  "quantity": 1
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer test")
                        .contentType("application/json")
                        .content("""
                                {
                                  "addressId": "%s"
                                }
                                """.formatted(addressId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PLACED_COD"))
                .andExpect(jsonPath("$.shippingInfo.city").value("Austin"))
                .andExpect(jsonPath("$.shippingInfo.fullName").value("Jane Doe"));
    }

    @Test
    void previewBelowMinimumShouldReturnBadRequest() throws Exception {
        AuthPrincipal minUser = new AuthPrincipal("u-min-below", "min-below@example.com");
        when(authClient.verify(anyString())).thenReturn(minUser);

        mockMvc.perform(post("/api/v1/basket/items")
                        .header("Authorization", "Bearer test")
                        .contentType("application/json")
                        .content("""
                                {
                                  "productId": "p-1040",
                                  "quantity": 1
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/checkout/preview")
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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Order total is below the minimum of 25.00"));
    }

    @Test
    void placeAboveMinimumShouldSucceed() throws Exception {
        AuthPrincipal minUser = new AuthPrincipal("u-min-above", "min-above@example.com");
        when(authClient.verify(anyString())).thenReturn(minUser);

        mockMvc.perform(post("/api/v1/basket/items")
                        .header("Authorization", "Bearer test")
                        .contentType("application/json")
                        .content("""
                                {
                                  "productId": "p-1001",
                                  "quantity": 1
                                }
                                """))
                .andExpect(status().isCreated());

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
                .andExpect(jsonPath("$.totalAmount").value(29.99));
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
