package com.jorgeandreu.products.domain.port.in;

import com.jorgeandreu.products.domain.model.PageResult;
import com.jorgeandreu.products.domain.model.Product;

public interface ListProductsUseCase {
    PageResult<Product> list(SearchCriteriaCommand criteria);
}
