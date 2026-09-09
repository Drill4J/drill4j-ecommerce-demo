package com.example.ecommerce.domain;

import java.math.BigDecimal;
import java.util.List;

public record Order(
        String id,
        String status,
        ShippingInfo shippingInfo,
        List<BasketItem> items,
        BigDecimal totalAmount,
        String paymentMethod
) {
}
