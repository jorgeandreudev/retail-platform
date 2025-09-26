package com.jorgeandreu.products.domain.model;

public record SearchCriteria(
        int page,
        int size,
        String sort,
        String category,
        Double minPrice,
        Double maxPrice,
        String text,
        boolean includeDeleted
) {}
