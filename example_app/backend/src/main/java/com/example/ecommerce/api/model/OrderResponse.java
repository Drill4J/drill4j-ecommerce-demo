package com.example.ecommerce.api.model;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponse(
        String orderId,
        String status,
        ShippingInfoRequest shippingInfo,
        List<BasketItemResponse> items,
        BigDecimal totalAmount,
        String paymentMethod,
        String promoCode,
        BigDecimal discountAmount,
        BigDecimal deliveryFee
) {
}
