package com.jorgeandreu.orders.application.exception;

public class OrderVersionConflictException extends RuntimeException {
    public OrderVersionConflictException(java.util.UUID id, long version) {
        super("Version conflict updating order %s with expected version %d".formatted(id, version));
    }
}
