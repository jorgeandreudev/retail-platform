package com.jorgeandreu.products.application.service;

import com.jorgeandreu.products.application.exception.SkuAlreadyExistsException;
import com.jorgeandreu.products.application.mapper.CreateProductMapper;
import com.jorgeandreu.products.domain.model.Product;
import com.jorgeandreu.products.domain.port.in.CreateProductCommand;
import com.jorgeandreu.products.domain.port.in.CreateProductUseCase;
import com.jorgeandreu.products.domain.port.out.ProductRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductCreateService implements CreateProductUseCase {

    @Value("${products.initial-version:0}")
    private long initialVersion;

    private final ProductRepositoryPort repository;

    private final CreateProductMapper createProductMapper;

    @Override
    @Transactional
    public Product create(CreateProductCommand cmd) {
        if (cmd == null) throw new IllegalArgumentException("command must not be null");
        if (cmd.price().signum() < 0) throw new IllegalArgumentException("price must be >= 0");
        if (cmd.stock() < 0) throw new IllegalArgumentException("stock must be >= 0");

        if (repository.existsBySku(cmd.sku())) {
            throw new SkuAlreadyExistsException(cmd.sku());
        }

        Product product = createProductMapper.toDomain(cmd, initialVersion);

        return repository.save(product);
    }
}
