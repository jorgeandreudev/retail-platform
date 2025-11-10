package com.jorgeandreu.orders.infastructure.web;

import com.jorgeandreu.orders.application.mapper.CreateOrderMapper;
import com.jorgeandreu.orders.domain.model.OrderStatus;
import com.jorgeandreu.orders.domain.model.PageResult;
import com.jorgeandreu.orders.domain.port.in.CancelOrderUseCase;
import com.jorgeandreu.orders.domain.port.in.CreateOrderCommand;
import com.jorgeandreu.orders.domain.port.in.CreateOrderUseCase;
import com.jorgeandreu.orders.domain.port.in.GetOrderUseCase;
import com.jorgeandreu.orders.domain.port.in.SearchCriteriaCommand;
import com.jorgeandreu.orders.infrastructure.api.model.CreateOrderRequest;
import com.jorgeandreu.orders.infrastructure.api.model.Order;
import com.jorgeandreu.orders.infrastructure.api.model.OrderPage;
import com.jorgeandreu.orders.infrastructure.api.model.OrderSearchCriteriaRequest;
import com.jorgeandreu.orders.infrastructure.web.OrderWebMapper;
import com.jorgeandreu.orders.infrastructure.web.OrdersApiDelegateImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrdersApiDelegateImpl.
 */
class OrdersApiDelegateImplTest {

    @Mock private CreateOrderUseCase createOrderUseCase;
    @Mock private CreateOrderMapper createOrderMapper;
    @Mock private OrderWebMapper orderWebMapper;
    @Mock private GetOrderUseCase getOrderUseCase;
    @Mock private CancelOrderUseCase cancelOrderUseCase;
    @Mock private HttpServletRequest httpRequest;

    @InjectMocks
    private OrdersApiDelegateImpl delegate;

