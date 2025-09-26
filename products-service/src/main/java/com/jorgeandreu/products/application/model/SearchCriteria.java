package com.jorgeandreu.products.application.model;

public record SearchCriteria(
        int page, int size, String sort,
        String category, Double minPrice, Double maxPrice, String text,
        boolean includeDeleted
) {}
