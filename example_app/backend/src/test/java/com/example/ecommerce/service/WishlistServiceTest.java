package com.example.ecommerce.service;

import com.example.ecommerce.domain.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("unit")
class WishlistServiceTest {

    private static final String USER_ID = "u-1";

    private ProductService productService;
    private BasketService basketService;
    private WishlistService wishlistService;

    @BeforeEach
    void setUp() {
        productService = mock(ProductService.class);
        basketService = mock(BasketService.class);
        wishlistService = new WishlistService(productService, basketService);
    }

    @Test
    void addListRemoveShouldManageUserWishlist() {
        Product mouse = product("p-1001", "24.99", true);
        Product keyboard = product("p-1002", "79.00", true);
        when(productService.getProductById("p-1001")).thenReturn(mouse);
        when(productService.getProductById("p-1002")).thenReturn(keyboard);

        assertThat(wishlistService.add(USER_ID, "p-1001")).isEqualTo(mouse);
        assertThat(wishlistService.add(USER_ID, "p-1002")).isEqualTo(keyboard);

        List<Product> listed = wishlistService.list(USER_ID);
        assertThat(listed).containsExactly(mouse, keyboard);
        assertThat(wishlistService.list("other-user")).isEmpty();

        wishlistService.remove(USER_ID, "p-1001");
        assertThat(wishlistService.list(USER_ID)).containsExactly(keyboard);
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
