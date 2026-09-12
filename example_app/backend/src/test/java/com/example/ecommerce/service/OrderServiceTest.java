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
        orderService = new OrderService(basketService, new PromoService(), new DeliveryFeeService());
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
        assertThat(preview.deliveryFee()).isEqualByComparingTo("5.00");
        assertThat(preview.totalAmount()).isEqualByComparingTo("54.98");
        assertThat(preview.promoCode()).isNull();
        assertThat(preview.discountAmount()).isEqualByComparingTo("0");
    }

    @Test
    void placeOrderShouldSucceedWhenAllLinesInStock() {
        ShippingInfo shippingInfo = shippingInfo();
        List<BasketItem> items = List.of(
                new BasketItem(product("p-1001", "24.99", true), 1),
                new BasketItem(product("p-1002", "79.00", true), 1)
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
        assertThat(placed.deliveryFee()).isEqualByComparingTo("5.00");
        assertThat(placed.totalAmount()).isEqualByComparingTo("108.99");
        assertThat(orderService.getOrder(USER_ID, placed.id())).isEqualTo(placed);
    }

    @Test
    void previewOrderShouldApplyValidPercentOffPromo() {
        ShippingInfo shippingInfo = shippingInfo();
        List<BasketItem> items = List.of(new BasketItem(product("p-1001", "24.99"), 2));
        when(basketService.snapshotItems(USER_ID)).thenReturn(items);
        when(basketService.calculateSubtotal(USER_ID)).thenReturn(new BigDecimal("49.98"));

        Order preview = orderService.previewOrder(USER_ID, shippingInfo, "SAVE10");

        assertThat(preview.promoCode()).isEqualTo("SAVE10");
        assertThat(preview.discountAmount()).isEqualByComparingTo("5.00");
        assertThat(preview.deliveryFee()).isEqualByComparingTo("5.00");
        assertThat(preview.totalAmount()).isEqualByComparingTo("49.98");
    }

    @Test
    void previewOrderShouldApplyKnownCityDeliveryFee() {
        ShippingInfo shippingInfo = shippingInfo();
        List<BasketItem> items = List.of(new BasketItem(product("p-1001", "24.99"), 2));
        when(basketService.snapshotItems(USER_ID)).thenReturn(items);
        when(basketService.calculateSubtotal(USER_ID)).thenReturn(new BigDecimal("49.98"));

        Order preview = orderService.previewOrder(USER_ID, shippingInfo);

        assertThat(shippingInfo.city()).isEqualTo("Austin");
        assertThat(preview.deliveryFee()).isEqualByComparingTo(DeliveryFeeService.AUSTIN_FEE);
        assertThat(preview.totalAmount()).isEqualByComparingTo("54.98");
    }

    @Test
    void getOrderShouldThrowWhenOrderDoesNotExist() {
        assertThatThrownBy(() -> orderService.getOrder(USER_ID, "missing-order"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Order not found: missing-order");
    }

    @Test
    void cancelOrderShouldChangePlacedCodToCancelled() {
        ShippingInfo shippingInfo = shippingInfo();
        List<BasketItem> items = List.of(new BasketItem(product("p-1001", "24.99", true), 1));
        when(basketService.snapshotItems(USER_ID)).thenReturn(items);
        when(basketService.calculateSubtotal(USER_ID)).thenReturn(new BigDecimal("24.99"));

        Order placed = orderService.placeOrder(USER_ID, shippingInfo);
        Order cancelled = orderService.cancelOrder(USER_ID, placed.id());

        assertThat(cancelled.status()).isEqualTo(OrderService.ORDER_STATUS_CANCELLED);
        assertThat(orderService.getOrder(USER_ID, placed.id()).status())
                .isEqualTo(OrderService.ORDER_STATUS_CANCELLED);
    }

    private static Product product(String id, String price) {
        return product(id, price, true);
    }

    private static Product product(String id, String price, boolean inStock) {
        return new Product(
                id,
                "Product " + id,
                "Description",
                new BigDecimal(price),
                "image-url",
                "Category",
                inStock
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
