package com.jorgeandreu.products.domain.port.in;

public record SearchCriteriaCommand(
        int page,
        int size,
        String sort,
        String category,
        Double minPrice,
        Double maxPrice,
        String text,
        boolean includeDeleted
) {}
