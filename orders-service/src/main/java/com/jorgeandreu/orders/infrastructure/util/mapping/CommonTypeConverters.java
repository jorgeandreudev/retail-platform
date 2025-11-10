package com.jorgeandreu.orders.infrastructure.util.mapping;

import com.jorgeandreu.orders.domain.model.OrderStatus;
import com.jorgeandreu.orders.infrastructure.api.model.Order;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class CommonTypeConverters {

    @Named("stringToUuid")
    public UUID stringToUuid(String id) {
        if (id == null || id.isBlank()) return null;
        return UUID.fromString(id);
    }

    @Named("bigDecimalToDouble")
    public Double bigDecimalToDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }

    @Named("statusToApi")
    public Order.StatusEnum statusToApi(OrderStatus status) {
        return status == null ? null : Order.StatusEnum.fromValue(status.name());
    }
}
