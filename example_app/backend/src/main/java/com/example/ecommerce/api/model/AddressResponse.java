package com.example.ecommerce.api.model;

public record AddressResponse(
        String id,
        String fullName,
        String phoneNumber,
        String addressLine,
        String city,
        String postalCode,
        boolean isDefault
) {
}
