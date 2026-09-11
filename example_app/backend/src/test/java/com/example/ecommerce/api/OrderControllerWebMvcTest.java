package com.example.ecommerce.api;

import com.example.ecommerce.auth.AuthClient;
import com.example.ecommerce.auth.AuthPrincipal;
import com.example.ecommerce.domain.BasketItem;
import com.example.ecommerce.domain.Order;
import com.example.ecommerce.domain.Product;
import com.example.ecommerce.domain.SavedAddress;
import com.example.ecommerce.domain.ShippingInfo;
import com.example.ecommerce.service.AddressService;
import com.example.ecommerce.service.OrderService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Tag("api")
class OrderControllerWebMvcTest {

    private static final AuthPrincipal PRINCIPAL = new AuthPrincipal("u-1", "user@example.com");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private AddressService addressService;

    @MockBean
    private AuthClient authClient;

    @Test
    void previewCheckoutShouldMapRequestAndReturnPreviewPayload() throws Exception {
        ShippingInfo shippingInfo = shippingInfo();
        List<BasketItem> items = List.of(new BasketItem(product("p-1001", "24.99"), 2));
        Order preview = new Order(
                "preview",
                OrderService.ORDER_STATUS_PLACED_COD,
                shippingInfo,
                items,
                new BigDecimal("54.98"),
                OrderService.PAYMENT_METHOD_COD,
                null,
                BigDecimal.ZERO,
                new BigDecimal("5.00")
        );
        when(authClient.verify(anyString())).thenReturn(PRINCIPAL);
        when(orderService.previewOrder(anyString(), any(ShippingInfo.class), isNull())).thenReturn(preview);

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
                .andExpect(jsonPath("$.shippingInfo.fullName").value("Jane Doe"))
                .andExpect(jsonPath("$.items[0].productId").value("p-1001"))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].lineTotal").value(49.98))
                .andExpect(jsonPath("$.deliveryFee").value(5.00))
                .andExpect(jsonPath("$.totalAmount").value(54.98))
                .andExpect(jsonPath("$.paymentMethod").value("CASH_ON_DELIVERY"));

