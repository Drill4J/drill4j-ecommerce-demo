package com.example.ecommerce.api.model;

import java.math.BigDecimal;

public record ProductResponse(
        String id,
        String name,
        String description,
        BigDecimal price,
        String imageUrl,
        String category,
        boolean inStock
) {
}
