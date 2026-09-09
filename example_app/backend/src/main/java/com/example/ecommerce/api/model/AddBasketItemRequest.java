package com.example.ecommerce.api.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AddBasketItemRequest(
        @NotBlank String productId,
        @Min(1) int quantity
) {
}
