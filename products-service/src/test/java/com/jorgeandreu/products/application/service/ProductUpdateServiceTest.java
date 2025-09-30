package com.jorgeandreu.products.application.service;

import com.jorgeandreu.products.application.exception.ProductNotFoundException;
import com.jorgeandreu.products.application.exception.ProductVersionConflictException;
import com.jorgeandreu.products.application.exception.SkuAlreadyExistsException;
import com.jorgeandreu.products.domain.port.in.UpdateProductCommand;
import com.jorgeandreu.products.domain.port.out.ProductRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ProductUpdateServiceTest {

    @Mock
    private ProductRepositoryPort repository;

    @InjectMocks
    private ProductUpdateService service;

    private static UpdateProductCommand cmd() {
        return new UpdateProductCommand("ACME-1", "Name", BigDecimal.valueOf(10.0), 5, "laptops", 2);
    }

    @Nested
    @DisplayName("updateById")
    class UpdateById {

        @Test
        @DisplayName("updates when version matches (1 row affected)")
        void success() {
            UUID id = UUID.randomUUID();
            given(repository.updateIfVersionMatches(eq(id), anyString(), anyString(), any(), anyInt(), anyString(), anyLong(), any(Instant.class)))
                    .willReturn(1);

            service.updateById(id, cmd());

            then(repository).should().updateIfVersionMatches(eq(id), anyString(), anyString(), any(), anyInt(), anyString(), anyLong(), any(Instant.class));
            then(repository).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("throws ProductVersionConflictException when 0 rows updated but product exists active")
        void versionConflict() {
            UUID id = UUID.randomUUID();
            given(repository.updateIfVersionMatches(eq(id), anyString(), anyString(), any(), anyInt(), anyString(), anyLong(), any(Instant.class)))
                    .willReturn(0);
            given(repository.existsActiveById(id)).willReturn(true);

            assertThatThrownBy(() -> service.updateById(id, cmd()))
                    .isInstanceOf(ProductVersionConflictException.class);

            then(repository).should().updateIfVersionMatches(eq(id), anyString(), anyString(), any(), anyInt(), anyString(), anyLong(), any(Instant.class));
            then(repository).should().existsActiveById(id);
            then(repository).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("throws ProductNotFoundException when 0 rows updated and product does not exist (or deleted)")
        void notFound() {
            UUID id = UUID.randomUUID();
            given(repository.updateIfVersionMatches(eq(id), anyString(), anyString(), any(), anyInt(), anyString(), anyLong(), any(Instant.class)))
                    .willReturn(0);
            given(repository.existsActiveById(id)).willReturn(false);

            assertThatThrownBy(() -> service.updateById(id, cmd()))
                    .isInstanceOf(ProductNotFoundException.class);

            then(repository).should().updateIfVersionMatches(eq(id), anyString(), anyString(), any(), anyInt(), anyString(), anyLong(), any(Instant.class));
            then(repository).should().existsActiveById(id);
            then(repository).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("translates DataIntegrityViolationException to SkuAlreadyExistsException (409)")
        void skuConflict() {
            UUID id = UUID.randomUUID();
            given(repository.updateIfVersionMatches(eq(id), anyString(), anyString(), any(), anyInt(), anyString(), anyLong(), any(Instant.class)))
                    .willThrow(new DataIntegrityViolationException("unique sku"));

            assertThatThrownBy(() -> service.updateById(id, cmd()))
                    .isInstanceOf(SkuAlreadyExistsException.class);

            then(repository).should().updateIfVersionMatches(eq(id), anyString(), anyString(), any(), anyInt(), anyString(), anyLong(), any(Instant.class));
            then(repository).shouldHaveNoMoreInteractions();
        }
    }
}

