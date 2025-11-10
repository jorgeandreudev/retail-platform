package com.jorgeandreu.orders.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public record SearchCriteria(
        int page,
        int size,
        String sort,
        String customerEmail,
        BigDecimal minTotal,
        BigDecimal maxTotal,
        Instant from,
        Instant to,
        OrderStatus status
) {}
