package com.jorgeandreu.orders.application.service;

import com.jorgeandreu.orders.domain.model.Order;
import com.jorgeandreu.orders.domain.model.OrderItem;
import com.jorgeandreu.orders.domain.port.in.CreateOrderCommand;
import com.jorgeandreu.orders.domain.port.in.CreateOrderUseCase;
import com.jorgeandreu.orders.domain.port.out.OrderRepositoryPort;
import com.jorgeandreu.orders.domain.port.out.ProductCatalogPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderCreateService implements CreateOrderUseCase {

    private final OrderRepositoryPort orders;

    private final ProductCatalogPort catalog;

    @Override
    public Order create(CreateOrderCommand cmd) {
        var now = Instant.now();

        List<OrderItem> items = cmd.items().stream().map(i -> {
            var snap = catalog.findSnapshotById(i.productId());
            return new OrderItem(
                    snap.productId().toString(), snap.sku(), snap.name(), snap.price(), i.quantity()
            );
        }).toList();

        var pending = Order.newPending(cmd.customerEmail(), items, now);
        return orders.save(pending);
    }
}
