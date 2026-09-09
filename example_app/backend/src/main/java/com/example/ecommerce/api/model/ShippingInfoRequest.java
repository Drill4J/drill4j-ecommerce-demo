package com.example.ecommerce.api.model;

import jakarta.validation.constraints.NotBlank;

public record ShippingInfoRequest(
        @NotBlank String fullName,
        @NotBlank String phoneNumber,
        @NotBlank String addressLine,
        @NotBlank String city,
        @NotBlank String postalCode
) {
}
