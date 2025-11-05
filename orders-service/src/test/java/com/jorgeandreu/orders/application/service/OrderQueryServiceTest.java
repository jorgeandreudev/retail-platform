package com.jorgeandreu.orders.application.service;

import com.jorgeandreu.orders.application.exception.OrderNotFoundException;
import com.jorgeandreu.orders.application.mapper.SearchOrderListMapper;
import com.jorgeandreu.orders.domain.model.Order;
import com.jorgeandreu.orders.domain.model.OrderStatus;
import com.jorgeandreu.orders.domain.model.PageResult;
import com.jorgeandreu.orders.domain.model.SearchCriteria;
import com.jorgeandreu.orders.domain.port.in.SearchCriteriaCommand;
import com.jorgeandreu.orders.domain.port.out.OrderRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@org.junit.jupiter.api.extension.ExtendWith(MockitoExtension.class)
class OrderQueryServiceTest {

    @Mock private OrderRepositoryPort orders;
    @Mock private SearchOrderListMapper listMapper;

    @InjectMocks
    private OrderQueryService service;

    private UUID id;
    private Order sample;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        sample = new Order(
                id,
                OrderStatus.PENDING,
                "john@example.com",
                List.of(),
                BigDecimal.ZERO,
                Instant.parse("2025-01-01T00:00:00Z"),
                Instant.parse("2025-01-01T00:00:00Z"),
                0L
        );
    }

    @Test
    @DisplayName("getById returns the order when it exists")
    void getById_found() {
        when(orders.findById(id)).thenReturn(Optional.of(sample));

        Order result = service.getById(id);

        assertThat(result).isSameAs(sample);
        verify(orders).findById(id);
        verifyNoMoreInteractions(orders, listMapper);
    }

    @Test
    @DisplayName("getById throws OrderNotFoundException when repository returns empty")
    void getById_notFound() {
        when(orders.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining(id.toString());

        verify(orders).findById(id);
        verifyNoMoreInteractions(orders, listMapper);
    }

    @Test
    @DisplayName("list maps SearchCriteriaCommand to SearchCriteria and delegates to repository.search")
    void list_mapsAndDelegates() {
        SearchCriteriaCommand cmd = new SearchCriteriaCommand(
                OrderStatus.PENDING,
                "john@example.com",
                new BigDecimal("10.00"),
                new BigDecimal("99.99"),
                Instant.parse("2025-01-01T00:00:00Z"),
                Instant.parse("2025-12-31T23:59:59Z"),
                1,
                20,
                "createdAt,desc"
        );

        SearchCriteria mapped = new SearchCriteria(
                1,
                20,
                "createdAt,desc",
                "john@example.com",
                new BigDecimal("10.00"),
                new BigDecimal("99.99"),
                Instant.parse("2025-01-01T00:00:00Z"),
                Instant.parse("2025-12-31T23:59:59Z"),
                OrderStatus.PENDING
        );

        when(listMapper.toDomain(cmd)).thenReturn(mapped);

        PageResult<Order> repoResult = new PageResult<>(List.of(sample), 1, 20, 1, 1);
        when(orders.search(mapped)).thenReturn(repoResult);

        PageResult<Order> page = service.list(cmd);

        assertThat(page).isSameAs(repoResult);

        InOrder inOrder = inOrder(listMapper, orders);
        inOrder.verify(listMapper).toDomain(cmd);
        inOrder.verify(orders).search(mapped);

        verifyNoMoreInteractions(listMapper, orders);
    }

    @Test
    @DisplayName("list propagates repository result metadata and content as-is")
    void list_passThroughResult() {
        SearchCriteriaCommand cmd = new SearchCriteriaCommand(
                null, null, null, null,
                null, null, 0, 10, "createdAt,asc"
        );

        SearchCriteria mapped = new SearchCriteria(
                0, 10, "createdAt,asc",
                null, null, null, null, null, null
        );

        when(listMapper.toDomain(cmd)).thenReturn(mapped);

        PageResult<Order> repoResult = new PageResult<>(List.of(sample), 0, 10, 42, 5);
        when(orders.search(mapped)).thenReturn(repoResult);

        PageResult<Order> page = service.list(cmd);

        assertThat(page.page()).isEqualTo(0);
        assertThat(page.size()).isEqualTo(10);
        assertThat(page.totalElements()).isEqualTo(42);
        assertThat(page.totalPages()).isEqualTo(5);
        assertThat(page.content()).containsExactly(sample);

        verify(listMapper).toDomain(cmd);
        verify(orders).search(mapped);
        verifyNoMoreInteractions(listMapper, orders);
    }
}

