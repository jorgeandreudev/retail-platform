package com.jorgeandreu.orders.domain.model;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record Order(
        UUID id,
        OrderStatus status,
        String customerEmail,
        List<OrderItem> items,
        BigDecimal total,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
    public static Order newPending(String email, List<OrderItem> items, Instant now) {
        var id = java.util.UUID.randomUUID();
        var total = items.stream()
                .map(OrderItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Order(id, OrderStatus.PENDING, email, items, total, now, now, null);
    }
}