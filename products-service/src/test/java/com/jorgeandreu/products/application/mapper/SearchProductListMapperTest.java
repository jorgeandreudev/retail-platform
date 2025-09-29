package com.jorgeandreu.products.application.mapper;

import com.jorgeandreu.products.domain.model.SearchCriteria;
import com.jorgeandreu.products.domain.port.in.SearchCriteriaCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SearchProductListMapperTest {

    private SearchProductListMapperImpl mapper;

    @BeforeEach
    void setUp() {
        mapper = new SearchProductListMapperImpl();
    }

    @Test
    @DisplayName("returns null when command is null")
    void returnsNullWhenCommandIsNull() {
        SearchCriteria result = mapper.toDomain(null);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("maps all fields from SearchCriteriaCommand to SearchCriteria")
    void mapsAllFields() {
        SearchCriteriaCommand cmd = new SearchCriteriaCommand(
                2,
                20,
                "price,asc",
                "laptops",
                1000.0,
                2000.0,
                "gaming",
                true
        );

        SearchCriteria result = mapper.toDomain(cmd);

        assertThat(result.page()).isEqualTo(2);
        assertThat(result.size()).isEqualTo(20);
        assertThat(result.sort()).isEqualTo("price,asc");
        assertThat(result.category()).isEqualTo("laptops");
        assertThat(result.minPrice()).isEqualTo(1000.0);
        assertThat(result.maxPrice()).isEqualTo(2000.0);
        assertThat(result.text()).isEqualTo("gaming");
        assertThat(result.includeDeleted()).isTrue();
    }

    @Test
    @DisplayName("handles null fields gracefully")
    void handlesNullFields() {
        SearchCriteriaCommand cmd = new SearchCriteriaCommand(
                0,
                0,
                null,
                null,
                null,
                null,
                null,
                false
        );

        SearchCriteria result = mapper.toDomain(cmd);

        assertThat(result.page()).isZero();
        assertThat(result.size()).isZero();
        assertThat(result.sort()).isNull();
        assertThat(result.category()).isNull();
        assertThat(result.minPrice()).isNull();
        assertThat(result.maxPrice()).isNull();
        assertThat(result.text()).isNull();
        assertThat(result.includeDeleted()).isFalse();
    }
}
