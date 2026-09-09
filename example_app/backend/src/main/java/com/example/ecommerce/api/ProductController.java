package com.example.ecommerce.api;

import com.example.ecommerce.api.model.ProductResponse;
import com.example.ecommerce.domain.Product;
import com.example.ecommerce.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{productId}")
    public ProductResponse getProduct(@PathVariable String productId) {
        return toResponse(productService.getProductById(productId));
    }

    @GetMapping
    public List<ProductResponse> listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ) {
        return productService.listProducts(page, size, search)
                .stream()
                .map(ProductController::toResponse)
                .toList();
    }

    private static ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.id(),
                product.name(),
                product.description(),
                product.price(),
                product.imageUrl(),
                product.category(),
                product.inStock()
        );
    }
}
