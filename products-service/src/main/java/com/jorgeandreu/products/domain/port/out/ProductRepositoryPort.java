package com.jorgeandreu.products.domain.port.out;

import com.jorgeandreu.products.domain.model.PageResult;
import com.jorgeandreu.products.domain.model.Product;
import com.jorgeandreu.products.domain.model.SearchCriteria;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepositoryPort {
    boolean existsBySku(String sku);
    Product save(Product product);
    Optional<Product> findById(UUID id);
    PageResult<Product> search(SearchCriteria criteria);
    boolean softDeleteById(UUID id, Instant deletedAt);
    int updateIfVersionMatches(UUID id,
                               String sku,
                               String name,
                               BigDecimal price,
                               Integer stock,
                               String category,
                               long expectedVersion,
                               Instant updatedAt);

    /** @return true if product exists and is not soft-deleted */
    boolean existsActiveById(UUID id);
}
