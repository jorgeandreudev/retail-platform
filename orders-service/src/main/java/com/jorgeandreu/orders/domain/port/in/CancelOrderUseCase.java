package com.jorgeandreu.orders.domain.port.in;

import java.util.Optional;
import java.util.UUID;

public interface CancelOrderUseCase {
    void cancelOrder(UUID orderId, Optional<Long> expectedVersion);
}
