package com.jorgeandreu.products.infrastructure.web;

import com.jorgeandreu.products.domain.model.PageResult;
import com.jorgeandreu.products.domain.port.in.CreateProductCommand;
import com.jorgeandreu.products.domain.port.in.SearchCriteriaCommand;
import com.jorgeandreu.products.infrastructure.api.model.CreateProductRequest;
import com.jorgeandreu.products.infrastructure.api.model.Product;
import com.jorgeandreu.products.infrastructure.api.model.ProductPage;
import com.jorgeandreu.products.infrastructure.api.model.ProductSearchCriteriaRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = TimeMapper.class)
public interface ProductWebMapper {

    @Named("toDto")
    Product toDto(com.jorgeandreu.products.domain.model.Product product);

    @Named("toApi")
    Product toApi(com.jorgeandreu.products.domain.model.Product domain);

    CreateProductCommand toCommand(CreateProductRequest request);

    @Mapping(target = "category", source = "filters.category")
    @Mapping(target = "minPrice", source = "filters.minPrice")
    @Mapping(target = "maxPrice", source = "filters.maxPrice")
    @Mapping(target = "text", source = "filters.text")
    SearchCriteriaCommand productSearchCriteriaToSearchCriteria(ProductSearchCriteriaRequest request);

    @Mapping(target = "content", qualifiedByName = "toApi")
    ProductPage toApi(PageResult<com.jorgeandreu.products.domain.model.Product> pageResult);

}
