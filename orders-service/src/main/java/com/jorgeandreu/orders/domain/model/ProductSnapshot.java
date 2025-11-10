package com.jorgeandreu.orders.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductSnapshot(UUID productId, String sku, String name, BigDecimal price) {}

