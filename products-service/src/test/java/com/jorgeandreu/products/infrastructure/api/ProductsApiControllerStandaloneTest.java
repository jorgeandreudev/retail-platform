package com.jorgeandreu.products.infrastructure.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jorgeandreu.products.infrastructure.api.model.CreateProductRequest;
import com.jorgeandreu.products.infrastructure.api.model.Product;
import com.jorgeandreu.products.infrastructure.api.model.ProductPage;
import com.jorgeandreu.products.infrastructure.api.model.ProductSearchCriteriaRequest;
import com.jorgeandreu.products.infrastructure.api.model.ProductSearchCriteriaRequestFilters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pure controller test using MockMvc standalone setup (no Spring ApplicationContext).
 * This completely avoids JPA/DataSource autoconfiguration issues.
 */
class ProductsApiControllerStandaloneTest {

    private static final String BASE = "/api/v1";
    private static final String PRODUCTS = BASE + "/products";

    private MockMvc mvc;
    private ObjectMapper objectMapper;

    private ProductsApiDelegate delegate; // mocked interface
    private ProductsApiController controller;

    @BeforeEach
    void setUp() {
        // Mock the generated delegate
        delegate = Mockito.mock(ProductsApiDelegate.class);

        // Instantiate the generated controller with the mocked delegate
        controller = new ProductsApiController(delegate);

        // Build standalone MockMvc (no Spring context)
        mvc = MockMvcBuilders.standaloneSetup(controller)
                // If you have a @ControllerAdvice, add it here; otherwise remove the next line.
                // .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Nested
    class CreateProduct {

        @Test
        @DisplayName("201 Created when payload is valid")
        void createdWhenValid() throws Exception {
            var req = new CreateProductRequest()
                    .sku("ACME-1")
                    .name("Laptop")
                    .price(1299.99)
                    .stock(5)
                    .category("laptops");

            var id = UUID.randomUUID();
            var apiProduct = new Product()
                    .id(id)
                    .sku("ACME-1")
                    .name("Laptop")
                    .price(1299.99)
                    .stock(5)
                    .category("laptops");

            Mockito.when(delegate.createProduct(ArgumentMatchers.any(CreateProductRequest.class)))
                    .thenAnswer(inv -> ResponseEntity
                            .created(URI.create(PRODUCTS + "/" + id))
                            .body(apiProduct));

            mvc.perform(post(PRODUCTS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", endsWith(PRODUCTS + "/" + id)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(id.toString())))
                    .andExpect(jsonPath("$.sku", is("ACME-1")))
                    .andExpect(jsonPath("$.name", is("Laptop")));

            Mockito.verify(delegate).createProduct(ArgumentMatchers.any(CreateProductRequest.class));
            Mockito.verifyNoMoreInteractions(delegate);
        }

        @Test
        @DisplayName("400 Bad Request when payload is invalid")
        void badRequestWhenInvalid() throws Exception {
            var invalid = new CreateProductRequest()
                    .price(-1.0)
                    .stock(-5);

            mvc.perform(post(PRODUCTS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class GetProductById {

        @Test
        @DisplayName("200 OK returns product by id")
        void okWhenFound() throws Exception {
            var id = UUID.randomUUID();
            var apiProduct = new Product()
                    .id(id)
                    .sku("ACME-2")
                    .name("Ultrabook")
                    .price(1499.0)
                    .stock(7)
                    .category("laptops");

            Mockito.when(delegate.getProductById(ArgumentMatchers.eq(id)))
                    .thenReturn(ResponseEntity.ok(apiProduct));

            mvc.perform(get(PRODUCTS + "/" + id))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(id.toString())))
                    .andExpect(jsonPath("$.sku", is("ACME-2")))
                    .andExpect(jsonPath("$.name", is("Ultrabook")));
        }

        @Test
        @DisplayName("404 Not Found when product does not exist")
        void notFound() throws Exception {
            var id = UUID.randomUUID();

            Mockito.when(delegate.getProductById(ArgumentMatchers.eq(id)))
                    .thenReturn(ResponseEntity.notFound().build());

            mvc.perform(get(PRODUCTS + "/" + id))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class SearchProducts {

        @Test
        @DisplayName("200 OK returns a page of products")
        void okPage() throws Exception {
            var p1 = new Product()
                    .id(UUID.randomUUID())
                    .sku("ACME-1")
                    .name("Laptop 13")
                    .price(999.0)
                    .stock(3)
                    .category("laptops");

            var p2 = new Product()
                    .id(UUID.randomUUID())
                    .sku("ACME-2")
                    .name("Laptop 15")
                    .price(1299.0)
                    .stock(8)
                    .category("laptops");

            var page = new ProductPage()
                    .page(0)
                    .size(2)
                    .totalPages(1)
                    .totalElements(2)
                    .content(List.of(p1, p2));

            Mockito.when(delegate.searchProducts(ArgumentMatchers.any(ProductSearchCriteriaRequest.class)))
                    .thenReturn(ResponseEntity.ok(page));

            ProductSearchCriteriaRequest productSearchCriteriaRequest = new ProductSearchCriteriaRequest();
            productSearchCriteriaRequest.setPage(0);
            productSearchCriteriaRequest.setSize(10);
            ProductSearchCriteriaRequestFilters productSearchCriteriaRequestFilters = new ProductSearchCriteriaRequestFilters();
            productSearchCriteriaRequestFilters.setCategory("laptops");
            productSearchCriteriaRequestFilters.minPrice(500.0);
            productSearchCriteriaRequestFilters.maxPrice(2000.0);
            productSearchCriteriaRequest.setFilters(productSearchCriteriaRequestFilters);

            mvc.perform(post(PRODUCTS + "/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(productSearchCriteriaRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.page", is(0)))
                    .andExpect(jsonPath("$.size", is(2)))
                    .andExpect(jsonPath("$.totalElements", is(2)))
                    .andExpect(jsonPath("$.content[0].sku", is("ACME-1")))
                    .andExpect(jsonPath("$.content[1].sku", is("ACME-2")));
        }
    }

    @Nested
    class DeleteProductById {

        @Test
        @DisplayName("204 No Content when deletion succeeds")
        void noContentOnSuccess() throws Exception {
            UUID id = UUID.randomUUID();

            Mockito.when(delegate.deleteProductById(ArgumentMatchers.eq(id)))
                    .thenReturn(ResponseEntity.noContent().build());

            mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .delete(PRODUCTS + "/" + id))
                    .andExpect(status().isNoContent());

            Mockito.verify(delegate).deleteProductById(ArgumentMatchers.eq(id));
            Mockito.verifyNoMoreInteractions(delegate);
        }

        @Test
        @DisplayName("404 Not Found when product doesn't exist or already deleted")
        void notFoundWhenMissingOrDeleted() throws Exception {
            UUID id = UUID.randomUUID();

            Mockito.when(delegate.deleteProductById(ArgumentMatchers.eq(id)))
                    .thenReturn(ResponseEntity.notFound().build());

            mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .delete(PRODUCTS + "/" + id))
                    .andExpect(status().isNotFound());

            Mockito.verify(delegate).deleteProductById(ArgumentMatchers.eq(id));
            Mockito.verifyNoMoreInteractions(delegate);
        }

        @Test
        @DisplayName("400 Bad Request when id is not a valid UUID")
        void badRequestOnInvalidUuid() throws Exception {
            String invalidId = "not-a-uuid";

            mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .delete(PRODUCTS + "/" + invalidId))
                    .andExpect(status().isBadRequest());

            Mockito.verifyNoInteractions(delegate);
        }
    }
}
