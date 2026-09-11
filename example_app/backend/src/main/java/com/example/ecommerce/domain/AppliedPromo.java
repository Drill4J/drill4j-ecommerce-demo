package com.example.ecommerce.domain;

import java.math.BigDecimal;

public record AppliedPromo(
        String code,
        BigDecimal discountAmount
) {
    public static AppliedPromo none() {
        return new AppliedPromo(null, BigDecimal.ZERO);
    }

    public boolean isApplied() {
        return code != null && discountAmount != null && discountAmount.compareTo(BigDecimal.ZERO) > 0;
    }
}
