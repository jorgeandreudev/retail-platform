package com.jorgeandreu.products.application.service;

import com.jorgeandreu.products.application.exception.SkuAlreadyExistsException;
import com.jorgeandreu.products.application.mapper.CreateProductMapper;
import com.jorgeandreu.products.domain.model.Product;
import com.jorgeandreu.products.domain.port.in.CreateProductCommand;
import com.jorgeandreu.products.domain.port.out.ProductRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ProductCreateServiceTest {

    @Mock
    private ProductRepositoryPort repository;

    @Mock
    private CreateProductMapper mapper;

    @InjectMocks
    private ProductCreateService service;

    private CreateProductCommand validCmd;

    @BeforeEach
    void setUp() {
        validCmd = new CreateProductCommand(
                "ACME-123",
                "Laptop Pro 15",
                BigDecimal.valueOf(1499.99),
                5,
                "laptops"
        );
    }

    @Nested
    @DisplayName("create() validations")
    class CreateValidations {

        @Test
        @DisplayName("throws IllegalArgumentException when command is null")
        void nullCommand() {
            assertThatThrownBy(() -> service.create(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("command must not be null");

            then(repository).shouldHaveNoInteractions();
            then(mapper).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("throws IllegalArgumentException when price is negative")
        void negativePrice() {
            CreateProductCommand cmd = new CreateProductCommand(
                    "ACME-001", "Test", BigDecimal.valueOf(-1), 10, "gadgets"
            );

            assertThatThrownBy(() -> service.create(cmd))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("price must be >= 0");

            then(repository).shouldHaveNoInteractions();
            then(mapper).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("throws IllegalArgumentException when stock is negative")
        void negativeStock() {
            CreateProductCommand cmd = new CreateProductCommand(
                    "ACME-002", "Test", BigDecimal.ONE, -5, "gadgets"
            );

            assertThatThrownBy(() -> service.create(cmd))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("stock must be >= 0");

            then(repository).shouldHaveNoInteractions();
            then(mapper).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("create() business logic")
    class CreateBusinessLogic {

        @Test
        @DisplayName("throws SkuAlreadyExistsException when repository says SKU exists")
        void duplicateSku() {
            given(repository.existsBySku(validCmd.sku())).willReturn(true);

            assertThatThrownBy(() -> service.create(validCmd))
                    .isInstanceOf(SkuAlreadyExistsException.class)
                    .hasMessageContaining(validCmd.sku());

            then(repository).should().existsBySku(validCmd.sku());
            then(repository).shouldHaveNoMoreInteractions();
            then(mapper).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("saves new product when validation passes")
        void savesNewProduct() {
            Product mapped = sampleProduct();
            Product saved = sampleProduct();

            given(repository.existsBySku(validCmd.sku())).willReturn(false);
            given(mapper.toDomain(validCmd, 0L)).willReturn(mapped);
            given(repository.save(mapped)).willReturn(saved);

            Product result = service.create(validCmd);

            assertThat(result).isEqualTo(saved);
            then(repository).should().existsBySku(validCmd.sku());
            then(mapper).should().toDomain(validCmd, 0L);
            then(repository).should().save(mapped);
            then(repository).shouldHaveNoMoreInteractions();
            then(mapper).shouldHaveNoMoreInteractions();
        }
    }

    // ---- test fixture ----
    private static Product sampleProduct() {
        return new Product(
                UUID.randomUUID(),
                "ACME-123",
                "Laptop Pro 15",
                BigDecimal.valueOf(1499.99),
                5,
                "laptops",
                Instant.now(),
                Instant.now(),
                null,
                1L
        );
    }
}
