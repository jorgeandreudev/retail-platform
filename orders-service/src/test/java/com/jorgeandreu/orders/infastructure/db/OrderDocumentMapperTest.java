package com.jorgeandreu.orders.infastructure.db;

import com.jorgeandreu.orders.domain.model.Order;
import com.jorgeandreu.orders.domain.model.OrderItem;
import com.jorgeandreu.orders.domain.model.OrderStatus;
import com.jorgeandreu.orders.infrastructure.db.ItemDocument;
import com.jorgeandreu.orders.infrastructure.db.OrderDocument;
import com.jorgeandreu.orders.infrastructure.db.mapper.OrderDocumentMapper;
import com.jorgeandreu.orders.infrastructure.db.mapper.OrderDocumentMapperImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderDocumentMapperTest {

    private OrderDocumentMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new OrderDocumentMapperImpl();
    }

    @Nested
    @DisplayName("toDocument(Order)")
    class ToDocument {

        @Test
        @DisplayName("returns null when domain is null")
        void nullDomain() {
            assertThat(mapper.toDocument(null)).isNull();
        }

        @Test
        @DisplayName("maps all scalar fields and converts UUID→String and enum→name")
        void mapsAllFields() {
            UUID id = UUID.randomUUID();
            Instant created = Instant.parse("2025-01-01T00:00:00Z");
            Instant updated = Instant.parse("2025-01-02T00:00:00Z");

            var items = List.of(new OrderItem("pid-1", "SKU-1", "Name 1", new BigDecimal("10.50"), 2));
            var total = new BigDecimal("21.00");
            var domain = new Order(id, OrderStatus.PENDING, "john@doe.com", items, total, created, updated, 3L);

            OrderDocument doc = mapper.toDocument(domain);

            assertThat(doc.getId()).isEqualTo(id.toString());
            assertThat(doc.getStatus()).isEqualTo(OrderStatus.PENDING.name());
            assertThat(doc.getCustomerEmail()).isEqualTo("john@doe.com");
            assertThat(doc.getTotal()).isEqualByComparingTo("21.00");
            assertThat(doc.getCreatedAt()).isEqualTo(created);
            assertThat(doc.getUpdatedAt()).isEqualTo(updated);
            assertThat(doc.getVersion()).isEqualTo(3L);
            assertThat(doc.getItems()).hasSize(1);
        }

        @Test
        @DisplayName("id null -> id document null")
        void nullId() {
            var domain = new Order(null, OrderStatus.PENDING, "a@b.com",
                    List.of(), BigDecimal.ZERO, Instant.EPOCH, Instant.EPOCH, 0L);

            OrderDocument doc = mapper.toDocument(domain);

            assertThat(doc.getId()).isNull();
        }
    }

    @Nested
    @DisplayName("toItemDocument(OrderItem)")
    class ToItemDocument {

        @Test
        @DisplayName("returns null when item is null")
        void nullItem() {
            assertThat(mapper.toItemDocument(null)).isNull();
        }

        @Test
        @DisplayName("maps all fields and copies lineTotal from domain")
        void mapsAll() {
            var item = new OrderItem("p-1", "SKU-9", "Nice", new BigDecimal("7.25"), 4);
            ItemDocument doc = mapper.toItemDocument(item);

            assertThat(doc.getProductId()).isEqualTo("p-1");
            assertThat(doc.getSku()).isEqualTo("SKU-9");
            assertThat(doc.getName()).isEqualTo("Nice");
            assertThat(doc.getUnitPrice()).isEqualByComparingTo("7.25");
            assertThat(doc.getQuantity()).isEqualTo(4);
            assertThat(doc.getLineTotal()).isEqualByComparingTo(new BigDecimal("29.00"));
        }
    }

    @Nested
    @DisplayName("toItemDocuments(List<OrderItem>)")
    class ToItemDocuments {

        @Test
        @DisplayName("returns null when list is null")
        void nullList() {
            assertThat(mapper.toItemDocuments(null)).isNull();
        }

        @Test
        @DisplayName("maps each element")
        void mapsList() {
            var list = List.of(
                    new OrderItem("p1", "S1", "N1", new BigDecimal("1.00"), 1),
                    new OrderItem("p2", "S2", "N2", new BigDecimal("2.00"), 3)
            );
            var docs = mapper.toItemDocuments(list);

            assertThat(docs).hasSize(2);
            assertThat(docs.get(0).getSku()).isEqualTo("S1");
            assertThat(docs.get(1).getSku()).isEqualTo("S2");
        }
    }

    @Nested
    @DisplayName("toDomain(OrderDocument)")
    class ToDomain {

        @Test
        @DisplayName("returns null when document is null")
        void nullDoc() {
            assertThat(mapper.toDomain(null)).isNull();
        }

        @Test
        @DisplayName("maps all scalar fields and converts String→UUID and name→enum")
        void mapsAllFields() {
            UUID id = UUID.randomUUID();
            Instant created = Instant.parse("2025-04-01T10:00:00Z");
            Instant updated = Instant.parse("2025-04-01T12:00:00Z");
            var itemDoc = ItemDocument.builder()
                    .productId("p-xyz")
                    .sku("SKU-X")
                    .name("Thing")
                    .unitPrice(new BigDecimal("5.00"))
                    .quantity(2)
                    .lineTotal(new BigDecimal("10.00"))
                    .build();

            var doc = OrderDocument.builder()
                    .id(id.toString())
                    .status(OrderStatus.CONFIRMED.name())
                    .customerEmail("foo@bar.com")
                    .items(List.of(itemDoc))
                    .total(new BigDecimal("10.00"))
                    .createdAt(created)
                    .updatedAt(updated)
                    .version(7L)
                    .build();

            Order domain = mapper.toDomain(doc);

            assertThat(domain.id()).isEqualTo(id);
            assertThat(domain.status()).isEqualTo(OrderStatus.CONFIRMED);
            assertThat(domain.customerEmail()).isEqualTo("foo@bar.com");
            assertThat(domain.total()).isEqualByComparingTo("10.00");
            assertThat(domain.createdAt()).isEqualTo(created);
            assertThat(domain.updatedAt()).isEqualTo(updated);
            assertThat(domain.version()).isEqualTo(7L);
            assertThat(domain.items()).hasSize(1);
            assertThat(domain.items().get(0).sku()).isEqualTo("SKU-X");
        }

        @Test
        @DisplayName("doc.id null -> domain.id null")
        void nullId() {
            var doc = OrderDocument.builder()
                    .id(null)
                    .status(OrderStatus.PENDING.name())
                    .customerEmail("a@b.com")
                    .items(List.of())
                    .total(BigDecimal.ZERO)
                    .createdAt(Instant.EPOCH)
                    .updatedAt(Instant.EPOCH)
                    .version(0L)
                    .build();

            var domain = mapper.toDomain(doc);
            assertThat(domain.id()).isNull();
        }
    }

    @Nested
    @DisplayName("toDomainItem(ItemDocument)")
    class ToDomainItem {

        @Test
        @DisplayName("returns null when doc is null")
        void nullItemDoc() {
            assertThat(mapper.toDomainItem(null)).isNull();
        }

        @Test
        @DisplayName("maps selected fields and ignores lineTotal (by design)")
        void mapsSelectedFields() {
            var doc = ItemDocument.builder()
                    .productId("p-1")
                    .sku("SKU-1")
                    .name("Name 1")
                    .unitPrice(new BigDecimal("2.50"))
                    .quantity(3)
                    .lineTotal(new BigDecimal("7.50"))
                    .build();

            var item = mapper.toDomainItem(doc);

            assertThat(item.productId()).isEqualTo("p-1");
            assertThat(item.sku()).isEqualTo("SKU-1");
            assertThat(item.name()).isEqualTo("Name 1");
            assertThat(item.unitPrice()).isEqualByComparingTo("2.50");
            assertThat(item.quantity()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("toDomainItems(List<ItemDocument>)")
    class ToDomainItems {

        @Test
        @DisplayName("returns null when list is null")
        void nullList() {
            assertThat(mapper.toDomainItems(null)).isNull();
        }

        @Test
        @DisplayName("maps each element")
        void mapsList() {
            var docs = List.of(
                    ItemDocument.builder().productId("p1").sku("S1").name("N1").unitPrice(new BigDecimal("1.00")).quantity(1).build(),
                    ItemDocument.builder().productId("p2").sku("S2").name("N2").unitPrice(new BigDecimal("3.00")).quantity(2).build()
            );

            var items = mapper.toDomainItems(docs);

            assertThat(items).hasSize(2);
            assertThat(items.get(0).sku()).isEqualTo("S1");
            assertThat(items.get(1).sku()).isEqualTo("S2");
        }
    }

    @Test
    @DisplayName("round-trip Order -> OrderDocument -> Order preserva campos clave")
    void roundTrip() {
        UUID id = UUID.randomUUID();
        var items = List.of(
                new OrderItem("pA", "SA", "NA", new BigDecimal("4.00"), 5),
                new OrderItem("pB", "SB", "NB", new BigDecimal("1.50"), 2)
        );
        var total = new BigDecimal("23.00");
        var created = Instant.parse("2025-02-01T00:00:00Z");
        var updated = Instant.parse("2025-02-01T01:00:00Z");
        var v = 11L;

        var original = new Order(id, OrderStatus.PENDING, "x@y.com", items, total, created, updated, v);

        OrderDocument doc = mapper.toDocument(original);
        Order back = mapper.toDomain(doc);

        assertThat(back.id()).isEqualTo(id);
        assertThat(back.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(back.customerEmail()).isEqualTo("x@y.com");
        assertThat(back.total()).isEqualByComparingTo(total);
        assertThat(back.createdAt()).isEqualTo(created);
        assertThat(back.updatedAt()).isEqualTo(updated);
        assertThat(back.version()).isEqualTo(v);
        assertThat(back.items()).hasSize(2);
        assertThat(back.items().get(0).sku()).isEqualTo("SA");
        assertThat(back.items().get(1).sku()).isEqualTo("SB");
    }
}
