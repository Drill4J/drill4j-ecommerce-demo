package com.example.ecommerce.api;

import com.example.ecommerce.api.model.AddBasketItemRequest;
import com.example.ecommerce.api.model.BasketItemResponse;
import com.example.ecommerce.api.model.BasketResponse;
import com.example.ecommerce.api.model.UpdateBasketItemRequest;
import com.example.ecommerce.auth.AuthPrincipal;
import com.example.ecommerce.auth.AuthRequestAttributes;
import com.example.ecommerce.domain.BasketItem;
import com.example.ecommerce.service.BasketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/basket")
public class BasketController {

    private final BasketService basketService;

    public BasketController(BasketService basketService) {
        this.basketService = basketService;
    }

    @GetMapping
    public BasketResponse getBasket(@RequestAttribute(AuthRequestAttributes.PRINCIPAL) AuthPrincipal principal) {
        return toBasketResponse(principal.userId());
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public BasketResponse addBasketItem(
            @RequestAttribute(AuthRequestAttributes.PRINCIPAL) AuthPrincipal principal,
            @Valid @RequestBody AddBasketItemRequest request
    ) {
        basketService.addItem(principal.userId(), request.productId(), request.quantity());
        return toBasketResponse(principal.userId());
    }

    @PatchMapping("/items/{productId}")
    public BasketResponse updateBasketItem(
            @RequestAttribute(AuthRequestAttributes.PRINCIPAL) AuthPrincipal principal,
            @PathVariable String productId,
            @Valid @RequestBody UpdateBasketItemRequest request
    ) {
        basketService.updateItemQuantity(principal.userId(), productId, request.quantity());
        return toBasketResponse(principal.userId());
    }

    @DeleteMapping("/items/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeBasketItem(
            @RequestAttribute(AuthRequestAttributes.PRINCIPAL) AuthPrincipal principal,
            @PathVariable String productId
    ) {
        basketService.removeItem(principal.userId(), productId);
    }

    private BasketResponse toBasketResponse(String userId) {
        List<BasketItemResponse> items = basketService.getBasketItems(userId).stream()
                .map(BasketController::toBasketItemResponse)
                .toList();
        return new BasketResponse(
                items,
                basketService.totalItems(userId),
                basketService.calculateSubtotal(userId)
        );
    }

    private static BasketItemResponse toBasketItemResponse(BasketItem item) {
        BigDecimal lineTotal = item.product().price().multiply(BigDecimal.valueOf(item.quantity()));
        return new BasketItemResponse(
                item.product().id(),
                item.product().name(),
                item.product().price(),
                item.quantity(),
                lineTotal
        );
    }
}
