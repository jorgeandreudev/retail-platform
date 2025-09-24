package com.jorgeandreu.products.infrastructure.db;


import com.jorgeandreu.products.domain.model.Product;
import com.jorgeandreu.products.domain.port.out.ProductRepositoryPort;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepositoryAdapter implements ProductRepositoryPort {

    private final SpringDataProductRepository repository;

    public ProductRepositoryAdapter(SpringDataProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsBySku(String sku) {
        return repository.existsBySku(sku);
    }

    @Override
    public Product save(Product product) {
        var saved = repository.save(ProductJpaMapper.toEntity(product));
        return ProductJpaMapper.toDomain(saved);
    }
}
