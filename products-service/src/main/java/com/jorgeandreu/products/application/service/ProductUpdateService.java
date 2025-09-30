package com.jorgeandreu.products.application.service;


import com.jorgeandreu.products.application.exception.ProductNotFoundException;
import com.jorgeandreu.products.application.exception.ProductVersionConflictException;
import com.jorgeandreu.products.application.exception.SkuAlreadyExistsException;
import com.jorgeandreu.products.domain.port.in.UpdateProductCommand;
import com.jorgeandreu.products.domain.port.in.UpdateProductUseCase;
import com.jorgeandreu.products.domain.port.out.ProductRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductUpdateService implements UpdateProductUseCase {

    private final ProductRepositoryPort repository;

    @Override
    @Transactional
    public void updateById(UUID id, UpdateProductCommand cmd) {
        try {
            int updated = repository.updateIfVersionMatches(
                    id,
                    cmd.sku(),
                    cmd.name(),
                    cmd.price(),
                    cmd.stock(),
                    cmd.category(),
                    cmd.version(),
                    Instant.now()
            );

            if (updated == 1) return;

            boolean existsActive = repository.existsActiveById(id);
            if (existsActive) {
                throw new ProductVersionConflictException(id, cmd.version());
            } else {
                throw new ProductNotFoundException(id);
            }
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new SkuAlreadyExistsException(cmd.sku());
        }
    }
}
