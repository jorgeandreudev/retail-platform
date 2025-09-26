package com.jorgeandreu.products.application.mapper;

import com.jorgeandreu.products.domain.model.Product;
import com.jorgeandreu.products.domain.port.in.CreateProductCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.util.UUID;

@Mapper(componentModel = "spring", imports = {UUID.class, Instant.class})
public interface CreateProductMapper {

    @Mapping(target = "id",         expression = "java(UUID.randomUUID())")
    @Mapping(target = "sku",        source = "cmd.sku")
    @Mapping(target = "name",       source = "cmd.name")
    @Mapping(target = "price",      source = "cmd.price")
    @Mapping(target = "stock",      source = "cmd.stock")
    @Mapping(target = "category",   source = "cmd.category")
    @Mapping(target = "createdAt",  expression = "java(Instant.now())")
    @Mapping(target = "updatedAt",  expression = "java(Instant.now())")
    @Mapping(target = "deletedAt",  expression = "java(null)")
    @Mapping(target = "version",    source = "initialVersion")
    Product toDomain(CreateProductCommand cmd, long initialVersion);

}
