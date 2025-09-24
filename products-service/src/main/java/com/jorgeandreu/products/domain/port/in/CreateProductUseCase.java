package com.jorgeandreu.products.domain.port.in;

import com.jorgeandreu.products.domain.model.Product;

public interface CreateProductUseCase {
    Product create(CreateProductCommand command);
}
