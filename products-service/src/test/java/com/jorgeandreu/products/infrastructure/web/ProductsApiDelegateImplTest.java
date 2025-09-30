package com.jorgeandreu.products.infrastructure.web;

import com.jorgeandreu.products.application.exception.ProductNotFoundException;
import com.jorgeandreu.products.domain.model.PageResult;
import com.jorgeandreu.products.domain.model.Product;
import com.jorgeandreu.products.domain.port.in.CreateProductUseCase;
import com.jorgeandreu.products.domain.port.in.DeleteProductUseCase;
import com.jorgeandreu.products.domain.port.in.GetProductUseCase;
import com.jorgeandreu.products.domain.port.in.ListProductsUseCase;
import com.jorgeandreu.products.domain.port.in.SearchCriteriaCommand;
import com.jorgeandreu.products.domain.port.in.UpdateProductCommand;
import com.jorgeandreu.products.domain.port.in.UpdateProductUseCase;
import com.jorgeandreu.products.infrastructure.api.model.CreateProductRequest;
import com.jorgeandreu.products.infrastructure.api.model.ProductPage;
import com.jorgeandreu.products.infrastructure.api.model.ProductSearchCriteriaRequest;
import com.jorgeandreu.products.infrastructure.api.model.ProductSearchCriteriaRequestFilters;
import com.jorgeandreu.products.infrastructure.api.model.UpdateProductRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductsApiDelegateImplTest {

    @Mock private GetProductUseCase getProduct;
    @Mock private CreateProductUseCase createProductUC;
    @Mock private ListProductsUseCase listProductUC;
    @Mock private DeleteProductUseCase deleteProductUC;
    @Mock private UpdateProductUseCase updateProductUC;
    @Mock private ProductWebMapper webMapper;

    @InjectMocks
    private ProductsApiDelegateImpl delegate;

    private Product sampleDomain;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Simulate current HTTP request for ServletUriComponentsBuilder
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/products");
        request.setServerName("localhost");
        request.setServerPort(80);
        request.setScheme("http");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        sampleDomain = new Product(
                UUID.randomUUID(), "ACME-1", "Laptop", BigDecimal.valueOf(999.99),
                5, "laptops", Instant.now(), Instant.now(), null, 0L
        );

        sampleDomain = new Product(
                UUID.randomUUID(),
                "ACME-1",
                "Laptop",
                BigDecimal.valueOf(999.99),
                5,
                "laptops",
                Instant.now(),
                Instant.now(),
                null,
                0L
        );
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void createProduct_returnsCreatedResponse() {
        var req = new CreateProductRequest()
                .sku("ACME-1").name("Laptop")
                .price(999.99)
                .stock(5).category("laptops");

        var apiProduct = new com.jorgeandreu.products.infrastructure.api.model.Product()
                .id(sampleDomain.id())
                .sku(sampleDomain.sku())
                .name(sampleDomain.name());

        when(webMapper.toCommand(req)).thenReturn(mock());
        when(createProductUC.create(any())).thenReturn(sampleDomain);
        when(webMapper.toApi(sampleDomain)).thenReturn(apiProduct);

        var response = delegate.createProduct(req);

        URI expected = URI.create("http://localhost/api/v1/products/" + sampleDomain.id());
        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getHeaders().getLocation()).isEqualTo(expected);
        assertThat(Objects.requireNonNull(response.getBody()).getId()).isEqualTo(sampleDomain.id());

        verify(createProductUC).create(any());
        verify(webMapper).toApi(sampleDomain);
    }


    @Test
    void getProductById_returnsOkResponse() {
        UUID id = sampleDomain.id();
        var apiProduct = new com.jorgeandreu.products.infrastructure.api.model.Product().id(id);

        when(getProduct.getById(id)).thenReturn(sampleDomain);
        when(webMapper.toApi(sampleDomain)).thenReturn(apiProduct);

        ResponseEntity<com.jorgeandreu.products.infrastructure.api.model.Product> response =
                delegate.getProductById(id);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(Objects.requireNonNull(response.getBody()).getId()).isEqualTo(id);

        verify(getProduct).getById(id);
        verify(webMapper).toApi(sampleDomain);
    }

    @Test
    void searchProducts_returnsPageResponse() {
        var criteriaReq = new ProductSearchCriteriaRequest()
                .page(0).size(10)
                .filters(new ProductSearchCriteriaRequestFilters().category("laptops"));

        SearchCriteriaCommand criteriaCmd = new SearchCriteriaCommand(
                0, 10, null, "laptops", null, null, null, false
        );
        when(webMapper.productSearchCriteriaToSearchCriteria(criteriaReq)).thenReturn(criteriaCmd);

        var pageResult = new PageResult<>(List.of(sampleDomain), 0, 10, 1, 1);
        when(listProductUC.list(criteriaCmd)).thenReturn(pageResult);

        var apiPage = new ProductPage()
                .page(0).size(10).totalElements(1).totalPages(1);
        when(webMapper.toApi(pageResult)).thenReturn(apiPage);

        var response = delegate.searchProducts(criteriaReq);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(Objects.requireNonNull(response.getBody()).getTotalElements()).isEqualTo(1);

        verify(webMapper).productSearchCriteriaToSearchCriteria(criteriaReq);
        verify(listProductUC).list(criteriaCmd);
        verify(webMapper).toApi(pageResult);
        verifyNoMoreInteractions(webMapper, listProductUC, getProduct, createProductUC);
    }

    @Test
    void deleteProductById_returnsNoContent() {
        UUID id = UUID.randomUUID();

        doNothing().when(deleteProductUC).deleteById(id);

        ResponseEntity<Void> response = delegate.deleteProductById(id);

        assertThat(response.getStatusCode().value()).isEqualTo(204);
        assertThat(response.getBody()).isNull();

        verify(deleteProductUC).deleteById(id);

        verifyNoInteractions(webMapper, listProductUC, getProduct, createProductUC);
    }

    @Test
    void deleteProductById_whenUseCaseThrowsNotFound_propagatesException() {
        UUID id = UUID.randomUUID();

        doThrow(new ProductNotFoundException(id))
                .when(deleteProductUC).deleteById(id);

        ProductNotFoundException ex = assertThrows(ProductNotFoundException.class,
                () -> delegate.deleteProductById(id));

        assertThat(ex.getMessage()).isEqualTo("Product not found: " + id);

        verify(deleteProductUC).deleteById(id);
        verifyNoInteractions(webMapper, listProductUC, getProduct, createProductUC);
    }

    @Test
    void updateProductById_returnsOkWithUpdatedProduct() {
        UUID id = UUID.randomUUID();

        var req = new UpdateProductRequest()
                .sku("ACME-9").name("New Name").price(777.77).stock(9).category("laptops").version(2);

        var cmd = new UpdateProductCommand("ACME-9", "New Name",
                BigDecimal.valueOf(777.77), 9, "laptops", 2);

        when(webMapper.toCommand(req)).thenReturn(cmd);

        Product updated = new Product(
                id, "ACME-9", "New Name", BigDecimal.valueOf(777.77), 9, "laptops",
                Instant.now(), Instant.now(), null, 3L
        );

        var apiProduct = new com.jorgeandreu.products.infrastructure.api.model.Product()
                .id(id).sku("ACME-9").name("New Name").price(777.77).stock(9).category("laptops");

        when(getProduct.getById(id)).thenReturn(updated);
        when(webMapper.toApi(updated)).thenReturn(apiProduct);

        ResponseEntity<com.jorgeandreu.products.infrastructure.api.model.Product> resp =
                delegate.updateProductById(id, req);

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(Objects.requireNonNull(resp.getBody()).getId()).isEqualTo(id);

        verify(updateProductUC).updateById(id, cmd);
        verify(getProduct).getById(id);
        verify(webMapper).toApi(updated);
        verify(webMapper).toCommand(req);
        verifyNoMoreInteractions(updateProductUC, getProduct, webMapper, createProductUC, listProductUC);
    }

    @Test
    void listProducts_simpleQuery_returnsPage() {
        Integer page = 1, size = 5; String sort = "name,asc"; Boolean includeDeleted = true;

        var p = new Product(
                UUID.randomUUID(), "ACME-7", "Desk", BigDecimal.TEN, 1,
                "furniture", Instant.now(), Instant.now(), null, 0L
        );
        var pageResult = new PageResult<>(List.of(p), 1, 5, 1, 1);
        when(listProductUC.list(any(SearchCriteriaCommand.class))).thenReturn(pageResult);

        var apiPage = new com.jorgeandreu.products.infrastructure.api.model.ProductPage()
                .page(1).size(5).totalPages(1).totalElements(1);
        when(webMapper.toApi(pageResult)).thenReturn(apiPage);

        var resp = delegate.listProducts(page, size, sort, includeDeleted);

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isSameAs(apiPage);

        ArgumentCaptor<SearchCriteriaCommand> captor = ArgumentCaptor.forClass(SearchCriteriaCommand.class);
        verify(listProductUC).list(captor.capture());
        SearchCriteriaCommand used = captor.getValue();
        assertThat(used.page()).isEqualTo(1);
        assertThat(used.size()).isEqualTo(5);
        assertThat(used.sort()).isEqualTo("name,asc");
        assertThat(used.includeDeleted()).isTrue();
        assertThat(used.category()).isNull();
        assertThat(used.minPrice()).isNull();
        assertThat(used.maxPrice()).isNull();
        assertThat(used.text()).isNull();

        verify(webMapper).toApi(pageResult);
        verifyNoMoreInteractions(webMapper, listProductUC, getProduct, createProductUC);
    }

    @Test
    void listProducts_defaultsWhenNullParams_returnsPage() {
        Integer page = null, size = null; String sort = null; Boolean includeDeleted = null;

        var p = new Product(
                UUID.randomUUID(), "ACME-8", "Chair", BigDecimal.ONE, 2,
                "furniture", Instant.now(), Instant.now(), null, 0L
        );
        var pageResult = new PageResult<>(List.of(p), 0, 20, 1, 1);
        when(listProductUC.list(any(SearchCriteriaCommand.class))).thenReturn(pageResult);

        var apiPage = new com.jorgeandreu.products.infrastructure.api.model.ProductPage()
                .page(0).size(20).totalPages(1).totalElements(1);
        when(webMapper.toApi(pageResult)).thenReturn(apiPage);

        // when
        var resp = delegate.listProducts(page, size, sort, includeDeleted);

        // then
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isSameAs(apiPage);

        ArgumentCaptor<SearchCriteriaCommand> captor = ArgumentCaptor.forClass(SearchCriteriaCommand.class);
        verify(listProductUC).list(captor.capture());
        SearchCriteriaCommand used = captor.getValue();

        assertThat(used.page()).isZero();
        assertThat(used.size()).isEqualTo(20);
        assertThat(used.sort()).isNull();
        assertThat(used.includeDeleted()).isFalse();

        assertThat(used.category()).isNull();
        assertThat(used.minPrice()).isNull();
        assertThat(used.maxPrice()).isNull();
        assertThat(used.text()).isNull();

        verify(webMapper).toApi(pageResult);
        verifyNoMoreInteractions(webMapper, listProductUC, getProduct, createProductUC);
    }

}
