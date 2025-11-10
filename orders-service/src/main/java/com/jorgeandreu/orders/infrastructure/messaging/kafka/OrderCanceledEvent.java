package com.jorgeandreu.orders.infrastructure.messaging.kafka;

import java.time.Instant;
import java.util.UUID;

public record OrderCanceledEvent(
        UUID orderId,
        Instant canceledAt
) {}
