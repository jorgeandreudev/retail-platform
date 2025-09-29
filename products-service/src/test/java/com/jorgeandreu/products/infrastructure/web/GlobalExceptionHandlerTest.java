package com.jorgeandreu.products.infrastructure.web;

import com.jorgeandreu.products.application.exception.ProductNotFoundException;
import com.jorgeandreu.products.application.exception.SkuAlreadyExistsException;
import com.jorgeandreu.products.infrastructure.api.model.Problem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        webRequest = Mockito.mock(WebRequest.class);
        given(webRequest.getDescription(false)).willReturn("uri=/api/v1/products");
    }

    @Nested
    @DisplayName("SKU conflict")
    class SkuConflict {

        @Test
        void handleDuplicateSku_returns409_withProblemPayload() {
            var ex = new SkuAlreadyExistsException("ACME-1");

            var response = handler.handleDuplicateSku(ex, webRequest);

            assertThat(response.getStatusCode().value()).isEqualTo(409);
            Problem p = response.getBody();
            assertThat(p).isNotNull();
            assertThat(p.getTitle()).isEqualTo("SKU conflict");
            assertThat(p.getStatus()).isEqualTo(409);
            assertThat(p.getDetail()).contains("ACME-1");
            assertThat(p.getType()).isEqualTo(URI.create("https://example.com/problems/sku-conflict"));
            assertThat(p.getInstance()).isEqualTo(URI.create("/api/v1/products"));
        }
    }

    @Nested
    @DisplayName("Product not found")
    class NotFound {

        @Test
        void handleProductNotFound_returns404_withProblemPayload() {
            UUID id = UUID.randomUUID();
            var ex = new ProductNotFoundException(id);

            var response = handler.handleProductNotFound(ex, webRequest);

            assertThat(response.getStatusCode().value()).isEqualTo(404);
            Problem p = response.getBody();
            assertThat(p).isNotNull();
            assertThat(p.getTitle()).isEqualTo("Product not found");
            assertThat(p.getStatus()).isEqualTo(404);
            assertThat(p.getDetail()).contains(id.toString());
            assertThat(p.getType()).isEqualTo(URI.create("https://example.com/problems/not-found"));
            assertThat(p.getInstance()).isEqualTo(URI.create("/api/v1/products"));
        }
    }

    @Nested
    @DisplayName("Data integrity")
    class DataIntegrity {

        @Test
        void handleDataIntegrity_returns409_withProblemPayload() {
            var ex = new DataIntegrityViolationException("unique violation");

            var response = handler.handleDataIntegrity(ex, webRequest);

            assertThat(response.getStatusCode().value()).isEqualTo(409);
            Problem p = response.getBody();
            assertThat(p).isNotNull();
            assertThat(p.getTitle()).isEqualTo("Data integrity violation");
            assertThat(p.getStatus()).isEqualTo(409);
            assertThat(p.getDetail()).contains("Unique constraint violated");
            assertThat(p.getType()).isEqualTo(URI.create("https://example.com/problems/constraint-violation"));
            assertThat(p.getInstance()).isEqualTo(URI.create("/api/v1/products"));
        }
    }

    @Nested
    @DisplayName("Validation (Bean Validation)")
    class Validation {

        @Test
        void handleValidation_returns400_withJoinedFieldErrors() {
            BindingResult bindingResult = Mockito.mock(BindingResult.class);
            given(bindingResult.getFieldErrors()).willReturn(List.of(
                    new FieldError("createProductRequest", "price", "must be greater than or equal to 0"),
                    new FieldError("createProductRequest", "stock", "must be greater than or equal to 0")
            ));

            MethodArgumentNotValidException ex = Mockito.mock(MethodArgumentNotValidException.class);
            given(ex.getBindingResult()).willReturn(bindingResult);

            var response = handler.handleValidation(ex, webRequest);

            assertThat(response.getStatusCode().value()).isEqualTo(400);
            Problem p = response.getBody();
            assertThat(p).isNotNull();
            assertThat(p.getTitle()).isEqualTo("Validation failed");
            assertThat(p.getStatus()).isEqualTo(400);
            assertThat(p.getDetail())
                    .contains("price: must be greater than or equal to 0")
                    .contains("stock: must be greater than or equal to 0");
            assertThat(p.getType()).isEqualTo(URI.create("https://example.com/problems/validation-error"));
            assertThat(p.getInstance()).isEqualTo(URI.create("/api/v1/products"));
        }
    }

    @Nested
    @DisplayName("Unexpected")
    class Unexpected {

        @Test
        void handleUnexpected_returns500_genericProblem() {
            var ex = new RuntimeException("boom");

            var response = handler.handleUnexpected(ex, webRequest);

            assertThat(response.getStatusCode().value()).isEqualTo(500);
            Problem p = response.getBody();
            assertThat(p).isNotNull();
            assertThat(p.getTitle()).isEqualTo("Internal error");
            assertThat(p.getStatus()).isEqualTo(500);
            assertThat(p.getDetail()).contains("Unexpected error");
            assertThat(p.getType()).isEqualTo(URI.create("https://example.com/problems/internal-error"));
            assertThat(p.getInstance()).isEqualTo(URI.create("/api/v1/products"));
        }
    }
}