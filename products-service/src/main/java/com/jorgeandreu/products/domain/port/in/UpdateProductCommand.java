package com.jorgeandreu.products.domain.port.in;

import java.math.BigDecimal;

public record UpdateProductCommand(
        String sku,
        String name,
        BigDecimal price,
        Integer stock,
        String category,
        long version
) {
    public UpdateProductCommand {
        if (price == null || price.signum() < 0) throw new IllegalArgumentException("price must be >= 0");
        if (stock == null || stock < 0) throw new IllegalArgumentException("stock must be >= 0");
    }
}