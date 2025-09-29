package com.jorgeandreu.products.infrastructure.db.mapper;

import com.jorgeandreu.products.domain.model.PageResult;
import com.jorgeandreu.products.domain.model.Product;
import com.jorgeandreu.products.infrastructure.db.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface ProductEntityMapper {
    Product toDomain(ProductEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "version", constant = "0L")
    ProductEntity toEntity(Product domain);

    PageResult<Product> toDomain(Page<ProductEntity> entityPageResult);
}
