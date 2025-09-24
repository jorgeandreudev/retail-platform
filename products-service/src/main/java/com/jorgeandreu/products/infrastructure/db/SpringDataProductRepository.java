package com.jorgeandreu.products.infrastructure.db;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SpringDataProductRepository extends JpaRepository<ProductEntity, UUID> {
    boolean existsBySku(String sku);
}
