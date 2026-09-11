package com.example.ecommerce.api.model;

import jakarta.validation.Valid;

public record PlaceOrderRequest(
        @Valid ShippingInfoRequest shippingInfo,
        String addressId,
        String promoCode
) {
}
