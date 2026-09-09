package com.example.ecommerce.api.model;

import java.math.BigDecimal;

public record BasketItemResponse(
        String productId,
        String productName,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal
) {
}
