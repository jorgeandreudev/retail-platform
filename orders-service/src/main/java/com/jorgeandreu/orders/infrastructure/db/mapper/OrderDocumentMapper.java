package com.jorgeandreu.orders.infrastructure.db.mapper;

import com.jorgeandreu.orders.domain.model.Order;
import com.jorgeandreu.orders.domain.model.OrderItem;
import com.jorgeandreu.orders.infrastructure.db.ItemDocument;
import com.jorgeandreu.orders.infrastructure.db.OrderDocument;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderDocumentMapper {

    @Mapping(target = "id", expression = "java(o.id() != null ? o.id().toString() : null)")
    @Mapping(target = "status",      expression = "java(o.status().name())")
    @Mapping(target = "customerEmail", expression = "java(o.customerEmail())")
    @Mapping(target = "items",       source = "items")
    @Mapping(target = "total",       expression = "java(o.total())")
    @Mapping(target = "createdAt",   expression = "java(o.createdAt())")
    @Mapping(target = "updatedAt",   expression = "java(o.updatedAt())")
    @Mapping(target = "version",     expression = "java(o.version())")
    OrderDocument toDocument(Order o);

    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "sku",       source = "sku")
    @Mapping(target = "name",      source = "name")
    @Mapping(target = "unitPrice", source = "unitPrice")
    @Mapping(target = "quantity",  source = "quantity")
    @Mapping(target = "lineTotal", expression = "java(domain.lineTotal())")
    ItemDocument toItemDocument(OrderItem domain);

    List<ItemDocument> toItemDocuments(List<OrderItem> domain);

    @Mapping(target = "id", expression = "java(d.getId() != null ? java.util.UUID.fromString(d.getId()) : null)")
    @Mapping(target = "status",      expression = "java(OrderStatus.valueOf(d.getStatus()))")
    @Mapping(target = "customerEmail", expression = "java(d.getCustomerEmail())")
    @Mapping(target = "items",       source = "items")
    @Mapping(target = "total",       expression = "java(d.getTotal())")
    @Mapping(target = "createdAt",   expression = "java(d.getCreatedAt())")
    @Mapping(target = "updatedAt",   expression = "java(d.getUpdatedAt())")
    @Mapping(target = "version",     expression = "java(d.getVersion())")
    Order toDomain(OrderDocument d);

    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "sku",       source = "sku")
    @Mapping(target = "name",      source = "name")
    @Mapping(target = "unitPrice", source = "unitPrice")
    @Mapping(target = "quantity",  source = "quantity")
    @BeanMapping(ignoreByDefault = true)
    OrderItem toDomainItem(ItemDocument doc);

    List<OrderItem> toDomainItems(List<ItemDocument> doc);
}
