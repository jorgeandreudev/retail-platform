package com.jorgeandreu.orders.infrastructure.products;

import com.jorgeandreu.orders.application.exception.ProductNotFoundInOrderException;
import com.jorgeandreu.orders.domain.model.ProductSnapshot;
import com.jorgeandreu.orders.domain.port.out.ProductCatalogPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile("!stub")
@RequiredArgsConstructor
public class FeignProductCatalogAdapter implements ProductCatalogPort {

    private final ProductsFeignClient client;

    @Override
    public ProductSnapshot findSnapshotById(String productId) {
        try {
            var dto = client.getById(productId);
            return new ProductSnapshot(
                    UUID.fromString(dto.id()),
                    dto.sku(),
                    dto.name(),
                    dto.price()
            );
        } catch (feign.FeignException.NotFound e) {
            throw new ProductNotFoundInOrderException(UUID.fromString(productId));
        }
    }
}