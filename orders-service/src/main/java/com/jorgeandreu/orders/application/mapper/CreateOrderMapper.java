package com.jorgeandreu.orders.application.mapper;

import com.jorgeandreu.orders.domain.port.in.CreateOrderCommand;
import com.jorgeandreu.orders.infrastructure.api.model.CreateOrderItem;
import com.jorgeandreu.orders.infrastructure.api.model.CreateOrderRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CreateOrderMapper {

    @Mapping(target = "idempotencyKey", source = "idempotencyKey")
    @Mapping(target = "customerEmail", source = "req.customerEmail")
    @Mapping(target = "items", source = "req.items")
    CreateOrderCommand toCommand(CreateOrderRequest req, String idempotencyKey);

    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "quantity",  source = "quantity")
    CreateOrderCommand.Item toCommandItem(CreateOrderItem api);

    List<CreateOrderCommand.Item> toCommandItems(List<CreateOrderItem> api);
}

