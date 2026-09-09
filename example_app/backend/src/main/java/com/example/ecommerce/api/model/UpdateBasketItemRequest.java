package com.example.ecommerce.api.model;

import jakarta.validation.constraints.Min;

public record UpdateBasketItemRequest(
        @Min(1) int quantity
) {
}
