package com.jorgeandreu.products.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Product(
        UUID id,
        String sku,
        String name,
        BigDecimal price,
        Integer stock,
        String category,
        Instant createdAt,
        Instant updatedAt
) {}
