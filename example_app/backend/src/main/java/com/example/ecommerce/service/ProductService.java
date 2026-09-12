package com.example.ecommerce.service;

import com.example.ecommerce.domain.Product;
import com.example.ecommerce.web.NotFoundException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;


@Service
public class ProductService {

    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;
    private final Map<String, Product> products = new ConcurrentHashMap<>();
    private static final int MIN_PAGE_SIZE = 5;

    public ProductService(ResourceLoader resourceLoader, ObjectMapper objectMapper) {
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void init() {
        try {
            Resource resource = resourceLoader.getResource("classpath:products.json");
            try (InputStream inputStream = resource.getInputStream()) {
                // Deserialize directly into a Map structure
                Map<String, Product> loadedMap = objectMapper.readValue(
                        inputStream,
                        new TypeReference<>() {
                        }
                );
                products.clear();
                products.putAll(loadedMap);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load products map from JSON", e);
        }
    }

    public Product getProductById(String id) {
        return Optional.ofNullable(products.get(id))
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
    }

    /**
     * Returns products flagged as featured, sorted by id.
     */
    public List<Product> listFeatured() {
        return products.values().stream()
                .filter(Product::featured)
                .sorted(Comparator.comparing(Product::id))
                .toList();
    }

    /**
     * Lists paginated products sorted consistently by their ID.
     *
     * @param page Zero-based page index (0, 1, 2...)
     * @param size Number of records per page
     * @param searchText Search term to filter by (optional)
     */
    public List<Product> listProducts(int page, int size, String searchText) {
        return listProducts(page, size, searchText, null);
    }

    /**
     * Lists paginated products with optional free-text search and exact category filter.
     *
     * @param category Exact category name (case-insensitive). Blank/null = no category filter.
     *                 Unknown category yields an empty page (not an error).
     */
    public List<Product> listProducts(int page, int size, String searchText, String category) {
        // prevent negative numbers
        int pageIndex = Math.max(0, page);
        int pageSize = Math.max(MIN_PAGE_SIZE, size);

        // Normalize search query
        final String query = Optional.ofNullable(searchText)
                .map(String::trim)
                .map(String::toLowerCase)
                .orElse("");
        final String categoryFilter = Optional.ofNullable(category)
                .map(String::trim)
                .map(String::toLowerCase)
                .orElse("");
        return products.values().stream()
                .filter(product -> categoryFilter.isEmpty()
                        || (product.category() != null && product.category().equalsIgnoreCase(categoryFilter)))
                // filter: Search across name, description, or category
                .filter(product -> query.isEmpty() ||
                        Stream.of(product.name(), product.description(), product.category())
                                .filter(Objects::nonNull)
                                .anyMatch(field -> field.toLowerCase().contains(query))
                )
                // sort by ID to ensure consistent page rendering
                .sorted(Comparator.comparing(Product::id))
                // calculate starting point (e.g., page 2 with size 10 skips 20 items)
                .skip((long) pageIndex * pageSize)
                // restrict the number of returned records to the page size
                .limit(pageSize)
                .toList();
    }
}
