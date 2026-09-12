package com.example.ecommerce.api.model;

import java.math.BigDecimal;

public record WishlistItemResponse(
        String productId,
        String productName,
        BigDecimal unitPrice,
        String imageUrl,
        String category,
        boolean inStock
) {
}
