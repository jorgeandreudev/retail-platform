package com.jorgeandreu.orders.application.exception;

public class OrderStateConflictException extends RuntimeException {
    public OrderStateConflictException(String msg) { super(msg); }
}
