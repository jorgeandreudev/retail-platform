package com.jorgeandreu.orders.domain.model;

import java.math.BigDecimal;

public record OrderItem(
        String productId,
        String sku,
        String name,
        BigDecimal unitPrice,
        int quantity
) {
    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}