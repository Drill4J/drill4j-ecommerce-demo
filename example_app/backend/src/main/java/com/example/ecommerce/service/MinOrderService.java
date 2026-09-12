package com.example.ecommerce.service;

import com.example.ecommerce.web.BadRequestException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class MinOrderService {

    public static final BigDecimal MIN_ORDER_AMOUNT = new BigDecimal("25.00");

    public void ensureMeetsMinimum(BigDecimal totalAmount) {
        if (totalAmount == null || totalAmount.compareTo(MIN_ORDER_AMOUNT) < 0) {
            throw new BadRequestException(
                    "Order total is below the minimum of " + MIN_ORDER_AMOUNT
            );
        }
    }
}
