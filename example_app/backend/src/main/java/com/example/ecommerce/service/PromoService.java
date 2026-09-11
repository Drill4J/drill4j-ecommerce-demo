package com.example.ecommerce.service;

import com.example.ecommerce.domain.AppliedPromo;
import com.example.ecommerce.web.BadRequestException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
public class PromoService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private record PromoRule(
            String code,
            Type type,
            BigDecimal value,
            boolean expired,
            BigDecimal minSubtotal
    ) {
        enum Type {
            PERCENT,
            FIXED
        }
    }

    private final Map<String, PromoRule> rules = Map.of(
            "SAVE10", new PromoRule("SAVE10", PromoRule.Type.PERCENT, new BigDecimal("10"), false, null),
            "EXPIRED10", new PromoRule("EXPIRED10", PromoRule.Type.PERCENT, new BigDecimal("10"), true, null),
            "SAVE20MIN", new PromoRule("SAVE20MIN", PromoRule.Type.PERCENT, new BigDecimal("20"), false, new BigDecimal("100.00")),
            "FLAT5", new PromoRule("FLAT5", PromoRule.Type.FIXED, new BigDecimal("5.00"), false, null)
    );

    public AppliedPromo apply(String promoCode, BigDecimal subtotal) {
        if (promoCode == null || promoCode.isBlank()) {
            return AppliedPromo.none();
        }

        String normalized = promoCode.trim().toUpperCase();
        PromoRule rule = rules.get(normalized);
        if (rule == null) {
            throw new BadRequestException("Invalid promo code: " + normalized);
        }
        if (rule.expired()) {
            throw new BadRequestException("Promo code has expired: " + normalized);
        }
        if (rule.minSubtotal() != null && subtotal.compareTo(rule.minSubtotal()) < 0) {
            throw new BadRequestException(
                    "Promo code requires minimum subtotal of " + rule.minSubtotal() + ": " + normalized
            );
        }

        BigDecimal discount = switch (rule.type()) {
            case PERCENT -> subtotal
                    .multiply(rule.value())
                    .divide(HUNDRED, 2, RoundingMode.HALF_UP);
            case FIXED -> rule.value().min(subtotal).setScale(2, RoundingMode.HALF_UP);
        };
        return new AppliedPromo(rule.code(), discount);
    }
}
