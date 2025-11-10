package com.jorgeandreu.orders.infrastructure.web;

import com.jorgeandreu.orders.application.exception.OrderNotFoundException;
import com.jorgeandreu.orders.application.exception.OrderSearchBadRequest;
import com.jorgeandreu.orders.application.exception.OrderStateConflictException;
import com.jorgeandreu.orders.application.exception.OrderVersionConflictException;
import com.jorgeandreu.orders.application.exception.ProductNotFoundInOrderException;
import com.jorgeandreu.orders.infrastructure.api.model.Problem;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;


@RestControllerAdvice
public class GlobalExceptionHandler {



    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<Problem> handleOrderNotFound(OrderNotFoundException ex, WebRequest req) {
        var p = new Problem()
                .title("Order not found")
                .status(NOT_FOUND.value())
                .detail("Order not found: " + ex.getMessage())
                .type(URI.create(URI.create("https://example.com/problems/not-found").toString()))
                .instance(path(req));
        return ResponseEntity.status(NOT_FOUND).body(p);
    }

    @ExceptionHandler(ProductNotFoundInOrderException.class)
    public ResponseEntity<Problem> handleProductNotFoundInOrder(ProductNotFoundInOrderException ex, WebRequest req) {
        var p = new Problem()
                .title("Product in order not found")
                .status(NOT_FOUND.value())
                .detail("Product in order not found: " + ex.getMessage())
                .type(URI.create(URI.create("https://example.com/problems/not-found").toString()))
                .instance(path(req));
        return ResponseEntity.status(NOT_FOUND).body(p);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Problem> handleDataIntegrity(DataIntegrityViolationException ex, WebRequest req) {
        var p = new Problem()
                .title("Data integrity violation")
                .status(CONFLICT.value())
                .detail("Unique constraint violated (SKU).")
                .type(URI.create(URI.create("https://example.com/problems/constraint-violation").toString()))
                .instance(path(req));
        return ResponseEntity.status(CONFLICT).body(p);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Problem> handleValidation(MethodArgumentNotValidException ex, WebRequest req) {
        var details = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        var p = new Problem()
                .title("Validation failed")
                .status(BAD_REQUEST.value())
                .detail(details)
                .type(URI.create(URI.create("https://example.com/problems/validation-error").toString()))
                .instance(path(req));
        return ResponseEntity.status(BAD_REQUEST).body(p);
    }

    @ExceptionHandler(OrderSearchBadRequest.class)
    public ResponseEntity<Problem> searchOrderBadRequest(OrderSearchBadRequest ex, WebRequest req) {
        var p = new Problem()
                .title("Validation failed")
                .status(BAD_REQUEST.value())
                .type(URI.create(URI.create("https://example.com/problems/validation-error").toString()))
                .instance(path(req));
        return ResponseEntity.status(BAD_REQUEST).body(p);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Problem> handleUnexpected(Exception ex, WebRequest req) {
        var p = new Problem()
                .title("Internal error")
                .status(INTERNAL_SERVER_ERROR.value())
                .detail("Unexpected error. Please try again later.")
                .type(URI.create(URI.create("https://example.com/problems/internal-error").toString()))
                .instance(path(req));
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(p);
    }

    @ExceptionHandler(OrderVersionConflictException.class)
    public ResponseEntity<Problem> handleVersionConflict(OrderVersionConflictException ex, WebRequest req) {
        var p = new Problem()
                .title("Version conflict")
                .status(CONFLICT.value())
                .detail(ex.getMessage()) // e.g. "Version conflict updating product <id> with expected version <n>"
                .type(URI.create(URI.create("https://example.com/problems/version-conflict").toString()))
                .instance(path(req));
        return ResponseEntity.status(CONFLICT).body(p);
    }

    @ExceptionHandler(OrderStateConflictException.class)
    public ResponseEntity<Problem> handleOrderStateConflict(OrderStateConflictException ex, WebRequest req) {
        var p = new Problem()
                .title("Order State conflict")
                .status(CONFLICT.value())
                .detail(ex.getMessage())
                .type(URI.create(URI.create("https://example.com/problems/validation-error").toString()))
                .instance(path(req));
        return ResponseEntity.status(CONFLICT).body(p);
    }

    private URI path(WebRequest req) {
        var d = req.getDescription(false);
        return URI.create(d.startsWith("uri=") ? d.substring(4) : d);
    }
}
