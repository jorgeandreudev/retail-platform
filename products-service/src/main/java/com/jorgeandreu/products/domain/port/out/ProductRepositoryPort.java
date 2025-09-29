package com.jorgeandreu.products.domain.port.out;

import com.jorgeandreu.products.domain.model.PageResult;
import com.jorgeandreu.products.domain.model.Product;
import com.jorgeandreu.products.domain.model.SearchCriteria;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepositoryPort {
    boolean existsBySku(String sku);
    Product save(Product product);
    Optional<Product> findById(UUID id);
    PageResult<Product> search(SearchCriteria criteria);
}
