package com.jorgeandreu.orders.infastructure.web;

import com.jorgeandreu.orders.domain.model.OrderItem;
import com.jorgeandreu.orders.domain.model.OrderStatus;
import com.jorgeandreu.orders.domain.model.PageResult;
import com.jorgeandreu.orders.infrastructure.api.model.OrderPage;
import com.jorgeandreu.orders.infrastructure.api.model.OrderSearchCriteriaRequest;
import com.jorgeandreu.orders.infrastructure.api.model.OrderSearchCriteriaRequestFilters;
import com.jorgeandreu.orders.infrastructure.util.mapping.CommonTypeConverters;
import com.jorgeandreu.orders.infrastructure.web.OrderWebMapperImpl;
import com.jorgeandreu.orders.infrastructure.web.TimeMapper;
import com.jorgeandreu.orders.infrastructure.web.utils.ApiConverters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderWebMapperTest {

    private OrderWebMapperImpl mapper;

    private final TimeMapper timeMapperStub = new TimeMapper() { };
    private final CommonTypeConverters convertersStub = new CommonTypeConverters() { };
    private final ApiConverters apiConvertersStub = new ApiConverters() { };

    @BeforeEach
    void setUp() {
        mapper = new OrderWebMapperImpl();
        ReflectionTestUtils.setField(mapper, "timeMapper", timeMapperStub);
        ReflectionTestUtils.setField(mapper, "commonTypeConverters", convertersStub);
        ReflectionTestUtils.setField(mapper, "apiConverters", apiConvertersStub);
    }

    @Nested
    class ToApiOrder {

        @Test @DisplayName("returns null when domain is null")
        void returnsNullOnNullDomain() {
            var api = mapper.toApi((com.jorgeandreu.orders.domain.model.Order) null);
            assertThat(api).isNull();
        }

        @Test @DisplayName("maps all fields (including time and version null -> 0)")
        void mapsAllFields() {
            var id = UUID.randomUUID();
            var created = Instant.parse("2025-01-10T10:00:00Z");
            var updated = Instant.parse("2025-01-11T11:11:11Z");

            var domain = new com.jorgeandreu.orders.domain.model.Order(
                    id,
                    OrderStatus.PENDING,
                    "john.doe@example.com",
                    List.of(
                            new OrderItem(UUID.randomUUID().toString(), "SKU-1", "Prod 1", new BigDecimal("10.00"), 2)
                    ),
                    new BigDecimal("20.00"),
                    created,
                    updated,
                    null
            );

            var api = mapper.toApi(domain);

            assertThat(api.getId()).isEqualTo(id);
            assertThat(api.getStatus()).isEqualTo(com.jorgeandreu.orders.infrastructure.api.model.Order.StatusEnum.PENDING);
            assertThat(api.getCustomerEmail()).isEqualTo("john.doe@example.com");
            assertThat(api.getTotal()).isEqualTo(20.00);
            assertThat(api.getCreatedAt()).isEqualTo(created.atOffset(ZoneOffset.UTC));
            assertThat(api.getUpdatedAt()).isEqualTo(updated.atOffset(ZoneOffset.UTC));
            assertThat(api.getVersion()).isZero();
            assertThat(api.getItems()).hasSize(1);
        }
    }

    @Nested
    class ToApiOrderItem {

        @Test @DisplayName("maps item fields and computed lineTotal")
        void mapsItem() {
            var pid = UUID.randomUUID().toString();
            var domainItem = new OrderItem(pid, "SKU-9", "Gaming Mouse",
                    new BigDecimal("25.50"), 3);

            var apiItem = mapper.toApiItem(domainItem);

            assertThat(apiItem.getProductId().toString()).isEqualTo(pid);
            assertThat(apiItem.getSku()).isEqualTo("SKU-9");
            assertThat(apiItem.getName()).isEqualTo("Gaming Mouse");
            assertThat(apiItem.getUnitPrice()).isEqualTo(25.50);
            assertThat(apiItem.getQuantity()).isEqualTo(3);
            assertThat(apiItem.getLineTotal()).isEqualTo(76.50);
        }

        @Test @DisplayName("toApiItems keeps size and nulls")
        void mapsItemList() {
            var pid = UUID.randomUUID().toString();
            var a = new OrderItem(pid, "SKU-A", "A", new BigDecimal("1.00"), 2);
            var list = Arrays.asList(a, null);

            var apiList = mapper.toApiItems(list);

            assertThat(apiList).hasSize(2);
            assertThat(apiList.get(0).getProductId().toString()).isEqualTo(pid);
            assertThat(apiList.get(1)).isNull();
        }
    }

    @Nested
    class ToCommandSearchCriteria {

        @Test @DisplayName("returns null when request is null")
        void returnsNullOnNullRequest() {
            assertThat(mapper.toCommand(null)).isNull();
        }

        @Test @DisplayName("maps filters (status/email/totals/dates) and pagination/sort")
        void mapsFiltersAndPaging() {
            var from = OffsetDateTime.parse("2025-02-01T00:00:00Z");
            var to   = OffsetDateTime.parse("2025-03-01T00:00:00Z");

            var filters = new OrderSearchCriteriaRequestFilters()
                    .status(OrderSearchCriteriaRequestFilters.StatusEnum.CONFIRMED)
                    .email("john.doe@example.com")
                    .minTotal(50.0)
                    .maxTotal(200.0)
                    .from(from)
                    .to(to);

            var req = new OrderSearchCriteriaRequest()
                    .page(2).size(25).sort("createdAt:asc")
                    .filters(filters);

            var cmd = mapper.toCommand(req);

            assertThat(cmd.page()).isEqualTo(2);
            assertThat(cmd.size()).isEqualTo(25);
            assertThat(cmd.sort()).isEqualTo("createdAt:asc");

            assertThat(cmd.status()).isEqualTo(OrderStatus.CONFIRMED);
            assertThat(cmd.customerEmail()).isEqualTo("john.doe@example.com");
            assertThat(cmd.minTotal()).isEqualByComparingTo(BigDecimal.valueOf(50.0));
            assertThat(cmd.maxTotal()).isEqualByComparingTo(BigDecimal.valueOf(200.0));
            assertThat(cmd.from()).isEqualTo(from.toInstant());
            assertThat(cmd.to()).isEqualTo(to.toInstant());
        }

        @Test @DisplayName("null filters -> all filter fields null")
        void nullFilters() {
            var req = new OrderSearchCriteriaRequest()
                    .page(0).size(10).sort(null)
                    .filters(null);

            var cmd = mapper.toCommand(req);

            assertThat(cmd.page()).isEqualTo(0);
            assertThat(cmd.size()).isEqualTo(10);
            assertThat(cmd.sort()).isNull();

            assertThat(cmd.status()).isNull();
            assertThat(cmd.customerEmail()).isNull();
            assertThat(cmd.minTotal()).isNull();
            assertThat(cmd.maxTotal()).isNull();
            assertThat(cmd.from()).isNull();
            assertThat(cmd.to()).isNull();
        }
    }

    @Nested
    class ToApiPageResult {

        @Test @DisplayName("returns null when pageResult is null")
        void nullPage() {
            OrderPage api = mapper.toApi((PageResult<com.jorgeandreu.orders.domain.model.Order>) null);
            assertThat(api).isNull();
        }

        @Test @DisplayName("maps metadata and content via @Named toApi")
        void mapsAll() {
            var created = Instant.parse("2025-04-01T12:00:00Z");

            var domainOrder = new com.jorgeandreu.orders.domain.model.Order(
                    UUID.randomUUID(),
                    OrderStatus.CANCELED,
                    "a@b.com",
                    List.of(),
                    new BigDecimal("0.00"),
                    created,
                    created,
                    7L
            );

            var page = new PageResult<>(List.of(domainOrder), 1, 5, 13, 3);

            var api = mapper.toApi(page);

            assertThat(api.getPage()).isEqualTo(1);
            assertThat(api.getSize()).isEqualTo(5);
            assertThat(api.getTotalElements()).isEqualTo(13);
            assertThat(api.getTotalPages()).isEqualTo(3);
            assertThat(api.getContent()).hasSize(1);
            var first = api.getContent().get(0);
            assertThat(first.getId()).isEqualTo(domainOrder.id());
            assertThat(first.getStatus()).isEqualTo(com.jorgeandreu.orders.infrastructure.api.model.Order.StatusEnum.CANCELED);
            assertThat(first.getCreatedAt()).isEqualTo(created.atOffset(ZoneOffset.UTC));
            assertThat(first.getVersion()).isEqualTo(7);
        }

        @Test @DisplayName("content == null -> keeps null")
        void nullContent() {
            var page = new PageResult<com.jorgeandreu.orders.domain.model.Order>(null, 0, 20, 0, 0);
            var api = mapper.toApi(page);
            assertThat(api.getContent()).isNull();
            assertThat(api.getPage()).isZero();
            assertThat(api.getSize()).isEqualTo(20);
        }

        @Test @DisplayName("content contains null item -> preserves null entry")
        void contentWithNull() {
            var created = Instant.parse("2025-05-01T00:00:00Z");
            var order = new com.jorgeandreu.orders.domain.model.Order(
                    UUID.randomUUID(), OrderStatus.PENDING, "x@y.com",
                    List.of(), BigDecimal.ZERO, created, created, 0L
            );
            var page = new PageResult<>(Arrays.asList(order, null), 0, 2, 2, 1);

            var api = mapper.toApi(page);

            assertThat(api.getContent()).hasSize(2);
            assertThat(api.getContent().get(0).getId()).isEqualTo(order.id());
            assertThat(api.getContent().get(1)).isNull();
        }
    }
}
