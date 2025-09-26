package com.jorgeandreu.products.application.mapper;

import com.jorgeandreu.products.domain.model.SearchCriteria;
import com.jorgeandreu.products.domain.port.in.SearchCriteriaCommand;
import org.mapstruct.Mapper;

import java.time.Instant;
import java.util.UUID;

@Mapper(componentModel = "spring", imports = {UUID.class, Instant.class})
public interface SearchProductListMapper {

    SearchCriteria toDomain(SearchCriteriaCommand criteria);
}
