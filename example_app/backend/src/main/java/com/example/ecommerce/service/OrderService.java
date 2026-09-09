package com.example.ecommerce.service;

import com.example.ecommerce.domain.BasketItem;
import com.example.ecommerce.domain.Order;
import com.example.ecommerce.domain.ShippingInfo;
import com.example.ecommerce.web.NotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OrderService {

    public static final String PAYMENT_METHOD_COD = "CASH_ON_DELIVERY";
    public static final String ORDER_STATUS_PLACED_COD = "PLACED_COD";

    private final BasketService basketService;
    private final ConcurrentHashMap<String, Map<String, Order>> ordersByUserId = new ConcurrentHashMap<>();

    public OrderService(BasketService basketService) {
        this.basketService = basketService;
    }

    public Order previewOrder(String userId, ShippingInfo shippingInfo) {
        basketService.ensureNotEmpty(userId);
        List<BasketItem> items = basketService.snapshotItems(userId);
        BigDecimal totalAmount = basketService.calculateSubtotal(userId);
        return new Order(
                "preview",
                ORDER_STATUS_PLACED_COD,
                shippingInfo,
                items,
                totalAmount,
                PAYMENT_METHOD_COD
        );
    }

    public Order placeOrder(String userId, ShippingInfo shippingInfo) {
        basketService.ensureNotEmpty(userId);
        List<BasketItem> items = basketService.snapshotItems(userId);
        BigDecimal totalAmount = basketService.calculateSubtotal(userId);
        String orderId = UUID.randomUUID().toString();
        Order order = new Order(
                orderId,
                ORDER_STATUS_PLACED_COD,
                shippingInfo,
                items,
                totalAmount,
                PAYMENT_METHOD_COD
        );
        getOrCreateUserOrders(userId).put(orderId, order);
        basketService.clear(userId);
        return order;
    }

    public Order getOrder(String userId, String orderId) {
        Order order = getOrCreateUserOrders(userId).get(orderId);
        if (order == null) {
            throw new NotFoundException("Order not found: " + orderId);
        }
        return order;
    }

    public List<Order> listOrders(String userId) {
        return getOrCreateUserOrders(userId).values().stream().toList();
    }

    private Map<String, Order> getOrCreateUserOrders(String userId) {
        return ordersByUserId.computeIfAbsent(userId, ignored -> new LinkedHashMap<>());
    }
}
