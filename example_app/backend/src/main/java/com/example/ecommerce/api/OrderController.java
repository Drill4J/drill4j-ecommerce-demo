package com.example.ecommerce.api;

import com.example.ecommerce.api.model.BasketItemResponse;
import com.example.ecommerce.api.model.CheckoutPreviewRequest;
import com.example.ecommerce.api.model.CheckoutPreviewResponse;
import com.example.ecommerce.api.model.OrderResponse;
import com.example.ecommerce.api.model.PlaceOrderRequest;
import com.example.ecommerce.api.model.ShippingInfoRequest;
import com.example.ecommerce.auth.AuthPrincipal;
import com.example.ecommerce.auth.AuthRequestAttributes;
import com.example.ecommerce.domain.BasketItem;
import com.example.ecommerce.domain.Order;
import com.example.ecommerce.domain.ShippingInfo;
import com.example.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/api/v1")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout/preview")
    public CheckoutPreviewResponse previewCheckout(
            @RequestAttribute(AuthRequestAttributes.PRINCIPAL) AuthPrincipal principal,
            @Valid @RequestBody CheckoutPreviewRequest request
    ) {
        Order previewOrder = orderService.previewOrder(principal.userId(), toShippingInfo(request.shippingInfo()));
        return new CheckoutPreviewResponse(
                request.shippingInfo(),
                toBasketItemResponses(previewOrder.items()),
                previewOrder.totalAmount(),
                previewOrder.paymentMethod()
        );
    }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse placeOrder(
            @RequestAttribute(AuthRequestAttributes.PRINCIPAL) AuthPrincipal principal,
            @Valid @RequestBody PlaceOrderRequest request
    ) {
        Order order = orderService.placeOrder(principal.userId(), toShippingInfo(request.shippingInfo()));
        return toOrderResponse(order);
    }

    @GetMapping("/orders")
    public List<OrderResponse> listOrders(@RequestAttribute(AuthRequestAttributes.PRINCIPAL) AuthPrincipal principal) {
        return orderService.listOrders(principal.userId()).stream()
                .map(OrderController::toOrderResponse)
                .toList();
    }

    @GetMapping("/orders/{orderId}")
    public OrderResponse getOrder(
            @RequestAttribute(AuthRequestAttributes.PRINCIPAL) AuthPrincipal principal,
            @PathVariable String orderId
    ) {
        return toOrderResponse(orderService.getOrder(principal.userId(), orderId));
    }

    private static OrderResponse toOrderResponse(Order order) {
        return new OrderResponse(
                order.id(),
                order.status(),
                toShippingInfoRequest(order.shippingInfo()),
                toBasketItemResponses(order.items()),
                order.totalAmount(),
                order.paymentMethod()
        );
    }

    private static List<BasketItemResponse> toBasketItemResponses(List<BasketItem> items) {
        return items.stream()
                .map(item -> new BasketItemResponse(
                        item.product().id(),
                        item.product().name(),
                        item.product().price(),
                        item.quantity(),
                        item.product().price().multiply(BigDecimal.valueOf(item.quantity()))
                ))
                .toList();
    }

    private static ShippingInfo toShippingInfo(ShippingInfoRequest request) {
        return new ShippingInfo(
                request.fullName(),
                request.phoneNumber(),
                request.addressLine(),
                request.city(),
                request.postalCode()
        );
    }

    private static ShippingInfoRequest toShippingInfoRequest(ShippingInfo shippingInfo) {
        return new ShippingInfoRequest(
                shippingInfo.fullName(),
                shippingInfo.phoneNumber(),
                shippingInfo.addressLine(),
                shippingInfo.city(),
                shippingInfo.postalCode()
        );
    }
}
