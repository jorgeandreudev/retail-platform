package com.jorgeandreu.orders.domain.port.out;

import com.jorgeandreu.orders.domain.model.ProductSnapshot;

public interface ProductCatalogPort {
    ProductSnapshot findSnapshotById(String productId);
}
