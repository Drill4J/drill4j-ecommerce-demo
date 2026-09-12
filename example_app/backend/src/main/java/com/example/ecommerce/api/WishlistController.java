package com.example.ecommerce.api;

import com.example.ecommerce.api.model.AddWishlistItemRequest;
import com.example.ecommerce.api.model.WishlistItemResponse;
import com.example.ecommerce.auth.AuthPrincipal;
import com.example.ecommerce.auth.AuthRequestAttributes;
import com.example.ecommerce.domain.Product;
import com.example.ecommerce.service.WishlistService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public List<WishlistItemResponse> listWishlist(
            @RequestAttribute(AuthRequestAttributes.PRINCIPAL) AuthPrincipal principal
    ) {
        return wishlistService.list(principal.userId()).stream()
                .map(WishlistController::toResponse)
                .toList();
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public WishlistItemResponse addWishlistItem(
            @RequestAttribute(AuthRequestAttributes.PRINCIPAL) AuthPrincipal principal,
            @Valid @RequestBody AddWishlistItemRequest request
    ) {
        Product product = wishlistService.add(principal.userId(), request.productId());
        return toResponse(product);
    }

    @DeleteMapping("/items/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeWishlistItem(
            @RequestAttribute(AuthRequestAttributes.PRINCIPAL) AuthPrincipal principal,
            @PathVariable String productId
    ) {
        wishlistService.remove(principal.userId(), productId);
    }

    private static WishlistItemResponse toResponse(Product product) {
        return new WishlistItemResponse(
                product.id(),
                product.name(),
                product.price(),
                product.imageUrl(),
                product.category(),
                product.inStock()
        );
    }
}
