package com.example.ecommerce.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Product(
        String id,
        String name,
        String description,
        BigDecimal price,
        String imageUrl,
        String category,
        boolean inStock,
        boolean featured
) {
    /** Demo stock ceiling when the product is marked in stock. */
    public static final int DEFAULT_AVAILABLE_STOCK = 10;

    /** Convenience for callers that do not set featured (defaults to false). */
    public Product(
            String id,
            String name,
            String description,
            BigDecimal price,
            String imageUrl,
            String category,
            boolean inStock
    ) {
        this(id, name, description, price, imageUrl, category, inStock, false);
    }

    public int availableStock() {
        return inStock ? DEFAULT_AVAILABLE_STOCK : 0;
    }
}
