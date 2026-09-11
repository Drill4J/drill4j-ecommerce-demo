package com.example.ecommerce.domain;

import java.math.BigDecimal;

public record Product(
        String id,
        String name,
        String description,
        BigDecimal price,
        String imageUrl,
        String category,
        boolean inStock
) {
    /** Demo stock ceiling when the product is marked in stock. */
    public static final int DEFAULT_AVAILABLE_STOCK = 10;

    public int availableStock() {
        return inStock ? DEFAULT_AVAILABLE_STOCK : 0;
    }
}
