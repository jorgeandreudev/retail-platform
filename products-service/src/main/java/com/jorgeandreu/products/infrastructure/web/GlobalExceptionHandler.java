package com.jorgeandreu.products.infrastructure.web;
import com.jorgeandreu.products.application.exception.SkuAlreadyExistsException;
import com.jorgeandreu.products.infrastructure.api.model.Problem;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SkuAlreadyExistsException.class)
    public ResponseEntity<Problem> handleDuplicateSku(SkuAlreadyExistsException ex, WebRequest req) {
        var p = new Problem()
                .title("SKU conflict")
                .status(CONFLICT.value())
                .detail("SKU already exists: " + ex.getSku())
                .type(URI.create(URI.create("https://example.com/problems/sku-conflict").toString()))
                .instance(path(req));
        return ResponseEntity.status(CONFLICT).body(p);
    }

    // Por si se cae por la constraint única de DB
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

    private URI path(WebRequest req) {
        var d = req.getDescription(false); // "uri=/api/v1/products"
        return URI.create(d.startsWith("uri=") ? d.substring(4) : d);
    }
}
