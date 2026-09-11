package com.example.ecommerce.api.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record PlaceOrderRequest(
        @Valid @NotNull ShippingInfoRequest shippingInfo,
        String promoCode
) {
}
