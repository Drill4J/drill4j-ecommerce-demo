package com.example.ecommerce.service;

import com.example.ecommerce.domain.BasketItem;
import com.example.ecommerce.domain.Order;
import com.example.ecommerce.domain.Product;
import com.example.ecommerce.domain.ShippingInfo;
import com.example.ecommerce.web.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
class OrderServiceTest {

    private static final String USER_ID = "u-1";

    private BasketService basketService;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        basketService = mock(BasketService.class);
        orderService = new OrderService(basketService);
    }

    @Test
    void previewOrderShouldReturnSnapshotWithPreviewIdAndCodPayment() {
        ShippingInfo shippingInfo = shippingInfo();
        List<BasketItem> items = List.of(new BasketItem(product("p-1001", "24.99"), 2));
        when(basketService.snapshotItems(USER_ID)).thenReturn(items);
        when(basketService.calculateSubtotal(USER_ID)).thenReturn(new BigDecimal("49.98"));

        Order preview = orderService.previewOrder(USER_ID, shippingInfo);

        verify(basketService).ensureNotEmpty(USER_ID);
        assertThat(preview.id()).isEqualTo("preview");
        assertThat(preview.status()).isEqualTo(OrderService.ORDER_STATUS_PLACED_COD);
        assertThat(preview.paymentMethod()).isEqualTo(OrderService.PAYMENT_METHOD_COD);
        assertThat(preview.shippingInfo()).isEqualTo(shippingInfo);
        assertThat(preview.items()).containsExactlyElementsOf(items);
        assertThat(preview.totalAmount()).isEqualByComparingTo("49.98");
    }

    @Test
    void placeOrderShouldStoreOrderAndClearBasket() {
        ShippingInfo shippingInfo = shippingInfo();
        List<BasketItem> items = List.of(
                new BasketItem(product("p-1001", "24.99"), 1),
                new BasketItem(product("p-1002", "79.00"), 1)
        );
        when(basketService.snapshotItems(USER_ID)).thenReturn(items);
        when(basketService.calculateSubtotal(USER_ID)).thenReturn(new BigDecimal("103.99"));

        Order placed = orderService.placeOrder(USER_ID, shippingInfo);

        verify(basketService).ensureNotEmpty(USER_ID);
        verify(basketService).clear(USER_ID);
        assertThat(placed.id()).isNotBlank();
        assertThat(placed.status()).isEqualTo(OrderService.ORDER_STATUS_PLACED_COD);
        assertThat(placed.paymentMethod()).isEqualTo(OrderService.PAYMENT_METHOD_COD);
        assertThat(placed.items()).containsExactlyElementsOf(items);
        assertThat(placed.totalAmount()).isEqualByComparingTo("103.99");
        assertThat(orderService.getOrder(USER_ID, placed.id())).isEqualTo(placed);
    }

    @Test
    void getOrderShouldThrowWhenOrderDoesNotExist() {
        assertThatThrownBy(() -> orderService.getOrder(USER_ID, "missing-order"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Order not found: missing-order");
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
