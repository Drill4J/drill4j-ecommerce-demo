package com.example.ecommerce.domain;

import java.math.BigDecimal;
import java.util.List;

public record ReorderResult(
        List<BasketItem> items,
        int totalItems,
        BigDecimal subtotal,
        List<String> skippedProductIds
) {
}
