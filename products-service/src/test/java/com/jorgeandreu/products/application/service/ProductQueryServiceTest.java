package com.jorgeandreu.products.application.service;

import com.jorgeandreu.products.application.exception.ProductNotFoundException;
import com.jorgeandreu.products.application.mapper.SearchProductListMapper;
import com.jorgeandreu.products.domain.model.PageResult;
import com.jorgeandreu.products.domain.model.Product;
import com.jorgeandreu.products.domain.model.SearchCriteria;
import com.jorgeandreu.products.domain.port.in.SearchCriteriaCommand;
import com.jorgeandreu.products.domain.port.out.ProductRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ProductQueryServiceTest {

    @Mock
    private ProductRepositoryPort repositoryPort;

    @Mock
    private SearchProductListMapper searchProductListMapper;

    @InjectMocks
    private ProductQueryService service;

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("returns product when it exists")
        void returnsProduct() {
            UUID id = UUID.randomUUID();
            Product domain = sampleProduct(id);
            given(repositoryPort.findById(id)).willReturn(Optional.of(domain));

            Product result = service.getById(id);

            assertThat(result).isEqualTo(domain);
            then(repositoryPort).should().findById(id);
            then(repositoryPort).shouldHaveNoMoreInteractions();
            then(searchProductListMapper).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("throws ProductNotFoundException when product does not exist")
        void throwsWhenMissing() {
            UUID id = UUID.randomUUID();
            given(repositoryPort.findById(id)).willReturn(Optional.empty());

            assertThatThrownBy(() -> service.getById(id))
                    .isInstanceOf(ProductNotFoundException.class)
                    .hasMessageContaining(id.toString());

            then(repositoryPort).should().findById(id);
            then(repositoryPort).shouldHaveNoMoreInteractions();
            then(searchProductListMapper).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("list")
    class ListProducts {

        @Test
        @DisplayName("maps SearchCriteriaCommand to SearchCriteria and delegates to repository")
        void mapsAndDelegates() {
            var cmd = new SearchCriteriaCommand(
                    1, 50, "createdAt,desc",
                    "laptops", 500.0, 2000.0, "pro",
                    false
            );
            var mapped = new SearchCriteria(
                    1, 50, "createdAt,desc",
                    "laptops", 500.0, 2000.0, "pro",
                    false
            );
            var p1 = sampleProduct(UUID.randomUUID());
            var p2 = sampleProduct(UUID.randomUUID());
            var expectedPage = new PageResult<>(List.of(p1, p2), 1, 50, 2, 1);

            given(searchProductListMapper.toDomain(cmd)).willReturn(mapped);
            given(repositoryPort.search(mapped)).willReturn(expectedPage);

            PageResult<Product> result = service.list(cmd);

            assertThat(result).isNotNull();
            assertThat(result.page()).isEqualTo(1);
            assertThat(result.size()).isEqualTo(50);
            assertThat(result.totalElements()).isEqualTo(2);
            assertThat(result.content()).containsExactly(p1, p2);

            then(searchProductListMapper).should().toDomain(cmd);
            then(repositoryPort).should().search(mapped);
            then(searchProductListMapper).shouldHaveNoMoreInteractions();
            then(repositoryPort).shouldHaveNoMoreInteractions();
        }
    }

    private static Product sampleProduct(UUID id) {
        return new Product(
                id,
                "ACME-123",
                "Laptop Pro",
                BigDecimal.valueOf(1299.99),
                10,
                "laptops",
                Instant.parse("2025-01-01T00:00:00Z"),
                Instant.parse("2025-01-01T00:00:00Z"),
                null,
                0L
        );
    }
}