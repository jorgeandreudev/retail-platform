package com.jorgeandreu.orders.domain.port.in;

import com.jorgeandreu.orders.domain.model.Order;
import com.jorgeandreu.orders.domain.model.PageResult;

import java.util.UUID;

public interface GetOrderUseCase {
    Order getById(UUID id);
    PageResult<Order> list(SearchCriteriaCommand criteria);
}
