package com.jorgeandreu.orders.infrastructure.util.mapping;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

@MapperConfig(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = { CommonTypeConverters.class }
)
public interface GlobalMapperConfig {}

