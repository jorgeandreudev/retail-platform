package com.jorgeandreu.products.domain.port.in;


import java.math.BigDecimal;

public record CreateProductCommand(
        String sku,
        String name,
        BigDecimal price,
        Integer stock,
        String category
) {}
