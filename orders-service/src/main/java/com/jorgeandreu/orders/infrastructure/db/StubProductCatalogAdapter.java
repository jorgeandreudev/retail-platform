package com.jorgeandreu.orders.infrastructure.db;

import com.jorgeandreu.orders.domain.model.ProductSnapshot;
import com.jorgeandreu.orders.domain.port.out.ProductCatalogPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@Profile("stub")
public class StubProductCatalogAdapter implements ProductCatalogPort {

    @Override
    public ProductSnapshot findSnapshotById(String productId) {
        return new ProductSnapshot(
                UUID.fromString(productId),
                "SKU-" + productId.substring(0, 6),
                "Product " + productId.substring(0, 6),
                new BigDecimal("49.99")
        );
    }
}