        verify(orderService).previewOrder(PRINCIPAL.userId(), shippingInfo, null);
    }

    @Test
    void previewCheckoutShouldReturnDeliveryFeeForKnownCity() throws Exception {
        ShippingInfo shippingInfo = shippingInfo();
        List<BasketItem> items = List.of(new BasketItem(product("p-1001", "24.99"), 2));
        Order preview = new Order(
                "preview",
                OrderService.ORDER_STATUS_PLACED_COD,
                shippingInfo,
                items,
                new BigDecimal("54.98"),
                OrderService.PAYMENT_METHOD_COD,
                null,
                BigDecimal.ZERO,
                new BigDecimal("5.00")
        );
        when(authClient.verify(anyString())).thenReturn(PRINCIPAL);
        when(orderService.previewOrder(anyString(), any(ShippingInfo.class), isNull())).thenReturn(preview);

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

        verify(orderService).previewOrder(PRINCIPAL.userId(), shippingInfo, null);
    }

    @Test
    void placeOrderShouldReturnCreatedOrderPayload() throws Exception {
        ShippingInfo shippingInfo = shippingInfo();
        List<BasketItem> items = List.of(new BasketItem(product("p-1002", "79.00"), 1));
        Order placed = new Order(
                "ord-1",
                OrderService.ORDER_STATUS_PLACED_COD,
                shippingInfo,
                items,
                new BigDecimal("84.00"),
                OrderService.PAYMENT_METHOD_COD,
                null,
                BigDecimal.ZERO,
                new BigDecimal("5.00")
        );
        when(authClient.verify(anyString())).thenReturn(PRINCIPAL);
        when(orderService.placeOrder(anyString(), any(ShippingInfo.class), isNull())).thenReturn(placed);

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
                .andExpect(jsonPath("$.orderId").value("ord-1"))
                .andExpect(jsonPath("$.status").value("PLACED_COD"))
                .andExpect(jsonPath("$.shippingInfo.city").value("Austin"))
                .andExpect(jsonPath("$.items[0].productId").value("p-1002"))
                .andExpect(jsonPath("$.items[0].lineTotal").value(79.00))
                .andExpect(jsonPath("$.deliveryFee").value(5.00))
                .andExpect(jsonPath("$.totalAmount").value(84.00))
                .andExpect(jsonPath("$.paymentMethod").value("CASH_ON_DELIVERY"));

        verify(orderService).placeOrder(PRINCIPAL.userId(), shippingInfo, null);
    }

    @Test
    void placeOrderWithSavedAddressIdShouldResolveShippingAndPlace() throws Exception {
        ShippingInfo shippingInfo = shippingInfo();
        SavedAddress saved = new SavedAddress(
                "addr-1",
                shippingInfo.fullName(),
                shippingInfo.phoneNumber(),
                shippingInfo.addressLine(),
                shippingInfo.city(),
                shippingInfo.postalCode(),
                true
        );
        List<BasketItem> items = List.of(new BasketItem(product("p-1002", "79.00"), 1));
        Order placed = new Order(
                "ord-addr",
                OrderService.ORDER_STATUS_PLACED_COD,
                shippingInfo,
                items,
                new BigDecimal("84.00"),
                OrderService.PAYMENT_METHOD_COD,
                null,
                BigDecimal.ZERO,
                new BigDecimal("5.00")
        );
        when(authClient.verify(anyString())).thenReturn(PRINCIPAL);
        when(addressService.get(PRINCIPAL.userId(), "addr-1")).thenReturn(saved);
        when(orderService.placeOrder(anyString(), any(ShippingInfo.class), isNull())).thenReturn(placed);

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer test")
                        .contentType("application/json")
                        .content("""
                                {
                                  "addressId": "addr-1"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value("ord-addr"))
                .andExpect(jsonPath("$.shippingInfo.city").value("Austin"))
                .andExpect(jsonPath("$.totalAmount").value(84.00));

        verify(addressService).get(PRINCIPAL.userId(), "addr-1");
        verify(orderService).placeOrder(PRINCIPAL.userId(), shippingInfo, null);
    }

    @Test
    void getOrderShouldReturnOrderPayload() throws Exception {
        ShippingInfo shippingInfo = shippingInfo();
        List<BasketItem> items = List.of(new BasketItem(product("p-1003", "129.50"), 1));
        Order order = new Order(
                "ord-xyz",
                OrderService.ORDER_STATUS_PLACED_COD,
                shippingInfo,
                items,
                new BigDecimal("134.50"),
                OrderService.PAYMENT_METHOD_COD,
                null,
                BigDecimal.ZERO,
                new BigDecimal("5.00")
        );
        when(authClient.verify(anyString())).thenReturn(PRINCIPAL);
        when(orderService.getOrder(PRINCIPAL.userId(), "ord-xyz")).thenReturn(order);

        mockMvc.perform(get("/api/v1/orders/ord-xyz")
                        .header("Authorization", "Bearer test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("ord-xyz"))
                .andExpect(jsonPath("$.items[0].productId").value("p-1003"))
                .andExpect(jsonPath("$.deliveryFee").value(5.00))
                .andExpect(jsonPath("$.totalAmount").value(134.50));

        verify(orderService).getOrder(eq(PRINCIPAL.userId()), eq("ord-xyz"));
    }

    private static Product product(String id, String price) {
        return new Product(
                id,
                "Product " + id,
                "Description",
                new BigDecimal(price),
                "image-url",
                "Category",
                true
        );
    }

    private static ShippingInfo shippingInfo() {
        return new ShippingInfo(
                "Jane Doe",
                "+1-555-100-100",
                "Main street 10",
                "Austin",
                "73301"
        );
    }
}
