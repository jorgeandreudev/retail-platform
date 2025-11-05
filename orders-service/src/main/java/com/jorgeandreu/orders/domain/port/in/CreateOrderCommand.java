package com.jorgeandreu.orders.domain.port.in;

import java.util.List;

public record CreateOrderCommand(
        String customerEmail,
        List<Item> items,
        String idempotencyKey
) {
    public record Item(String productId, int quantity) {}
}
