package com.example.ecommerce.service;

import com.example.ecommerce.domain.Product;
import com.example.ecommerce.web.NotFoundException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ResourceLoader resourceLoader;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ProductService productService;

    private Map<String, Product> generatedProducts;

    private static final int MIN_PAGE_SIZE = 5;

    @BeforeEach
    void setUp() throws Exception {
        generatedProducts = createMockProductsMap();
        Resource mockResource = mock(Resource.class);
        InputStream mockInputStream = new ByteArrayInputStream("{}".getBytes());

        when(resourceLoader.getResource("classpath:products.json")).thenReturn(mockResource);
        when(mockResource.getInputStream()).thenReturn(mockInputStream);

        when(objectMapper.readValue(any(InputStream.class), any(TypeReference.class)))
                .thenReturn(generatedProducts);

        productService.init();
    }

    @Test
    void getProductByIdShouldReturnProductWhenProductExists() {
        var expectedProduct = getFirstProduct();
        Product result = productService.getProductById(expectedProduct.id());
        assertThat(result).usingRecursiveComparison().isEqualTo(expectedProduct);
    }

    @Test
    void getProductByIdShouldThrowNotFoundExceptionWhenProductDoesNotExist() {
        assertThatThrownBy(() -> productService.getProductById("p-9999"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Product not found: p-9999");
    }

    @Test
    void listProductsShouldReturnSortedFirstPage() {
        var expectedProducts = getProductsSubList(12);
        // Passing null for search matches all items
        List<Product> paginatedList = productService.listProducts(0, 12, null);
        assertThat(paginatedList)
                .hasSize(12)
                .containsExactlyElementsOf(expectedProducts);
    }

    @Test
    void listProductsShouldReturnSecondPage() {
        var expectedProducts = getProductsSubList(10, 5);
        List<Product> paginatedList = productService.listProducts(1, 10, null);

        assertThat(paginatedList).containsExactlyElementsOf(expectedProducts);
    }

    @Test
    void listProductsShouldReturnEmptyListWhenPageIsOutOfBounds() {
        List<Product> paginatedList = productService.listProducts(5, 10, null);
        assertThat(paginatedList).isEmpty();
    }

    @Test
    void listProductsShouldFallbackToFirstPageWhenPageIsNegative() {
        var expectedProducts = getProductsSubList(10);
        List<Product> paginatedList = productService.listProducts(-3, 10, null);

        assertThat(paginatedList)
                .containsExactlyElementsOf(expectedProducts);
    }

    @Test
    void listProductsShouldFallbackToDefaultPageSizeWhenRequestedSizeIsLessThanTen() {
        var expectedProducts = getProductsSubList(MIN_PAGE_SIZE);
        List<Product> paginatedList = productService.listProducts(0, MIN_PAGE_SIZE - 1, null);

        assertThat(paginatedList)
                .containsExactlyElementsOf(expectedProducts);
    }


    @Test
    void listProductsShouldReturnFilteredItemsWhenSearchMatchesName() {
        // "Product 1" query should match p-0001, and p-0010 through p-0015
        var expectedProducts = getProductsSubListFiltered("Product 1", 0, 10);
        List<Product> paginatedList = productService.listProducts(0, 10, "Product 1");
        assertThat(paginatedList)
                .containsExactlyElementsOf(expectedProducts);
    }

    @Test
    void listProductsShouldBeCaseInsensitiveWhenSearching() {
        // "product" query in lowercase should match all "Product X" names
        var expectedProducts = getProductsSubListFiltered("product", 0, 10);
        List<Product> paginatedList = productService.listProducts(0, 10, "pRoDuCt");
        assertThat(paginatedList)
                .containsExactlyElementsOf(expectedProducts);
    }

    @Test
    void listProductsShouldFilterOnDescription() {
        // "Desc" matches all products as they have description="Desc"
        var expectedProductsDesc = getProductsSubListFiltered("Desc", 0, 10);
        List<Product> paginatedListDesc = productService.listProducts(0, 10, "desc");
        assertThat(paginatedListDesc).containsExactlyElementsOf(expectedProductsDesc);
    }

    @Test
    void listProductsShouldFilterOnCategory() {
        // "Category" matches all products as they have category="Category"
        var expectedProductsCat = getProductsSubListFiltered("Category", 0, 10);
        List<Product> paginatedListCat = productService.listProducts(0, 10, "CATEGORY");
        assertThat(paginatedListCat).containsExactlyElementsOf(expectedProductsCat);
    }

    @Test
    void listProductsShouldReturnEmptyListWhenSearchMatchesNothing() {
        List<Product> paginatedList = productService.listProducts(0, 10, "Non-existent query");
        assertThat(paginatedList).isEmpty();
    }

    @Test
    void listProductsShouldFilterByKnownCategoryWithPagination() throws Exception {
        // Cover: exact category filter + pagination (leave unknown / category+search as gaps)
        Map<String, Product> catalog = new java.util.HashMap<>();
        catalog.put("p-g1", product("p-g1", "Pad", "Gaming"));
        catalog.put("p-g2", product("p-g2", "Headset", "Gaming"));
        catalog.put("p-g3", product("p-g3", "Chair", "Gaming"));
        catalog.put("p-g4", product("p-g4", "Mouse", "Gaming"));
        catalog.put("p-g5", product("p-g5", "Desk", "Gaming"));
        catalog.put("p-g6", product("p-g6", "Light", "Gaming"));
        catalog.put("p-a1", product("p-a1", "Cable", "Accessories"));
        generatedProducts = catalog;
        when(objectMapper.readValue(any(InputStream.class), any(TypeReference.class)))
                .thenReturn(generatedProducts);
        productService.init();

        List<Product> page0 = productService.listProducts(0, 5, null, "Gaming");
        assertThat(page0).hasSize(5);
        assertThat(page0).allMatch(p -> p.category().equalsIgnoreCase("Gaming"));

        List<Product> page1 = productService.listProducts(1, 5, null, "gaming");
        assertThat(page1).hasSize(1);
        assertThat(page1.get(0).id()).isEqualTo("p-g6");
    }

    @Test
    void listFeaturedShouldReturnOnlyFeaturedProducts() throws Exception {
        Map<String, Product> catalog = new java.util.HashMap<>();
        catalog.put("p-f2", featuredProduct("p-f2", "Headset"));
        catalog.put("p-n1", product("p-n1", "Cable", "Accessories"));
        catalog.put("p-f1", featuredProduct("p-f1", "Pad"));
        catalog.put("p-n2", product("p-n2", "Mouse", "Accessories"));
        generatedProducts = catalog;
        when(objectMapper.readValue(any(InputStream.class), any(TypeReference.class)))
                .thenReturn(generatedProducts);
        productService.init();

        List<Product> featured = productService.listFeatured();
        assertThat(featured).extracting(Product::id).containsExactly("p-f1", "p-f2");
        assertThat(featured).allMatch(Product::featured);
    }

    private static Product product(String id, String name, String category) {
        return new Product(id, name, "Desc", BigDecimal.TEN, "url", category, true);
    }

    private static Product featuredProduct(String id, String name) {
        return new Product(id, name, "Desc", BigDecimal.TEN, "url", "Featured", true, true);
    }

    private Map<String, Product> createMockProductsMap() {
        return IntStream.rangeClosed(1, 15).mapToObj(indx -> {
            String id = String.format("p-%04d", indx);
            return new Product(
                    id,
                    "Product " + indx,
                    "Desc",
                    BigDecimal.valueOf(10.0 + indx),
                    "url",
                    "Category",
                    true
            );
        }).collect(Collectors.toMap(Product::id, Function.identity()));
    }

    private Product getFirstProduct() {
        return generatedProducts.values()
                .stream()
                .min(Comparator.comparing(Product::id))
                .orElseThrow();
    }

    private List<Product> getProductsSubList(int size) {
        return getProductsSubList(0, size);
    }

    private List<Product> getProductsSubList(int skip, int size) {
        return getProductsSubListFiltered(null, skip, size);
    }

    /**
     * Comprehensive helper that mimics the backend filtering, sorting, skipping, and limiting rules.
     */
    private List<Product> getProductsSubListFiltered(String search, int skip, int size) {
        final String query = Optional.ofNullable(search)
                .map(String::trim)
                .map(String::toLowerCase)
                .orElse("");
        return generatedProducts.values().stream()
                .filter(p -> query.isEmpty() ||
                        Stream.of(p.name(), p.description(), p.category())
                                .anyMatch(field -> field.toLowerCase().contains(query))
                )
                .sorted(Comparator.comparing(Product::id))
                .skip(skip)
                .limit(size)
                .collect(Collectors.toList());
    }
}