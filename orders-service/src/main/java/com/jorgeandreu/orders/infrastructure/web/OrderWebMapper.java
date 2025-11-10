package com.jorgeandreu.orders.infrastructure.web;

import com.jorgeandreu.orders.domain.model.Order;
import com.jorgeandreu.orders.domain.model.OrderItem;
import com.jorgeandreu.orders.domain.model.PageResult;
import com.jorgeandreu.orders.domain.port.in.SearchCriteriaCommand;
import com.jorgeandreu.orders.infrastructure.api.model.OrderPage;
import com.jorgeandreu.orders.infrastructure.api.model.OrderSearchCriteriaRequest;
import com.jorgeandreu.orders.infrastructure.util.mapping.CommonTypeConverters;
import com.jorgeandreu.orders.infrastructure.web.utils.ApiConverters;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = { TimeMapper.class, CommonTypeConverters.class, ApiConverters.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface OrderWebMapper {

    @Named("toApi")
    @Mapping(target = "id",        source = "id",        qualifiedByName = "stringToUuid")
    @Mapping(target = "status",    source = "status",    qualifiedByName = "statusToApi")
    @Mapping(target = "customerEmail", source = "customerEmail")
    @Mapping(target = "items",     source = "items")
    @Mapping(target = "total",     source = "total",     qualifiedByName = "bigDecimalToDouble")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "instantToOffset")
    @Mapping(target = "updatedAt", source = "updatedAt", qualifiedByName = "instantToOffset")
    @Mapping(target = "version",   expression = "java(o.version() == null ? 0 : o.version().intValue())")
    com.jorgeandreu.orders.infrastructure.api.model.Order toApi(Order o);

    @Mapping(target = "productId", source = "productId", qualifiedByName = "stringToUuid")
    @Mapping(target = "sku",       source = "sku")
    @Mapping(target = "name",      source = "name")
    @Mapping(target = "unitPrice", source = "unitPrice", qualifiedByName = "bigDecimalToDouble")
    @Mapping(target = "quantity",  source = "quantity")
    @Mapping(
            target = "lineTotal",
            expression = "java(domain.lineTotal() == null ? null : domain.lineTotal().doubleValue())"
    )
    com.jorgeandreu.orders.infrastructure.api.model.OrderItem toApiItem(OrderItem domain);

    List<com.jorgeandreu.orders.infrastructure.api.model.OrderItem> toApiItems(List<OrderItem> domain);

    @Mapping(target = "status", source = "filters.status")
    @Mapping(target = "minTotal", source = "filters.minTotal")
    @Mapping(target = "maxTotal", source = "filters.maxTotal")
    @Mapping(target = "customerEmail", source = "filters.email")
    @Mapping(target = "from", source = "filters.from")
    @Mapping(target = "to", source = "filters.to")
    SearchCriteriaCommand toCommand(OrderSearchCriteriaRequest req);

    @Mapping(target = "content", qualifiedByName = "toApi")
    OrderPage toApi(PageResult<Order> pageResult);


}