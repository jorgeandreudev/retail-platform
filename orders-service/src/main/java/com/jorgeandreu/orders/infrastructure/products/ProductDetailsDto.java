package com.jorgeandreu.orders.infrastructure.products;

import java.math.BigDecimal;

public record ProductDetailsDto(
        String id,
        String sku,
        String name,
        BigDecimal price,
        String category
) {}
