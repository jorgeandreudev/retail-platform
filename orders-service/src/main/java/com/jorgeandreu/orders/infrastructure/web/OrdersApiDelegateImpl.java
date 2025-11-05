package com.jorgeandreu.orders.infrastructure.web;

import com.jorgeandreu.orders.application.mapper.CreateOrderMapper;
import com.jorgeandreu.orders.domain.model.PageResult;
import com.jorgeandreu.orders.domain.port.in.CancelOrderUseCase;
import com.jorgeandreu.orders.domain.port.in.CreateOrderUseCase;
import com.jorgeandreu.orders.domain.port.in.GetOrderUseCase;
import com.jorgeandreu.orders.domain.port.in.SearchCriteriaCommand;
import com.jorgeandreu.orders.infrastructure.api.OrdersApiDelegate;
import com.jorgeandreu.orders.infrastructure.api.model.CreateOrderRequest;
import com.jorgeandreu.orders.infrastructure.api.model.Order;
import com.jorgeandreu.orders.infrastructure.api.model.OrderPage;
import com.jorgeandreu.orders.infrastructure.api.model.OrderSearchCriteriaRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrdersApiDelegateImpl implements OrdersApiDelegate {

    private final CreateOrderUseCase createOrderUseCase;

    private final CreateOrderMapper createOrderMapper;

    private final OrderWebMapper orderWebMapper;

    private final GetOrderUseCase getOrderUseCase;

    private final CancelOrderUseCase cancelOrderUseCase;

    private final HttpServletRequest request;


    @Override
    public ResponseEntity<Order> createOrder(
            CreateOrderRequest req, @Nullable String idempotencyKey) {

        var cmd = createOrderMapper.toCommand(req, idempotencyKey);
        var created = createOrderUseCase.create(cmd);
        var body = orderWebMapper.toApi(created);

        var location = URI.create("/api/v1/orders/" + body.getId());
        return ResponseEntity.created(location).body(body);
    }

    @Override
    public ResponseEntity<Order> getOrderById(UUID id) {
        com.jorgeandreu.orders.domain.model.Order order = getOrderUseCase.getById(id);
        return ResponseEntity.ok(orderWebMapper.toApi(order));
    }

    @Override
    public ResponseEntity<OrderPage> listOrders(Integer page, Integer size, String sort) {
        var cmd = new SearchCriteriaCommand(
                null,
                null,
                null,
                null,
                null,
                null,
                page == null ? 0 : page,
                size == null ? 20 : size,
                sort == null ? "createdAt:desc" : sort
        );

        PageResult<com.jorgeandreu.orders.domain.model.Order> pageResult = getOrderUseCase.list(cmd);
        return ResponseEntity.ok(orderWebMapper.toApi(pageResult));
    }

    @Override
    public ResponseEntity<Void> cancelOrder(UUID id) {
        Optional<Long> expectedVersion = Optional.ofNullable(request.getHeader("If-Match"))
                .map(h -> h.replace("\"", ""))
                .filter(s -> !s.isBlank())
                .map(Long::valueOf);

        cancelOrderUseCase.cancelOrder(id, expectedVersion);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<OrderPage> searchOrders(OrderSearchCriteriaRequest req){
        var cmd = orderWebMapper.toCommand(req);
        var page = getOrderUseCase.list(cmd);
        return ResponseEntity.ok(orderWebMapper.toApi(page));
    }
}

