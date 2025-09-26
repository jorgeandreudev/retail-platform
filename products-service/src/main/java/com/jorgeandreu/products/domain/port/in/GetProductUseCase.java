package com.jorgeandreu.products.domain.port.in;


import com.jorgeandreu.products.domain.model.Product;

import java.util.UUID;

public interface GetProductUseCase {
    Product getById(UUID id);
}
