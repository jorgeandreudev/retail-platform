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
        Instant updatedAt,
        Instant deletedAt,
        long version
) {
    public Product {
    if (price.signum() < 0) throw new IllegalArgumentException("price must be >= 0");
    if (stock < 0) throw new IllegalArgumentException("stock must be >= 0");
    }
    public boolean isDeleted() { return deletedAt != null; }
}

