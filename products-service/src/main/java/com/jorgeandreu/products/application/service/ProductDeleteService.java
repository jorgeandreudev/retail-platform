package com.jorgeandreu.products.application.service;

import com.jorgeandreu.products.application.exception.ProductNotFoundException;
import com.jorgeandreu.products.domain.port.in.DeleteProductUseCase;
import com.jorgeandreu.products.domain.port.out.ProductRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductDeleteService implements DeleteProductUseCase {

    private final ProductRepositoryPort repository;

    @Override
    @Transactional
    public void deleteById(UUID id) {
        boolean deleted = repository.softDeleteById(id, Instant.now());
        if (!deleted) {
            throw new ProductNotFoundException(id);
        }
    }
}