package com.example.ecommerce.domain;

public record SavedAddress(
        String id,
        String fullName,
        String phoneNumber,
        String addressLine,
        String city,
        String postalCode,
        boolean isDefault
) {
}
