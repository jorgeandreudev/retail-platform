package com.jorgeandreu.orders.infastructure.api.mapper;

import com.jorgeandreu.orders.domain.model.OrderStatus;
import com.jorgeandreu.orders.infrastructure.api.mapper.OrderStatusMapper;
import com.jorgeandreu.orders.infrastructure.api.model.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusMapperTest {

    private final OrderStatusMapper mapper = new OrderStatusMapper();

    @Test
    @DisplayName("returns null when domain status is null")
    void returnsNullOnNull() {
        Order.StatusEnum api = mapper.toApiStatus(null);
        assertThat(api).isNull();
    }

    @Test
    @DisplayName("maps all domain enum values to API enum with same name")
    void mapsAllEnumValues() {
        for (OrderStatus domain : OrderStatus.values()) {
            Order.StatusEnum api = mapper.toApiStatus(domain);
            assertThat(api).isNotNull();
            assertThat(api.getValue()).isEqualTo(domain.name());
            assertThat(Order.StatusEnum.fromValue(domain.name())).isEqualTo(api);
        }
    }
}
