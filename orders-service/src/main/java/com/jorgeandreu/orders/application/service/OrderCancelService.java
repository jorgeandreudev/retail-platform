package com.jorgeandreu.orders.application.service;

import com.jorgeandreu.orders.application.exception.OrderNotFoundException;
import com.jorgeandreu.orders.application.exception.OrderStateConflictException;
import com.jorgeandreu.orders.application.port.out.OrderEventPublisher;
import com.jorgeandreu.orders.domain.model.OrderStatus;
import com.jorgeandreu.orders.domain.port.in.CancelOrderUseCase;
import com.jorgeandreu.orders.domain.port.out.OrderRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderCancelService implements CancelOrderUseCase {

    private final OrderRepositoryPort repository;
    private final OrderEventPublisher eventPublisher;

    @Override
    public void cancelOrder(UUID orderId, Optional<Long> expectedVersion) {

        var snapshot = repository.findStatusAndVersion(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (snapshot.status() != OrderStatus.PENDING) {
            throw new OrderStateConflictException("Order is not cancelable in state: " + snapshot.status());
        }

        long versionToMatch = expectedVersion.orElse(snapshot.version());

        boolean updated = repository.cancelIfPendingAndVersionMatches(orderId, versionToMatch, Instant.now());

        if (!updated) {
            throw new OrderStateConflictException("Version/state conflict while canceling order " + orderId);
        }

        eventPublisher.publishOrderCanceled(orderId, Instant.now());
    }
}
