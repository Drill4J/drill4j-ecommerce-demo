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
}
