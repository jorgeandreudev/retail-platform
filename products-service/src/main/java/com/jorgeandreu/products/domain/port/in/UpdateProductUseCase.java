package com.jorgeandreu.products.domain.port.in;

import java.util.UUID;

public interface UpdateProductUseCase {
    void updateById(UUID id, UpdateProductCommand command);
}
