package com.jorgeandreu.orders.domain.port.in;

import com.jorgeandreu.orders.domain.model.Order;

public interface CreateOrderUseCase {
    Order create(CreateOrderCommand cmd);
}
