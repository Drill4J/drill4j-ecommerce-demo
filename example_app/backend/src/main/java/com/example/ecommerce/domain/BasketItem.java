package com.example.ecommerce.domain;

public record BasketItem(
        Product product,
        int quantity
) {
}
