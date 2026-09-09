package com.example.ecommerce.api.model;

import java.math.BigDecimal;
import java.util.List;

public record BasketResponse(
        List<BasketItemResponse> items,
        int totalItems,
        BigDecimal subtotal
) {
}
