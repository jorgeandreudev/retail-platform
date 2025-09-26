package com.jorgeandreu.products.application.service;

import com.jorgeandreu.products.application.exception.ProductNotFoundException;
import com.jorgeandreu.products.application.mapper.SearchProductListMapper;
import com.jorgeandreu.products.domain.model.PageResult;
import com.jorgeandreu.products.domain.model.Product;
import com.jorgeandreu.products.domain.model.SearchCriteria;
import com.jorgeandreu.products.domain.port.in.GetProductUseCase;
import com.jorgeandreu.products.domain.port.in.ListProductsUseCase;
import com.jorgeandreu.products.domain.port.in.SearchCriteriaCommand;
import com.jorgeandreu.products.domain.port.out.ProductRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductQueryService implements GetProductUseCase, ListProductsUseCase {
    private final ProductRepositoryPort repositoryPort;
    private final SearchProductListMapper searchProductListMapper;

    @Override
    public Product getById(UUID id) {
        return repositoryPort.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Override
    public PageResult<Product> list(SearchCriteriaCommand criteria) {

        SearchCriteria criteriaDom = searchProductListMapper.toDomain(criteria);

        return repositoryPort.search(criteriaDom);
    }
}
