package com.jorgeandreu.products.application.service;

import com.jorgeandreu.products.application.exception.SkuAlreadyExistsException;
import com.jorgeandreu.products.domain.model.Product;
import com.jorgeandreu.products.domain.port.in.CreateProductCommand;
import com.jorgeandreu.products.domain.port.in.CreateProductUseCase;
import com.jorgeandreu.products.domain.port.out.ProductRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService implements CreateProductUseCase {

    private final ProductRepositoryPort repository;

    public ProductService(ProductRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Product create(CreateProductCommand cmd) {
        if (cmd == null) throw new IllegalArgumentException("command must not be null");
        if (cmd.price().signum() < 0) throw new IllegalArgumentException("price must be >= 0");
        if (cmd.stock() < 0) throw new IllegalArgumentException("stock must be >= 0");

        if (repository.existsBySku(cmd.sku())) {
            throw new SkuAlreadyExistsException(cmd.sku());
        }

        var now = java.time.Instant.now();
        var product = new Product(
                null,
                cmd.sku(),
                cmd.name(),
                cmd.price(),
                cmd.stock(),
                cmd.category(),
                now, now
        );
        return repository.save(product);
    }
}
