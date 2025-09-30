package com.jorgeandreu.products.infrastructure.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataProductRepository extends JpaRepository<ProductEntity, UUID>, JpaSpecificationExecutor<ProductEntity> {

    boolean existsBySku(String sku);

    Optional<ProductEntity> findById(@Param("id") UUID id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           UPDATE ProductEntity p
           SET p.deletedAt = :deletedAt,
               p.version = p.version + 1
           WHERE p.id = :id AND p.deletedAt IS NULL
           """)
    int softDeleteIfNotDeleted(@Param("id") UUID id, @Param("deletedAt") Instant deletedAt);
}
