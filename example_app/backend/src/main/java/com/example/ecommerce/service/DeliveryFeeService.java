package com.example.ecommerce.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;

@Service
public class DeliveryFeeService {

    public static final BigDecimal AUSTIN_FEE = new BigDecimal("5.00");
    public static final BigDecimal DEFAULT_FEE = new BigDecimal("8.00");
    /** Subtotal (after promo, before fee) at/above this amount ships free. Untested gap. */
    public static final BigDecimal FREE_DELIVERY_MIN_SUBTOTAL = new BigDecimal("150.00");

    private final Map<String, BigDecimal> feesByCity = Map.of(
            "austin", AUSTIN_FEE,
            "dallas", new BigDecimal("6.50"),
            "houston", new BigDecimal("7.00")
    );

    public BigDecimal calculateFee(String city, BigDecimal merchandiseTotal) {
        if (merchandiseTotal != null
                && merchandiseTotal.compareTo(FREE_DELIVERY_MIN_SUBTOTAL) >= 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        if (city == null || city.isBlank()) {
            return DEFAULT_FEE;
        }
        String normalized = city.trim().toLowerCase(Locale.ROOT);
        return feesByCity.getOrDefault(normalized, DEFAULT_FEE);
    }
}
