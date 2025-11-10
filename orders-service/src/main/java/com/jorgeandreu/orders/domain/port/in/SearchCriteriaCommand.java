package com.jorgeandreu.orders.domain.port.in;

import com.jorgeandreu.orders.domain.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record SearchCriteriaCommand(
        OrderStatus status,
        String customerEmail,
        BigDecimal minTotal,
        BigDecimal maxTotal,
        Instant from,
        Instant to,
        int page,
        int size,
        String sort
) {}

