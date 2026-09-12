package com.example.ecommerce.api.model;

import jakarta.validation.constraints.NotBlank;

public record AddWishlistItemRequest(
        @NotBlank String productId
) {
}
