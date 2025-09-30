package com.jorgeandreu.products.application.exception;

public class ProductVersionConflictException extends RuntimeException {
    public ProductVersionConflictException(java.util.UUID id, long version) {
        super("Version conflict updating product %s with expected version %d".formatted(id, version));
    }
}
