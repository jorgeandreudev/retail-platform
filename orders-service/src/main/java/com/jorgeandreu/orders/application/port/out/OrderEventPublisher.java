package com.jorgeandreu.orders.application.port.out;

import java.time.Instant;
import java.util.UUID;

public interface OrderEventPublisher {
    void publishOrderCanceled(UUID orderId, Instant canceledAt);
}
