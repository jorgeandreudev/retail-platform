package com.jorgeandreu.products.infrastructure.db;

import com.jorgeandreu.products.domain.model.Product;


public class ProductJpaMapper {

    private ProductJpaMapper() {
    }

    public static Product toDomain(ProductEntity entity) {
        return new Product(
                entity.getId(), entity.getSku(), entity.getName(), entity.getPrice(), entity.getStock(),
                entity.getCategory(), entity.getCreatedAt(), entity.getUpdatedAt()
        );
    }

    public static ProductEntity toEntity(Product product) {
        var entity = new ProductEntity();
        entity.setId(product.id());
        entity.setSku(product.sku());
        entity.setName(product.name());
        entity.setPrice(product.price());
        entity.setStock(product.stock());
        entity.setCategory(product.category());
        return entity;
    }
}
