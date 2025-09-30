package com.jorgeandreu.products.domain.port.in;

import java.util.UUID;

public interface DeleteProductUseCase {
    void deleteById(UUID id);
}