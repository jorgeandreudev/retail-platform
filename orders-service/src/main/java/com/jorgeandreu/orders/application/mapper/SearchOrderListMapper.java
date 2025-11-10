package com.jorgeandreu.orders.application.mapper;

import com.jorgeandreu.orders.domain.model.SearchCriteria;
import com.jorgeandreu.orders.domain.port.in.SearchCriteriaCommand;
import org.mapstruct.Mapper;

import java.time.Instant;
import java.util.UUID;

@Mapper(componentModel = "spring", imports = {UUID.class, Instant.class})
public interface SearchOrderListMapper {
    SearchCriteria toDomain(SearchCriteriaCommand criteria);

}
