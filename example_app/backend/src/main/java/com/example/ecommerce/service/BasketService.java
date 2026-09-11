package com.example.ecommerce.service;

import com.example.ecommerce.domain.BasketItem;
import com.example.ecommerce.domain.Product;
import com.example.ecommerce.web.BadRequestException;
import com.example.ecommerce.web.NotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BasketService {

    private final ProductService productService;
    private final ConcurrentHashMap<String, Map<String, Integer>> basketByUserId = new ConcurrentHashMap<>();

    public BasketService(ProductService productService) {
        this.productService = productService;
    }

    public List<BasketItem> getBasketItems(String userId) {
        return getOrCreateUserBasket(userId).entrySet().stream()
                .map(entry -> new BasketItem(productService.getProductById(entry.getKey()), entry.getValue()))
                .toList();
    }

    public void addItem(String userId, String productId, int quantity) {
        Product product = productService.getProductById(productId);
        if (!product.inStock()) {
            throw new BadRequestException("Product is out of stock: " + productId);
        }
        int available = product.availableStock();
        // Incoming qty only — merged total overflow is an intentional C2 Gap.
        if (quantity > available) {
            throw new BadRequestException(
                    "Requested quantity exceeds available stock (" + available + ") for product: " + productId
            );
        }
        getOrCreateUserBasket(userId).merge(productId, quantity, (currentQty, incomingQty) -> {
            int current = currentQty != null ? currentQty : 0;
            int incoming = incomingQty != null ? incomingQty : 0;
            return current + incoming;
        });
    }

    public void updateItemQuantity(String userId, String productId, int quantity) {
        Map<String, Integer> basket = getOrCreateUserBasket(userId);
        if (!basket.containsKey(productId)) {
            throw new NotFoundException("Basket item not found for product: " + productId);
        }
        basket.put(productId, quantity);
    }

    public void removeItem(String userId, String productId) {
        Map<String, Integer> basket = getOrCreateUserBasket(userId);
        if (basket.remove(productId) == null) {
            throw new NotFoundException("Basket item not found for product: " + productId);
        }
    }

    public BigDecimal calculateSubtotal(String userId) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (BasketItem item : getBasketItems(userId)) {
            subtotal = subtotal.add(item.product().price().multiply(BigDecimal.valueOf(item.quantity())));
        }
        return subtotal;
    }

    public int totalItems(String userId) {
        return getOrCreateUserBasket(userId).values().stream().mapToInt(Integer::intValue).sum();
    }

    public void ensureNotEmpty(String userId) {
        if (getOrCreateUserBasket(userId).isEmpty()) {
            throw new BadRequestException("Basket is empty");
        }
    }

    public List<BasketItem> snapshotItems(String userId) {
        return new ArrayList<>(getBasketItems(userId));
    }

    public void clear(String userId) {
        getOrCreateUserBasket(userId).clear();
    }

    private Map<String, Integer> getOrCreateUserBasket(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new BadRequestException("Missing user context");
        }
        return basketByUserId.computeIfAbsent(userId, ignored -> new LinkedHashMap<>());
    }
}
