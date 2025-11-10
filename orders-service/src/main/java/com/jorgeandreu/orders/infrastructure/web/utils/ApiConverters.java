package com.jorgeandreu.orders.infrastructure.web.utils;

import com.jorgeandreu.orders.domain.model.OrderStatus;
import com.jorgeandreu.orders.infrastructure.api.model.Order;
import org.mapstruct.Mapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;

@Mapper(componentModel = "spring")
public interface ApiConverters {

    default Instant map(OffsetDateTime v) { return v == null ? null : v.toInstant(); }
    default OffsetDateTime map(Instant v) { return v == null ? null : OffsetDateTime.ofInstant(v, java.time.ZoneOffset.UTC); }

    default BigDecimal map(Double v) { return v == null ? null : BigDecimal.valueOf(v); }
    default Double map(BigDecimal v) { return v == null ? null : v.doubleValue(); }

    default <T> Optional<T> map(T v) { return Optional.ofNullable(v); }
    default <T> T map(Optional<T> v) { return v == null ? null : v.orElse(null); }

    default OrderStatus map(Order.StatusEnum v) { return v == null ? null : OrderStatus.valueOf(v.getValue()); }
    default Order.StatusEnum map(OrderStatus v) { return v == null ? null : Order.StatusEnum.fromValue(v.name()); }
}
