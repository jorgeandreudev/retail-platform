package com.jorgeandreu.orders.application.exception;

import java.util.UUID;

public class ProductNotFoundInOrderException extends  RuntimeException{
    public ProductNotFoundInOrderException(UUID id) { super("Order not found: " + id); }
}
