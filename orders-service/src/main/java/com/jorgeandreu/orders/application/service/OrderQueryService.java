package com.jorgeandreu.orders.application.service;

import com.jorgeandreu.orders.application.exception.OrderNotFoundException;
import com.jorgeandreu.orders.application.mapper.SearchOrderListMapper;
import com.jorgeandreu.orders.domain.model.Order;
import com.jorgeandreu.orders.domain.model.PageResult;
import com.jorgeandreu.orders.domain.model.SearchCriteria;
import com.jorgeandreu.orders.domain.port.in.GetOrderUseCase;
import com.jorgeandreu.orders.domain.port.in.SearchCriteriaCommand;
import com.jorgeandreu.orders.domain.port.out.OrderRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderQueryService implements GetOrderUseCase {

    private final OrderRepositoryPort orders;
    private final SearchOrderListMapper listMapper;

    @Override
    public Order getById(UUID id) {
        return orders.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Override
    public PageResult<com.jorgeandreu.orders.domain.model.Order> list(SearchCriteriaCommand command){

        SearchCriteria criteriaDom = listMapper.toDomain(command);

        return orders.search(criteriaDom);
    }
}
