package com.example.ecommerce.service;

import com.example.ecommerce.domain.Product;
import com.example.ecommerce.web.BadRequestException;
import com.example.ecommerce.web.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WishlistService {

    private final ProductService productService;
    private final BasketService basketService;
    private final ConcurrentHashMap<String, Map<String, Product>> wishlistByUserId = new ConcurrentHashMap<>();

    public WishlistService(ProductService productService, BasketService basketService) {
        this.productService = productService;
        this.basketService = basketService;
    }

    public Product add(String userId, String productId) {
        Product product = productService.getProductById(productId);
        // OOS products are allowed (intentional Gap for OOS-rule tests).
        Map<String, Product> wishlist = getOrCreateUserWishlist(userId);
        wishlist.putIfAbsent(productId, product);
        return wishlist.get(productId);
    }

    public List<Product> list(String userId) {
        return new ArrayList<>(getOrCreateUserWishlist(userId).values());
    }

    public void remove(String userId, String productId) {
        Map<String, Product> wishlist = getOrCreateUserWishlist(userId);
        if (wishlist.remove(productId) == null) {
            throw new NotFoundException("Wishlist item not found for product: " + productId);
        }
    }

    /**
     * Happy-path move to basket. Failure paths (e.g. OOS) are an intentional Gap.
     */
    public void moveToBasket(String userId, String productId) {
        Map<String, Product> wishlist = getOrCreateUserWishlist(userId);
        if (!wishlist.containsKey(productId)) {
            throw new NotFoundException("Wishlist item not found for product: " + productId);
        }
        basketService.addItem(userId, productId, 1);
        wishlist.remove(productId);
    }

    private Map<String, Product> getOrCreateUserWishlist(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new BadRequestException("Missing user context");
        }
        return wishlistByUserId.computeIfAbsent(userId, ignored -> new LinkedHashMap<>());
    }
}
