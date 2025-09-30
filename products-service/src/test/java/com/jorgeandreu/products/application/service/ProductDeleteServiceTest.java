package com.jorgeandreu.products.application.service;

import com.jorgeandreu.products.application.exception.ProductNotFoundException;
import com.jorgeandreu.products.domain.port.out.ProductRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ProductDeleteServiceTest {

    @Mock
    private ProductRepositoryPort repository;

    @InjectMocks
    private ProductDeleteService service;

    @Test
    @DisplayName("deleteById: soft-deletes product when repository returns true")
    void deleteById_success() {
        UUID id = UUID.randomUUID();
        given(repository.softDeleteById(eq(id), any(Instant.class))).willReturn(true);

        service.deleteById(id);

        ArgumentCaptor<Instant> captor = ArgumentCaptor.forClass(Instant.class);
        then(repository).should().softDeleteById(eq(id), captor.capture());
        then(repository).shouldHaveNoMoreInteractions();

        Instant usedInstant = captor.getValue();
        assertThat(usedInstant).isNotNull();
        assertThat(usedInstant).isBeforeOrEqualTo(Instant.now().plusSeconds(1));
    }

    @Test
    @DisplayName("deleteById: throws ProductNotFoundException when repository returns false")
    void deleteById_notFound() {
        UUID id = UUID.randomUUID();
        given(repository.softDeleteById(eq(id), any(Instant.class))).willReturn(false);

        assertThatThrownBy(() -> service.deleteById(id))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining(id.toString());

        then(repository).should().softDeleteById(eq(id), any(Instant.class));
        then(repository).shouldHaveNoMoreInteractions();
    }
}