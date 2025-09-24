package com.jorgeandreu.products.infrastructure.web;

import com.jorgeandreu.products.domain.port.in.CreateProductCommand;
import com.jorgeandreu.products.domain.port.in.CreateProductUseCase;
import com.jorgeandreu.products.infrastructure.api.ProductsApiDelegate;
import com.jorgeandreu.products.infrastructure.api.model.CreateProductRequest;
import com.jorgeandreu.products.infrastructure.api.model.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Component
public class ProductsApiDelegateImpl implements ProductsApiDelegate {

    private final CreateProductUseCase create;

    public ProductsApiDelegateImpl(CreateProductUseCase create) {
        this.create = create;
    }

    private static OffsetDateTime toOdt(Instant i) {
        return (i == null) ? null : OffsetDateTime.ofInstant(i, ZoneOffset.UTC);
    }

    @Override
    public ResponseEntity<Product> createProduct(CreateProductRequest req) {
        var cmd = new CreateProductCommand(
                req.getSku(),
                req.getName(),
                BigDecimal.valueOf(req.getPrice()),
                req.getStock(),
                req.getCategory()
        );

        var created = create.create(cmd);

        var body = new Product()
                .id(UUID.fromString(created.id().toString()))
                .sku(created.sku())
                .name(created.name())
                .price(created.price().doubleValue())
                .stock(created.stock())
                .category(created.category())
                .createdAt(toOdt(created.createdAt()))
                .updatedAt(toOdt(created.updatedAt()));

        var location = URI.create("/api/v1/products/" + body.getId());
        return ResponseEntity.created(location).body(body);
    }
}