    private com.jorgeandreu.orders.domain.model.Order sampleDomain;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        sampleDomain = new com.jorgeandreu.orders.domain.model.Order(
                UUID.randomUUID(),
                OrderStatus.PENDING,
                "john.doe@example.com",
                List.of(),
                new BigDecimal("49.99"),
                Instant.now(),
                Instant.now(),
                0L
        );
    }

    @Test
    void createOrder_returnsCreatedResponse_withLocation() {
        CreateOrderRequest req = new CreateOrderRequest()
                .customerEmail("john.doe@example.com")
                .items(List.of());

        CreateOrderCommand createCmd = new CreateOrderCommand("john.doe@example.com", List.of(), null);

        Order apiOrder = new Order()
                .id(sampleDomain.id())
                .status(Order.StatusEnum.PENDING)
                .customerEmail(sampleDomain.customerEmail())
                .items(List.of())
                .total(sampleDomain.total().doubleValue())
                .createdAt(java.time.OffsetDateTime.now())
                .updatedAt(java.time.OffsetDateTime.now())
                .version(0);

        when(createOrderMapper.toCommand(eq(req), isNull())).thenReturn(createCmd);
        when(createOrderUseCase.create(createCmd)).thenReturn(sampleDomain);
        when(orderWebMapper.toApi(sampleDomain)).thenReturn(apiOrder);

        ResponseEntity<Order> response = delegate.createOrder(req, null);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(Objects.requireNonNull(response.getBody()).getId()).isEqualTo(sampleDomain.id());
        assertThat(response.getHeaders().getLocation())
                .isEqualTo(URI.create("/api/v1/orders/" + sampleDomain.id()));

        verify(createOrderMapper).toCommand(eq(req), isNull());
        verify(createOrderUseCase).create(createCmd);
        verify(orderWebMapper).toApi(sampleDomain);
        verifyNoMoreInteractions(createOrderUseCase, orderWebMapper, createOrderMapper);
    }

    @Test
    void createOrder_withIdempotencyKey_passesKeyToMapper() {
        String key = "idem-123";
        CreateOrderRequest req = new CreateOrderRequest()
                .customerEmail("john.doe@example.com")
                .items(List.of());

        CreateOrderCommand createCmd = new CreateOrderCommand("john.doe@example.com", List.of(), null);

        Order apiOrder = new Order().id(sampleDomain.id());
        when(createOrderMapper.toCommand(req, key)).thenReturn(createCmd);
        when(createOrderUseCase.create(createCmd)).thenReturn(sampleDomain);
        when(orderWebMapper.toApi(sampleDomain)).thenReturn(apiOrder);

        ResponseEntity<Order> response = delegate.createOrder(req, key);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(Objects.requireNonNull(response.getBody()).getId()).isEqualTo(sampleDomain.id());
        assertThat(response.getHeaders().getLocation())
                .isEqualTo(URI.create("/api/v1/orders/" + sampleDomain.id()));

        verify(createOrderMapper).toCommand(req, key);
        verify(createOrderUseCase).create(createCmd);
        verify(orderWebMapper).toApi(sampleDomain);
    }

    @Test
    void getOrderById_returnsOkResponse() {
        UUID id = sampleDomain.id();
        Order apiOrder = new Order().id(id);

        when(getOrderUseCase.getById(id)).thenReturn(sampleDomain);
        when(orderWebMapper.toApi(sampleDomain)).thenReturn(apiOrder);

        ResponseEntity<Order> response = delegate.getOrderById(id);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(Objects.requireNonNull(response.getBody()).getId()).isEqualTo(id);

        verify(getOrderUseCase).getById(id);
        verify(orderWebMapper).toApi(sampleDomain);
        verifyNoMoreInteractions(getOrderUseCase, orderWebMapper);
    }

    @Test
    void listOrders_withParams_buildsCriteriaAndReturnsPage() {
        Integer page = 2, size = 5;
        String sort = "createdAt:asc";

        PageResult<com.jorgeandreu.orders.domain.model.Order> pageResult =
                new PageResult<>(List.of(sampleDomain), 2, 5, 1, 1);

        OrderPage apiPage = new OrderPage()
                .page(2).size(5).totalElements(1).totalPages(1);

        ArgumentCaptor<SearchCriteriaCommand> captor = ArgumentCaptor.forClass(SearchCriteriaCommand.class);

        when(getOrderUseCase.list(any())).thenReturn(pageResult);
        when(orderWebMapper.toApi(pageResult)).thenReturn(apiPage);

        ResponseEntity<OrderPage> resp = delegate.listOrders(page, size, sort);

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isSameAs(apiPage);

        verify(getOrderUseCase).list(captor.capture());
        SearchCriteriaCommand used = captor.getValue();
        assertThat(used.page()).isEqualTo(2);
        assertThat(used.size()).isEqualTo(5);
        assertThat(used.sort()).isEqualTo("createdAt:asc");
        assertThat(used.status()).isNull();
        assertThat(used.customerEmail()).isNull();
        assertThat(used.minTotal()).isNull();
        assertThat(used.maxTotal()).isNull();
        assertThat(used.from()).isNull();
        assertThat(used.to()).isNull();

        verify(orderWebMapper).toApi(pageResult);
        verifyNoMoreInteractions(getOrderUseCase, orderWebMapper);
    }

    @Test
    void listOrders_defaultsWhenNullParams_returnsPage() {
        // given
        Integer page = null, size = null;
        String sort = null;

        PageResult<com.jorgeandreu.orders.domain.model.Order> pageResult =
                new PageResult<>(List.of(sampleDomain), 0, 20, 1, 1);
        OrderPage apiPage = new OrderPage().page(0).size(20).totalElements(1).totalPages(1);

        when(getOrderUseCase.list(any())).thenReturn(pageResult);
        when(orderWebMapper.toApi(pageResult)).thenReturn(apiPage);

        ArgumentCaptor<SearchCriteriaCommand> captor = ArgumentCaptor.forClass(SearchCriteriaCommand.class);

        ResponseEntity<OrderPage> resp = delegate.listOrders(page, size, sort);

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isSameAs(apiPage);

        verify(getOrderUseCase).list(captor.capture());
        SearchCriteriaCommand used = captor.getValue();

        assertThat(used.page()).isZero();
        assertThat(used.size()).isEqualTo(20);
        assertThat(used.sort()).isEqualTo("createdAt:desc");
        assertThat(used.status()).isNull();
        assertThat(used.customerEmail()).isNull();
        assertThat(used.minTotal()).isNull();
        assertThat(used.maxTotal()).isNull();
        assertThat(used.from()).isNull();
        assertThat(used.to()).isNull();

        verify(orderWebMapper).toApi(pageResult);
        verifyNoMoreInteractions(getOrderUseCase, orderWebMapper);
    }

    @Test
    void cancelOrder_withIfMatchHeader_passesExpectedVersion() {
        UUID id = UUID.randomUUID();
        when(httpRequest.getHeader("If-Match")).thenReturn("\"5\""); // typical ETag format with quotes

        ResponseEntity<Void> resp = delegate.cancelOrder(id);

        assertThat(resp.getStatusCode().value()).isEqualTo(204);
        assertThat(resp.getBody()).isNull();

        ArgumentCaptor<Optional<Long>> captor = ArgumentCaptor.forClass(Optional.class);
        verify(cancelOrderUseCase).cancelOrder(eq(id), captor.capture());
        assertThat(captor.getValue()).contains(5L);

        verifyNoMoreInteractions(cancelOrderUseCase);
    }

    @Test
    void cancelOrder_withoutIfMatchHeader_passesEmptyExpectedVersion() {
        UUID id = UUID.randomUUID();
        when(httpRequest.getHeader("If-Match")).thenReturn(null);

        ResponseEntity<Void> resp = delegate.cancelOrder(id);

        assertThat(resp.getStatusCode().value()).isEqualTo(204);
        assertThat(resp.getBody()).isNull();

        ArgumentCaptor<Optional<Long>> captor = ArgumentCaptor.forClass(Optional.class);
        verify(cancelOrderUseCase).cancelOrder(eq(id), captor.capture());
        assertThat(captor.getValue()).isEmpty();

        verifyNoMoreInteractions(cancelOrderUseCase);
    }

    @Test
    void searchOrders_forwardsToUseCaseAndMapper() {
        OrderSearchCriteriaRequest req = new OrderSearchCriteriaRequest()
                .page(0).size(10).sort("createdAt:desc"); // add filters if needed in your mapper

        SearchCriteriaCommand cmd = new SearchCriteriaCommand(
                null, null, null, null, null, null, 0, 10, "createdAt:desc"
        );

        PageResult<com.jorgeandreu.orders.domain.model.Order> pageResult =
                new PageResult<>(List.of(sampleDomain), 0, 10, 1, 1);
        OrderPage apiPage = new OrderPage().page(0).size(10).totalElements(1).totalPages(1);

        when(orderWebMapper.toCommand(req)).thenReturn(cmd);
        when(getOrderUseCase.list(cmd)).thenReturn(pageResult);
        when(orderWebMapper.toApi(pageResult)).thenReturn(apiPage);

        ResponseEntity<OrderPage> resp = delegate.searchOrders(req);

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isSameAs(apiPage);

        verify(orderWebMapper).toCommand(req);
        verify(getOrderUseCase).list(cmd);
        verify(orderWebMapper).toApi(pageResult);
        verifyNoMoreInteractions(orderWebMapper, getOrderUseCase);
    }
}

