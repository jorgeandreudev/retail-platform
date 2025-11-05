package com.jorgeandreu.orders.infastructure.web;

import com.jorgeandreu.orders.application.exception.OrderNotFoundException;
import com.jorgeandreu.orders.application.exception.OrderSearchBadRequest;
import com.jorgeandreu.orders.application.exception.OrderStateConflictException;
import com.jorgeandreu.orders.application.exception.OrderVersionConflictException;
import com.jorgeandreu.orders.application.exception.ProductNotFoundInOrderException;
import com.jorgeandreu.orders.infrastructure.api.model.Problem;
import com.jorgeandreu.orders.infrastructure.web.GlobalExceptionHandler;
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
        given(webRequest.getDescription(false)).willReturn("uri=/api/v1/orders");
    }

    @Nested
    @DisplayName("Order not found")
    class OrderNotFound {

        @Test
        void handleOrderNotFound_returns404_withProblemPayload() {
            UUID id = UUID.randomUUID();
            var ex = new OrderNotFoundException(id);

            var response = handler.handleOrderNotFound(ex, webRequest);

            assertThat(response.getStatusCode().value()).isEqualTo(404);
            Problem p = response.getBody();
            assertThat(p).isNotNull();
            assertThat(p.getTitle()).isEqualTo("Order not found");
            assertThat(p.getStatus()).isEqualTo(404);
            assertThat(p.getDetail()).contains(id.toString());
            assertThat(p.getType()).isEqualTo(URI.create("https://example.com/problems/not-found"));
            assertThat(p.getInstance()).isEqualTo(URI.create("/api/v1/orders"));
        }
    }

    @Nested
    @DisplayName("Product in order not found")
    class ProductNotFoundInOrder {

        @Test
        void handleProductNotFoundInOrder_returns404_withProblemPayload() {
            UUID pid = UUID.randomUUID();
            var ex = new ProductNotFoundInOrderException(pid);

            var response = handler.handleProductNotFoundInOrder(ex, webRequest);

            assertThat(response.getStatusCode().value()).isEqualTo(404);
            Problem p = response.getBody();
            assertThat(p).isNotNull();
            assertThat(p.getTitle()).isEqualTo("Product in order not found");
            assertThat(p.getStatus()).isEqualTo(404);
            assertThat(p.getDetail()).contains(pid.toString());
            assertThat(p.getType()).isEqualTo(URI.create("https://example.com/problems/not-found"));
            assertThat(p.getInstance()).isEqualTo(URI.create("/api/v1/orders"));
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
            assertThat(p.getInstance()).isEqualTo(URI.create("/api/v1/orders"));
        }
    }

    @Nested
    @DisplayName("Validation (Bean Validation)")
    class Validation {

        @Test
        void handleValidation_returns400_withJoinedFieldErrors() {
            BindingResult bindingResult = Mockito.mock(BindingResult.class);
            given(bindingResult.getFieldErrors()).willReturn(List.of(
                    new FieldError("createOrderRequest", "customerEmail", "must be a well-formed email address"),
                    new FieldError("createOrderRequest", "items[0].quantity", "must be greater than or equal to 1")
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
                    .contains("customerEmail: must be a well-formed email address")
                    .contains("items[0].quantity: must be greater than or equal to 1");
            assertThat(p.getType()).isEqualTo(URI.create("https://example.com/problems/validation-error"));
            assertThat(p.getInstance()).isEqualTo(URI.create("/api/v1/orders"));
        }
    }

    @Nested
    @DisplayName("Search bad request")
    class SearchBadRequest {

        @Test
        void searchOrderBadRequest_returns400_withProblemPayload() {
            var ex = new OrderSearchBadRequest(null);

            var response = handler.searchOrderBadRequest(ex, webRequest);

            assertThat(response.getStatusCode().value()).isEqualTo(400);
            Problem p = response.getBody();
            assertThat(p).isNotNull();
            assertThat(p.getTitle()).isEqualTo("Validation failed");
            assertThat(p.getStatus()).isEqualTo(400);
            assertThat(p.getType()).isEqualTo(URI.create("https://example.com/problems/validation-error"));
            assertThat(p.getInstance()).isEqualTo(URI.create("/api/v1/orders"));
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
            assertThat(p.getInstance()).isEqualTo(URI.create("/api/v1/orders"));
        }
    }

    @Nested
    @DisplayName("Version conflict")
    class VersionConflict {

        @Test
        void handleVersionConflict_returns409_withProblemPayload() {
            UUID id = UUID.randomUUID();
            var ex = new OrderVersionConflictException(id, 5);

            var response = handler.handleVersionConflict(ex, webRequest);

            assertThat(response.getStatusCode().value()).isEqualTo(409);
            Problem p = response.getBody();
            assertThat(p).isNotNull();
            assertThat(p.getTitle()).isEqualTo("Version conflict");
            assertThat(p.getStatus()).isEqualTo(409);
            assertThat(p.getDetail()).contains(id.toString()).contains("5");
            assertThat(p.getType()).isEqualTo(URI.create("https://example.com/problems/version-conflict"));
            assertThat(p.getInstance()).isEqualTo(URI.create("/api/v1/orders"));
        }
    }

    @Nested
    @DisplayName("Order state conflict")
    class StateConflict {

        @Test
        void handleOrderStateConflict_returns409_withProblemPayload() {
            var ex = new OrderStateConflictException("Cannot cancel a CONFIRMED order");

            var response = handler.handleOrderStateConflict(ex, webRequest);

            assertThat(response.getStatusCode().value()).isEqualTo(409);
            Problem p = response.getBody();
            assertThat(p).isNotNull();
            assertThat(p.getTitle()).isEqualTo("Order State conflict");
            assertThat(p.getStatus()).isEqualTo(409);
            assertThat(p.getDetail()).contains("Cannot cancel a CONFIRMED order");
            assertThat(p.getType()).isEqualTo(URI.create("https://example.com/problems/validation-error"));
            assertThat(p.getInstance()).isEqualTo(URI.create("/api/v1/orders"));
        }

        @Test
        void handleOrderStateConflict_returns409_withProblemUri() {
            var ex = new OrderStateConflictException("Cannot cancel a CONFIRMED order");

            var response = handler.handleOrderStateConflict(ex, webRequest);

            assertThat(response.getStatusCode().value()).isEqualTo(409);
            Problem p = response.getBody();
            assertThat(p).isNotNull();
            assertThat(p.getTitle()).isEqualTo("Order State conflict");
            assertThat(p.getStatus()).isEqualTo(409);
            assertThat(p.getDetail()).contains("Cannot cancel a CONFIRMED order");
            assertThat(p.getInstance()).isEqualTo(URI.create("/api/v1/orders"));
        }
    }
}

