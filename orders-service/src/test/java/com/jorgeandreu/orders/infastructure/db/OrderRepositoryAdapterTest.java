package com.jorgeandreu.orders.infastructure.db;

import com.jorgeandreu.orders.application.exception.OrderSearchBadRequest;
import com.jorgeandreu.orders.domain.model.Order;
import com.jorgeandreu.orders.domain.model.OrderItem;
import com.jorgeandreu.orders.domain.model.OrderStatus;
import com.jorgeandreu.orders.domain.model.PageResult;
import com.jorgeandreu.orders.domain.model.SearchCriteria;
import com.jorgeandreu.orders.infrastructure.db.OrderDocument;
import com.jorgeandreu.orders.infrastructure.db.OrderRepositoryAdapter;
import com.jorgeandreu.orders.infrastructure.db.SpringDataOrderRepository;
import com.jorgeandreu.orders.infrastructure.db.mapper.OrderDocumentMapper;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class OrderRepositoryAdapterTest {

    private SpringDataOrderRepository repository;
    private OrderDocumentMapper mapper;
    private MongoTemplate mongoTemplate;

    private OrderRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = mock(SpringDataOrderRepository.class);
        mapper = mock(OrderDocumentMapper.class);
        mongoTemplate = mock(MongoTemplate.class);
        adapter = new OrderRepositoryAdapter(repository, mapper, mongoTemplate);
    }

    private Order sampleOrder() {
        return new Order(
                UUID.randomUUID(),
                OrderStatus.PENDING,
                "john@doe.com",
                List.of(new OrderItem(UUID.randomUUID().toString(), "SKU-1", "Prod 1", new BigDecimal("10.00"), 2)),
                new BigDecimal("20.00"),
                Instant.parse("2025-01-01T00:00:00Z"),
                Instant.parse("2025-01-01T00:00:00Z"),
                0L
        );
    }

    private OrderDocument sampleDocFrom(Order o) {
        OrderDocument d = new OrderDocument();
        d.setId(o.id().toString());
        d.setStatus(o.status().name());
        d.setCustomerEmail(o.customerEmail());
        d.setTotal(o.total());
        d.setCreatedAt(o.createdAt());
        d.setUpdatedAt(o.updatedAt());
        d.setVersion(o.version());
        return d;
    }

    @Test
    void save_mapsAndPersists() {
        Order order = sampleOrder();
        OrderDocument toSave = sampleDocFrom(order);
        OrderDocument saved = sampleDocFrom(order);
        saved.setVersion(1L);
        Order mappedBack = new Order(order.id(), order.status(), order.customerEmail(),
                order.items(), order.total(), order.createdAt(), order.updatedAt(), 1L);

        given(mapper.toDocument(order)).willReturn(toSave);
        given(repository.save(toSave)).willReturn(saved);
        given(mapper.toDomain(saved)).willReturn(mappedBack);

        Order result = adapter.save(order);

        assertThat(result.version()).isEqualTo(1L);
        verify(mapper).toDocument(order);
        verify(repository).save(toSave);
        verify(mapper).toDomain(saved);
    }

    @Test
    void existsByIdempotencyKey_behaviour() {
        assertThat(adapter.existsByIdempotencyKey(null)).isFalse();
        assertThat(adapter.existsByIdempotencyKey("   ")).isFalse();

        given(repository.existsByIdempotencyKey("KEY")).willReturn(true);
        assertThat(adapter.existsByIdempotencyKey("KEY")).isTrue();

        verify(repository).existsByIdempotencyKey("KEY");
    }

    @Test
    void findById_maps() {
        Order order = sampleOrder();
        OrderDocument doc = sampleDocFrom(order);

        given(repository.findById(order.id().toString())).willReturn(Optional.of(doc));
        given(mapper.toDomain(doc)).willReturn(order);

        Optional<Order> result = adapter.findById(order.id());

        assertThat(result).contains(order);
        verify(repository).findById(order.id().toString());
        verify(mapper).toDomain(doc);
    }

    @Test
    void findStatusAndVersion_empty() {
        given(mongoTemplate.findOne(any(Query.class), eq(OrderDocument.class))).willReturn(null);
        assertThat(adapter.findStatusAndVersion(UUID.randomUUID())).isEmpty();
    }

    @Test
    void findStatusAndVersion_ok() {
        Order order = sampleOrder();
        OrderDocument doc = sampleDocFrom(order);
        doc.setStatus(OrderStatus.CONFIRMED.name());
        doc.setVersion(5L);

        given(mongoTemplate.findOne(any(Query.class), eq(OrderDocument.class))).willReturn(doc);

        var result = adapter.findStatusAndVersion(order.id());
        assertThat(result).isPresent();
        assertThat(result.get().status()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(result.get().version()).isEqualTo(5L);

        ArgumentCaptor<Query> qCap = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).findOne(qCap.capture(), eq(OrderDocument.class));
        Query used = qCap.getValue();
        assertThat(used.getFieldsObject().keySet()).contains("status", "version");
    }

    @Test
    void cancelIfPendingAndVersionMatches_behaviour() {
        UpdateResult ok = mock(UpdateResult.class);
        given(ok.getModifiedCount()).willReturn(1L);
        UpdateResult no = mock(UpdateResult.class);
        given(no.getModifiedCount()).willReturn(0L);

        UUID id = UUID.randomUUID();

        given(mongoTemplate.updateFirst(any(Query.class), any(), eq(OrderDocument.class)))
                .willReturn(ok);
        assertThat(adapter.cancelIfPendingAndVersionMatches(id, 3L, Instant.now())).isTrue();

        given(mongoTemplate.updateFirst(any(Query.class), any(), eq(OrderDocument.class)))
                .willReturn(no);
        assertThat(adapter.cancelIfPendingAndVersionMatches(id, 3L, Instant.now())).isFalse();
    }

    @Nested
    @DisplayName("search()")
    class SearchTests {

        @Test
        @DisplayName("minTotal > maxTotal -> OrderSearchBadRequest")
        void minGreaterThanMax_throws() {
            SearchCriteria sc = new SearchCriteria(
                    0, 10, "createdAt,desc",
                    "john@doe.com",
                    new BigDecimal("100.00"),
                    new BigDecimal("50.00"),
                    null, null,
                    OrderStatus.PENDING
            );
            assertThatThrownBy(() -> adapter.search(sc))
                    .isInstanceOf(OrderSearchBadRequest.class);
        }

        @Test
        @DisplayName("from > to -> OrderSearchBadRequest")
        void fromAfterTo_throws() {
            SearchCriteria sc = new SearchCriteria(
                    0, 10, "createdAt,desc",
                    null, null, null,
                    Instant.parse("2025-01-02T00:00:00Z"),
                    Instant.parse("2025-01-01T00:00:00Z"),
                    null
            );
            assertThatThrownBy(() -> adapter.search(sc))
                    .isInstanceOf(OrderSearchBadRequest.class);
        }

        @Test
        void mapsAndPages() {
            SearchCriteria sc = new SearchCriteria(
                    1, 5, "total,asc",
                    "john@doe.com",
                    new BigDecimal("10.00"),
                    new BigDecimal("100.00"),
                    Instant.parse("2025-01-01T00:00:00Z"),
                    Instant.parse("2025-02-01T00:00:00Z"),
                    OrderStatus.PENDING
            );

            Order o1 = sampleOrder();
            Order o2 = sampleOrder();

            OrderDocument d1 = sampleDocFrom(o1);
            OrderDocument d2 = sampleDocFrom(o2);

            given(mongoTemplate.find(any(Query.class), eq(OrderDocument.class)))
                    .willReturn(List.of(d1, d2));
            given(mongoTemplate.count(any(Query.class), eq(OrderDocument.class)))
                    .willReturn(17L);

            given(mapper.toDomain(d1)).willReturn(o1);
            given(mapper.toDomain(d2)).willReturn(o2);

            PageResult<Order> page = adapter.search(sc);

            assertThat(page.page()).isEqualTo(1);
            assertThat(page.size()).isEqualTo(5);
            assertThat(page.totalElements()).isEqualTo(17L);
            assertThat(page.totalPages()).isEqualTo((int) Math.ceil(17.0 / 5.0));
            assertThat(page.content()).containsExactly(o1, o2);

            ArgumentCaptor<Query> findCap = ArgumentCaptor.forClass(Query.class);
            ArgumentCaptor<Query> countCap = ArgumentCaptor.forClass(Query.class);

            verify(mongoTemplate).find(findCap.capture(), eq(OrderDocument.class));
            verify(mongoTemplate).count(countCap.capture(), eq(OrderDocument.class));

            Query findQ = findCap.getValue();

            assertThat(findQ.getSkip()).isEqualTo(1L * 5);
            assertThat(findQ.getLimit()).isEqualTo(5);

            Document sort = findQ.getSortObject();
            assertThat(sort).isNotNull();
            assertThat(findQ.getSortObject().toBsonDocument().getFirstKey()).isEqualTo("total");
            assertThat(findQ.getSortObject().toBsonDocument().toJson()).contains("1");
        }

        @Test
        void sizeBoundsAndSortDefault() {
            SearchCriteria sc = new SearchCriteria(
                    -10, 9999, "unknown,asc",
                    null, null, null, null, null, null
            );

            given(mongoTemplate.find(any(Query.class), eq(OrderDocument.class)))
                    .willReturn(List.of());
            given(mongoTemplate.count(any(Query.class), eq(OrderDocument.class)))
                    .willReturn(0L);

            PageResult<Order> page = adapter.search(sc);

            assertThat(page.page()).isZero();
            assertThat(page.size()).isEqualTo(200);

            ArgumentCaptor<Query> findCap = ArgumentCaptor.forClass(Query.class);
            verify(mongoTemplate).find(findCap.capture(), eq(OrderDocument.class));
            Query findQ = findCap.getValue();

            assertThat(findQ.getSkip()).isEqualTo(0L);
            assertThat(findQ.getLimit()).isEqualTo(200);

            Document sort = findQ.getSortObject();
            assertThat(sort).isNotNull();
        }
    }
}

