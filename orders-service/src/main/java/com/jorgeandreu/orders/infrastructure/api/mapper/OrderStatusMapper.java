package com.jorgeandreu.orders.infrastructure.api.mapper;

import com.jorgeandreu.orders.domain.model.OrderStatus;
import com.jorgeandreu.orders.infrastructure.api.model.Order;
import org.mapstruct.Named;

public class OrderStatusMapper {

    public OrderStatusMapper() {}

    @Named("statusToApi")
    public Order.StatusEnum toApiStatus(OrderStatus status) {
        return status == null ? null : Order.StatusEnum.fromValue(status.name());
    }
}
