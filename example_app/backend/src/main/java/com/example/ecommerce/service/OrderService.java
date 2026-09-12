package com.example.ecommerce.service;

import com.example.ecommerce.domain.AppliedPromo;
import com.example.ecommerce.domain.BasketItem;
import com.example.ecommerce.domain.Order;
import com.example.ecommerce.domain.Product;
import com.example.ecommerce.domain.ReorderResult;
import com.example.ecommerce.domain.ShippingInfo;
import com.example.ecommerce.web.BadRequestException;
import com.example.ecommerce.web.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OrderService {

    public static final String PAYMENT_METHOD_COD = "CASH_ON_DELIVERY";
    public static final String ORDER_STATUS_PLACED_COD = "PLACED_COD";
    public static final String ORDER_STATUS_CANCELLED = "CANCELLED";
    /** Cancellation allowed within this window after place (Gap: outside-window not covered by tests). */
    public static final Duration CANCELLATION_WINDOW = Duration.ofHours(24);

    private final BasketService basketService;
    private final PromoService promoService;
    private final DeliveryFeeService deliveryFeeService;
    private final MinOrderService minOrderService;
    private final ProductService productService;
    private final Clock clock;
    private final ConcurrentHashMap<String, Map<String, Order>> ordersByUserId = new ConcurrentHashMap<>();

    @Autowired
    public OrderService(
            BasketService basketService,
            PromoService promoService,
            DeliveryFeeService deliveryFeeService,
            MinOrderService minOrderService,
            ProductService productService
    ) {
        this(basketService, promoService, deliveryFeeService, minOrderService, productService, Clock.systemUTC());
    }

    OrderService(
            BasketService basketService,
            PromoService promoService,
            DeliveryFeeService deliveryFeeService,
            MinOrderService minOrderService,
            ProductService productService,
            Clock clock
    ) {
        this.basketService = basketService;
        this.promoService = promoService;
        this.deliveryFeeService = deliveryFeeService;
        this.minOrderService = minOrderService;
        this.productService = productService;
        this.clock = clock;
    }

    public Order previewOrder(String userId, ShippingInfo shippingInfo) {
        return previewOrder(userId, shippingInfo, null);
    }

    public Order previewOrder(String userId, ShippingInfo shippingInfo, String promoCode) {
        basketService.ensureNotEmpty(userId);
        List<BasketItem> items = basketService.snapshotItems(userId);
        BigDecimal subtotal = basketService.calculateSubtotal(userId);
        AppliedPromo appliedPromo = promoService.apply(promoCode, subtotal);
        BigDecimal merchandiseTotal = subtotal.subtract(appliedPromo.discountAmount());
        BigDecimal deliveryFee = deliveryFeeService.calculateFee(shippingInfo.city(), merchandiseTotal);
        BigDecimal totalAmount = merchandiseTotal.add(deliveryFee);
        minOrderService.ensureMeetsMinimum(totalAmount);
        return new Order(
                "preview",
                ORDER_STATUS_PLACED_COD,
                shippingInfo,
                items,
                totalAmount,
                PAYMENT_METHOD_COD,
                appliedPromo.code(),
                appliedPromo.discountAmount(),
                deliveryFee,
                null
        );
    }

    public Order placeOrder(String userId, ShippingInfo shippingInfo) {
        return placeOrder(userId, shippingInfo, null);
    }

    public Order placeOrder(String userId, ShippingInfo shippingInfo, String promoCode) {
        basketService.ensureNotEmpty(userId);
        List<BasketItem> items = basketService.snapshotItems(userId);
        for (BasketItem item : items) {
            if (!item.product().inStock()) {
                throw new BadRequestException("Product is out of stock: " + item.product().id());
            }
        }
        BigDecimal subtotal = basketService.calculateSubtotal(userId);
        AppliedPromo appliedPromo = promoService.apply(promoCode, subtotal);
        BigDecimal merchandiseTotal = subtotal.subtract(appliedPromo.discountAmount());
        BigDecimal deliveryFee = deliveryFeeService.calculateFee(shippingInfo.city(), merchandiseTotal);
        BigDecimal totalAmount = merchandiseTotal.add(deliveryFee);
        minOrderService.ensureMeetsMinimum(totalAmount);
        String orderId = UUID.randomUUID().toString();
        Order order = new Order(
                orderId,
                ORDER_STATUS_PLACED_COD,
                shippingInfo,
                items,
                totalAmount,
                PAYMENT_METHOD_COD,
                appliedPromo.code(),
                appliedPromo.discountAmount(),
                deliveryFee,
                Instant.now(clock)
        );
        getOrCreateUserOrders(userId).put(orderId, order);
        basketService.clear(userId);
        return order;
    }

    public Order cancelOrder(String userId, String orderId) {
        Order order = getOrder(userId, orderId);
        if (!ORDER_STATUS_PLACED_COD.equals(order.status())) {
            throw new BadRequestException("Order cannot be cancelled: " + orderId);
        }
        if (order.placedAt() == null
                || Duration.between(order.placedAt(), Instant.now(clock)).compareTo(CANCELLATION_WINDOW) > 0) {
            throw new BadRequestException("Cancellation window has expired for order: " + orderId);
        }
        Order cancelled = new Order(
                order.id(),
                ORDER_STATUS_CANCELLED,
                order.shippingInfo(),
                order.items(),
                order.totalAmount(),
                order.paymentMethod(),
                order.promoCode(),
                order.discountAmount(),
                order.deliveryFee(),
                order.placedAt()
        );
        getOrCreateUserOrders(userId).put(orderId, cancelled);
        return cancelled;
    }

    public ReorderResult reorder(String userId, String orderId) {
        Order order = getOrder(userId, orderId);
        List<String> skippedProductIds = new ArrayList<>();
        List<BasketItem> inStockLines = new ArrayList<>();

        for (BasketItem line : order.items()) {
            Product product = productService.getProductById(line.product().id());
            if (!product.inStock()) {
                skippedProductIds.add(product.id());
            } else {
                inStockLines.add(new BasketItem(product, line.quantity()));
            }
        }

        if (inStockLines.isEmpty()) {
            throw new BadRequestException("No items available to reorder from order: " + orderId);
        }

        for (BasketItem line : inStockLines) {
            basketService.addItem(userId, line.product().id(), line.quantity());
        }

        return new ReorderResult(
                basketService.snapshotItems(userId),
                basketService.totalItems(userId),
                basketService.calculateSubtotal(userId),
                List.copyOf(skippedProductIds)
        );
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
