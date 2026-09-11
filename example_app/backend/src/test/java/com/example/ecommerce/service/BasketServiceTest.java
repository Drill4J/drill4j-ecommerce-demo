package com.example.ecommerce.service;

import com.example.ecommerce.domain.BasketItem;
import com.example.ecommerce.domain.Product;
import com.example.ecommerce.web.BadRequestException;
import com.example.ecommerce.web.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("unit")
class BasketServiceTest {

    private static final String USER_ID = "u-1";

    private ProductService productService;
    private BasketService basketService;

    @BeforeEach
    void setUp() {
        productService = mock(ProductService.class);
        basketService = new BasketService(productService);
    }

    @Test
    void addItemShouldAcceptQuantityWithinStockLimit() {
        Product mouse = product("p-1001", "24.99", true);
        when(productService.getProductById("p-1001")).thenReturn(mouse);

        basketService.addItem(USER_ID, "p-1001", Product.DEFAULT_AVAILABLE_STOCK);

        List<BasketItem> items = basketService.getBasketItems(USER_ID);
        assertThat(items).hasSize(1);
        assertThat(items.get(0).product().id()).isEqualTo("p-1001");
        assertThat(items.get(0).quantity()).isEqualTo(Product.DEFAULT_AVAILABLE_STOCK);
        assertThat(basketService.totalItems(USER_ID)).isEqualTo(Product.DEFAULT_AVAILABLE_STOCK);
    }

    @Test
    void addItemShouldMergeQuantityForSameProduct() {
        Product mouse = product("p-1001", "24.99", true);
        when(productService.getProductById("p-1001")).thenReturn(mouse);

        basketService.addItem(USER_ID, "p-1001", 1);
        basketService.addItem(USER_ID, "p-1001", 2);

        List<BasketItem> items = basketService.getBasketItems(USER_ID);
        assertThat(items).hasSize(1);
        assertThat(items.get(0).product().id()).isEqualTo("p-1001");
        assertThat(items.get(0).quantity()).isEqualTo(3);
        assertThat(basketService.totalItems(USER_ID)).isEqualTo(3);
    }

    @Test
    void calculateSubtotalShouldReturnSumOfLineTotals() {
        Product mouse = product("p-1001", "24.99", true);
        Product keyboard = product("p-1002", "79.00", true);
        when(productService.getProductById("p-1001")).thenReturn(mouse);
        when(productService.getProductById("p-1002")).thenReturn(keyboard);

        basketService.addItem(USER_ID, "p-1001", 2);
        basketService.addItem(USER_ID, "p-1002", 1);

        assertThat(basketService.calculateSubtotal(USER_ID)).isEqualByComparingTo("128.98");
    }

    @Test
    void updateItemQuantityShouldChangeExistingItemQuantity() {
        Product mouse = product("p-1001", "24.99", true);
        when(productService.getProductById("p-1001")).thenReturn(mouse);
        basketService.addItem(USER_ID, "p-1001", 1);

        basketService.updateItemQuantity(USER_ID, "p-1001", 4);

        assertThat(basketService.getBasketItems(USER_ID).get(0).quantity()).isEqualTo(4);
        assertThat(basketService.totalItems(USER_ID)).isEqualTo(4);
    }

    @Test
    void updateItemQuantityShouldThrowWhenItemDoesNotExist() {
        assertThatThrownBy(() -> basketService.updateItemQuantity(USER_ID, "missing", 2))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Basket item not found for product: missing");
    }

    @Test
    void removeItemShouldDeleteExistingItem() {
        Product mouse = product("p-1001", "24.99", true);
        when(productService.getProductById("p-1001")).thenReturn(mouse);
        basketService.addItem(USER_ID, "p-1001", 1);

        basketService.removeItem(USER_ID, "p-1001");

        assertThat(basketService.getBasketItems(USER_ID)).isEmpty();
        assertThat(basketService.totalItems(USER_ID)).isZero();
    }

    @Test
    void removeItemShouldThrowWhenItemDoesNotExist() {
        assertThatThrownBy(() -> basketService.removeItem(USER_ID, "missing"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Basket item not found for product: missing");
    }

    @Test
    void addItemShouldThrowWhenProductIsOutOfStock() {
        Product outOfStockProduct = product("p-2001", "10.00", false);
        when(productService.getProductById("p-2001")).thenReturn(outOfStockProduct);

        assertThatThrownBy(() -> basketService.addItem(USER_ID, "p-2001", 1))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Product is out of stock: p-2001");
    }

    @Test
    void ensureNotEmptyShouldThrowWhenBasketIsEmpty() {
        assertThatThrownBy(() -> basketService.ensureNotEmpty(USER_ID))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Basket is empty");
    }

    @Test
    void clearShouldRemoveAllItems() {
        Product mouse = product("p-1001", "24.99", true);
        when(productService.getProductById("p-1001")).thenReturn(mouse);
        basketService.addItem(USER_ID, "p-1001", 2);

        basketService.clear(USER_ID);

        assertThat(basketService.getBasketItems(USER_ID)).isEmpty();
        assertThat(basketService.calculateSubtotal(USER_ID)).isEqualByComparingTo(BigDecimal.ZERO);
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
}
