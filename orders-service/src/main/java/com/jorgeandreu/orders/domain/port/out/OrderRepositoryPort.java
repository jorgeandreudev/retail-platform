package com.jorgeandreu.orders.domain.port.out;

import com.jorgeandreu.orders.domain.model.Order;
import com.jorgeandreu.orders.domain.model.OrderStatus;
import com.jorgeandreu.orders.domain.model.PageResult;
import com.jorgeandreu.orders.domain.model.SearchCriteria;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepositoryPort {
    record StatusVersion(OrderStatus status, long version) {}

    Order save(Order order);

    boolean existsByIdempotencyKey(String key);

    Optional<Order> findById(UUID id);

    Optional<StatusVersion> findStatusAndVersion(UUID id);

    boolean cancelIfPendingAndVersionMatches(UUID id, long expectedVersion, Instant ts);

    PageResult<Order> search(SearchCriteria criteria);


}
