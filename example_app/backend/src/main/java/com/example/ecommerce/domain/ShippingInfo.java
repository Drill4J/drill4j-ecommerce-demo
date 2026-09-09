package com.example.ecommerce.domain;

public record ShippingInfo(
        String fullName,
        String phoneNumber,
        String addressLine,
        String city,
        String postalCode
) {
}
