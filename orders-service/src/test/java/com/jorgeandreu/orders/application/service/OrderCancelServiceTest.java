package com.jorgeandreu.orders.application.service;

import com.jorgeandreu.orders.application.exception.OrderNotFoundException;
import com.jorgeandreu.orders.application.exception.OrderStateConflictException;
import com.jorgeandreu.orders.application.port.out.OrderEventPublisher;
import com.jorgeandreu.orders.domain.model.OrderStatus;
import com.jorgeandreu.orders.domain.port.out.OrderRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderCancelServiceTest {

    @Mock private OrderRepositoryPort repository;
    @Mock private OrderEventPublisher eventPublisher;

    @InjectMocks
    private OrderCancelService service;

    private UUID orderId;

    @BeforeEach
    void init() {
        orderId = UUID.randomUUID();
    }

    private OrderRepositoryPort.StatusVersion pending(long version) {
        return new OrderRepositoryPort.StatusVersion(OrderStatus.PENDING, version);
    }

    private OrderRepositoryPort.StatusVersion confirmed(long version) {
        return new OrderRepositoryPort.StatusVersion(OrderStatus.CONFIRMED, version);
    }

    @Nested
    @DisplayName("cancelOrder - success paths")
    class Success {

        @Test
        void cancelsWithProvidedExpectedVersion() {
            long snapshotVersion = 7L;
            long providedVersion = 12L;

            when(repository.findStatusAndVersion(orderId))
                    .thenReturn(Optional.of(pending(snapshotVersion)));
            when(repository.cancelIfPendingAndVersionMatches(eq(orderId), eq(providedVersion), any(Instant.class)))
                    .thenReturn(true);

            Instant t0 = Instant.now();
            service.cancelOrder(orderId, Optional.of(providedVersion));
            Instant t1 = Instant.now();

            verify(repository).cancelIfPendingAndVersionMatches(eq(orderId), eq(providedVersion), argThat(ts ->
                    ts != null && !ts.isBefore(t0) && !ts.isAfter(t1)
            ));

            verify(eventPublisher).publishOrderCanceled(eq(orderId), argThat(ts ->
                    ts != null && !ts.isBefore(t0) && !ts.isAfter(t1)
            ));

            InOrder inOrder = inOrder(repository, eventPublisher);
            inOrder.verify(repository).findStatusAndVersion(orderId);
            inOrder.verify(repository).cancelIfPendingAndVersionMatches(eq(orderId), eq(providedVersion), any(Instant.class));
            inOrder.verify(eventPublisher).publishOrderCanceled(eq(orderId), any(Instant.class));

            verifyNoMoreInteractions(repository, eventPublisher);
        }

        @Test
        void cancelsUsingSnapshotVersionWhenNoneProvided() {
            long snapshotVersion = 3L;
            when(repository.findStatusAndVersion(orderId))
                    .thenReturn(Optional.of(pending(snapshotVersion)));
            when(repository.cancelIfPendingAndVersionMatches(eq(orderId), eq(snapshotVersion), any(Instant.class)))
                    .thenReturn(true);

            Instant t0 = Instant.now();
            service.cancelOrder(orderId, Optional.empty());
            Instant t1 = Instant.now();

            verify(repository).cancelIfPendingAndVersionMatches(eq(orderId), eq(snapshotVersion), argThat(ts ->
                    ts != null && !ts.isBefore(t0) && !ts.isAfter(t1)
            ));
            verify(eventPublisher).publishOrderCanceled(eq(orderId), argThat(ts ->
                    ts != null && !ts.isBefore(t0) && !ts.isAfter(t1)
            ));

            InOrder inOrder = inOrder(repository, eventPublisher);
            inOrder.verify(repository).findStatusAndVersion(orderId);
            inOrder.verify(repository).cancelIfPendingAndVersionMatches(eq(orderId), eq(snapshotVersion), any(Instant.class));
            inOrder.verify(eventPublisher).publishOrderCanceled(eq(orderId), any(Instant.class));

            verifyNoMoreInteractions(repository, eventPublisher);
        }
    }

    @Nested
    @DisplayName("cancelOrder - error paths")
    class Errors {

        @Test
        void throwsWhenNotFound() {
            when(repository.findStatusAndVersion(orderId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.cancelOrder(orderId, Optional.empty()))
                    .isInstanceOf(OrderNotFoundException.class);

            verify(repository).findStatusAndVersion(orderId);
            verifyNoMoreInteractions(repository);
            verifyNoInteractions(eventPublisher);
        }

        @Test
        void throwsWhenStateNotPending() {
            when(repository.findStatusAndVersion(orderId))
                    .thenReturn(Optional.of(confirmed(9L)));

            assertThatThrownBy(() -> service.cancelOrder(orderId, Optional.empty()))
                    .isInstanceOf(OrderStateConflictException.class)
                    .hasMessageContaining("not cancelable").hasMessageContaining("CONFIRMED");

            verify(repository).findStatusAndVersion(orderId);
            verifyNoMoreInteractions(repository);
            verifyNoInteractions(eventPublisher);
        }

        @Test
        void throwsWhenVersionConflictOnUpdate() {
            long snapshotVersion = 5L;
            when(repository.findStatusAndVersion(orderId))
                    .thenReturn(Optional.of(pending(snapshotVersion)));
            when(repository.cancelIfPendingAndVersionMatches(eq(orderId), eq(snapshotVersion), any(Instant.class)))
                    .thenReturn(false);

            assertThatThrownBy(() -> service.cancelOrder(orderId, Optional.empty()))
                    .isInstanceOf(OrderStateConflictException.class)
                    .hasMessageContaining("Version/state conflict");

            verify(eventPublisher, never()).publishOrderCanceled(any(), any());

            InOrder inOrder = inOrder(repository);
            inOrder.verify(repository).findStatusAndVersion(orderId);
            inOrder.verify(repository).cancelIfPendingAndVersionMatches(eq(orderId), eq(snapshotVersion), any(Instant.class));

            verifyNoMoreInteractions(repository, eventPublisher);
        }

        @Test
        void usesProvidedVersionOverSnapshot() {
            long snapshotVersion = 1L;
            long providedVersion = 99L;

            when(repository.findStatusAndVersion(orderId))
                    .thenReturn(Optional.of(pending(snapshotVersion)));
            when(repository.cancelIfPendingAndVersionMatches(eq(orderId), eq(providedVersion), any(Instant.class)))
                    .thenReturn(false);

            assertThatThrownBy(() -> service.cancelOrder(orderId, Optional.of(providedVersion)))
                    .isInstanceOf(OrderStateConflictException.class);

            verify(repository).cancelIfPendingAndVersionMatches(eq(orderId), eq(providedVersion), any(Instant.class));
            verify(eventPublisher, never()).publishOrderCanceled(any(), any());
            verifyNoMoreInteractions(repository, eventPublisher);
        }
    }
}

