package com.jorgeandreu.products.application.exception;

public class SkuAlreadyExistsException extends RuntimeException {
    private final String sku;
    public SkuAlreadyExistsException(String sku) {
        super("SKU already exists: " + sku);
        this.sku = sku;
    }
    public String getSku() { return sku; }
}