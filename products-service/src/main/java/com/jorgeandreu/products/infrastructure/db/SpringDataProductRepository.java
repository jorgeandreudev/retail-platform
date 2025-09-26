package com.jorgeandreu.products.infrastructure.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataProductRepository extends JpaRepository<ProductEntity, UUID>, JpaSpecificationExecutor<ProductEntity> {

    boolean existsBySku(String sku);

    @Query("select p from ProductEntity p where p.id=:id and (:includeDeleted=true or p.deletedAt is null)")
    Optional<ProductEntity> findByIdAndVisibility(@Param("id") UUID id, @Param("includeDeleted") boolean includeDeleted);

    Optional<ProductEntity> findById(@Param("id") UUID id);
}
