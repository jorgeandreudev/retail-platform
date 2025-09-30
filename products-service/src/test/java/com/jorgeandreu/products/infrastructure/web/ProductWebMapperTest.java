package com.jorgeandreu.products.infrastructure.web;

import com.jorgeandreu.products.domain.model.PageResult;
import com.jorgeandreu.products.domain.model.Product;
import com.jorgeandreu.products.domain.port.in.CreateProductCommand;
import com.jorgeandreu.products.domain.port.in.SearchCriteriaCommand;
import com.jorgeandreu.products.infrastructure.api.model.CreateProductRequest;
import com.jorgeandreu.products.infrastructure.api.model.ProductPage;
import com.jorgeandreu.products.infrastructure.api.model.ProductSearchCriteriaRequest;
import com.jorgeandreu.products.infrastructure.api.model.ProductSearchCriteriaRequestFilters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProductWebMapperTest {

    private ProductWebMapperImpl mapper;

    private final TimeMapper timeMapperStub = new TimeMapper() {
    };

    @BeforeEach
    void setUp() {
        mapper = new ProductWebMapperImpl();
        ReflectionTestUtils.setField(mapper, "timeMapper", timeMapperStub);
    }

    @Nested
    class ToApiDomainProduct {

        @Test @DisplayName("returns null when domain is null")
        void nullDomain() {
            var api = mapper.toApi((Product) null);
            assertThat(api).isNull();
        }

        @Test @DisplayName("maps all fields including time conversion")
        void mapsAll() {
            var id = UUID.randomUUID();
            var created = Instant.parse("2025-01-01T10:15:30Z");
            var updated = Instant.parse("2025-01-02T11:16:31Z");
            var domain = new Product(
                    id, "ACME-1", "Laptop", BigDecimal.valueOf(999.99),
                    7, "laptops", created, updated, null, 0L
            );

            var api = mapper.toApi(domain);

            assertThat(api.getId()).isEqualTo(id);
            assertThat(api.getSku()).isEqualTo("ACME-1");
            assertThat(api.getName()).isEqualTo("Laptop");
            assertThat(api.getPrice()).isEqualTo(999.99);
            assertThat(api.getStock()).isEqualTo(7);
            assertThat(api.getCategory()).isEqualTo("laptops");
            assertThat(api.getCreatedAt()).isEqualTo(created.atOffset(ZoneOffset.UTC));
            assertThat(api.getUpdatedAt()).isEqualTo(updated.atOffset(ZoneOffset.UTC));
        }
    }

    @Nested
    class ToDtoDomainProduct {

        @Test @DisplayName("toDto: maps same as toApi")
        void mapsToDto() {
            var id = UUID.randomUUID();
            var created = Instant.parse("2025-03-01T00:00:00Z");
            var domain = new Product(
                    id, "ACME-2", "Ultra", BigDecimal.valueOf(1234.56),
                    3, "laptops", created, created, null, 0L
            );

            var dto = mapper.toDto(domain);

            assertThat(dto.getId()).isEqualTo(id);
            assertThat(dto.getSku()).isEqualTo("ACME-2");
            assertThat(dto.getName()).isEqualTo("Ultra");
            assertThat(dto.getPrice()).isEqualTo(1234.56);
            assertThat(dto.getStock()).isEqualTo(3);
            assertThat(dto.getCategory()).isEqualTo("laptops");
            assertThat(dto.getCreatedAt()).isEqualTo(created.atOffset(ZoneOffset.UTC));
            assertThat(dto.getUpdatedAt()).isEqualTo(created.atOffset(ZoneOffset.UTC));
        }

        @Test @DisplayName("toDto: returns null when domain is null")
        void toDtoNull() {
            assertThat(mapper.toDto(null)).isNull();
        }
    }

    @Nested
    class ToCommandCreateRequest {

        @Test @DisplayName("returns null when request is null")
        void nullRequest() {
            CreateProductCommand cmd = mapper.toCommand((CreateProductRequest) null);
            assertThat(cmd).isNull();
        }

        @Test @DisplayName("maps all fields and converts double price -> BigDecimal")
        void mapsAll() {
            var req = new CreateProductRequest()
                    .sku("ACME-9").name("Ultra")
                    .price(1234.56)
                    .stock(3)
                    .category("laptops");

            var cmd = mapper.toCommand(req);

            assertThat(cmd.sku()).isEqualTo("ACME-9");
            assertThat(cmd.name()).isEqualTo("Ultra");
            assertThat(cmd.price()).isEqualByComparingTo(BigDecimal.valueOf(1234.56));
            assertThat(cmd.stock()).isEqualTo(3);
            assertThat(cmd.category()).isEqualTo("laptops");
        }

        @Test @DisplayName("handles null price (cmd.price == null)")
        void nullPrice() {
            var req = new CreateProductRequest()
                    .sku("ACME-10").name("Basic")
                    .stock(1).category("laptops");

            var cmd = mapper.toCommand(req);

            assertThat(cmd.price()).isNull();
        }
    }

    @Nested
    class ProductSearchCriteriaToCommand {

        @Test @DisplayName("returns null when request is null")
        void nullRequest() {
            assertThat(mapper.productSearchCriteriaToSearchCriteria(null)).isNull();
        }

        @Test @DisplayName("maps filters and flags from nested object")
        void mapsFiltersAndFlags() {
            var filters = new ProductSearchCriteriaRequestFilters()
                    .category("laptops").minPrice(500.0).maxPrice(2000.0).text("pro");
            var req = new ProductSearchCriteriaRequest()
                    .page(1).size(50).sort("createdAt,desc")
                    .includeDeleted(true)
                    .filters(filters);

            SearchCriteriaCommand cmd = mapper.productSearchCriteriaToSearchCriteria(req);

            assertThat(cmd.page()).isEqualTo(1);
            assertThat(cmd.size()).isEqualTo(50);
            assertThat(cmd.sort()).isEqualTo("createdAt,desc");
            assertThat(cmd.category()).isEqualTo("laptops");
            assertThat(cmd.minPrice()).isEqualTo(500.0);
            assertThat(cmd.maxPrice()).isEqualTo(2000.0);
            assertThat(cmd.text()).isEqualTo("pro");
            assertThat(cmd.includeDeleted()).isTrue();
        }

        @Test @DisplayName("filters == null -> all filter fields are null")
        void nullFilters() {
            var req = new ProductSearchCriteriaRequest()
                    .page(0).size(10).sort(null)
                    .includeDeleted(false);

            SearchCriteriaCommand cmd = mapper.productSearchCriteriaToSearchCriteria(req);

            assertThat(cmd.category()).isNull();
            assertThat(cmd.minPrice()).isNull();
            assertThat(cmd.maxPrice()).isNull();
            assertThat(cmd.text()).isNull();
            assertThat(cmd.includeDeleted()).isFalse();
        }

        @Test @DisplayName("individual filter fields null are propagated as null")
        void individualFilterNulls() {
            var filters = new ProductSearchCriteriaRequestFilters()
                    .category(null).minPrice(null).maxPrice(null).text(null);
            var req = new ProductSearchCriteriaRequest()
                    .page(null).size(null).sort(null)
                    .includeDeleted(null)
                    .filters(filters);

            SearchCriteriaCommand cmd = mapper.productSearchCriteriaToSearchCriteria(req);

            assertThat(cmd.page()).isZero();
            assertThat(cmd.size()).isZero();
            assertThat(cmd.sort()).isNull();
            assertThat(cmd.includeDeleted()).isFalse();
            assertThat(cmd.category()).isNull();
            assertThat(cmd.minPrice()).isNull();
            assertThat(cmd.maxPrice()).isNull();
            assertThat(cmd.text()).isNull();
        }
    }

    @Nested
    class ToApiPageResult {

        @Test @DisplayName("returns null when pageResult is null")
        void nullPage() {
            ProductPage api = mapper.toApi((PageResult<Product>) null);
            assertThat(api).isNull();
        }

        @Test @DisplayName("maps metadata and non-null content items")
        void mapsMetadataAndContent() {
            var created = Instant.parse("2025-01-01T00:00:00Z");
            var p = new Product(
                    UUID.randomUUID(), "ACME-1", "Lap",
                    BigDecimal.valueOf(999.99), 5, "laptops",
                    created, created, null, 0L
            );
            var page = new PageResult<>(List.of(p), 2, 10, 23, 3);

            ProductPage api = mapper.toApi(page);

            assertThat(api.getPage()).isEqualTo(2);
            assertThat(api.getSize()).isEqualTo(10);
            assertThat(api.getTotalElements()).isEqualTo(23);
            assertThat(api.getTotalPages()).isEqualTo(3);
            assertThat(api.getContent()).hasSize(1);
            assertThat(api.getContent().get(0).getId()).isEqualTo(p.id());
            assertThat(api.getContent().get(0).getCreatedAt()).isEqualTo(created.atOffset(ZoneOffset.UTC));
        }

        @Test @DisplayName("content == null -> ProductPage.content == null")
        void nullContentList() {
            var page = new PageResult<Product>(null, 0, 20, 0, 0);

            ProductPage api = mapper.toApi(page);

            assertThat(api.getContent()).isNull();
            assertThat(api.getPage()).isZero();
            assertThat(api.getSize()).isEqualTo(20);
        }

        @Test @DisplayName("content contains null items -> keeps null entries after mapping")
        void contentWithNullItem() {
            var created = Instant.parse("2025-01-01T00:00:00Z");
            var p = new Product(
                    UUID.randomUUID(), "ACME-2", "Ultra",
                    BigDecimal.TEN, 1, "laptops",
                    created, created, null, 0L
            );
            var page = new PageResult<>(Arrays.asList(p, null), 0, 2, 2, 1);

            ProductPage api = mapper.toApi(page);

            assertThat(api.getContent()).hasSize(2);
            assertThat(api.getContent().get(0).getId()).isEqualTo(p.id());
            assertThat(api.getContent().get(1)).isNull();
        }
    }

    @Nested
    @DisplayName("toCommand(UpdateProductRequest)")
    class ToCommandUpdateRequest {

        @Test
        @DisplayName("maps all fields (double → BigDecimal, version preserved)")
        void mapsAllFields() {
            var req = new com.jorgeandreu.products.infrastructure.api.model.UpdateProductRequest()
                    .sku("ACME-123")
                    .name("Gaming Laptop")
                    .price(1499.99)
                    .stock(7)
                    .category("laptops")
                    .version(3);

            var cmd = mapper.toCommand(req);

            assertThat(cmd).isNotNull();
            assertThat(cmd.sku()).isEqualTo("ACME-123");
            assertThat(cmd.name()).isEqualTo("Gaming Laptop");
            assertThat(cmd.price()).isEqualByComparingTo(BigDecimal.valueOf(1499.99));
            assertThat(cmd.stock()).isEqualTo(7);
            assertThat(cmd.category()).isEqualTo("laptops");
            assertThat(cmd.version()).isEqualTo(3L);
        }

        @Test
        @DisplayName("null version → defaults to 0 (price provided)")
        void nullVersionDefaultsToZero() {
            var req = new com.jorgeandreu.products.infrastructure.api.model.UpdateProductRequest()
                    .sku("ACME-456")
                    .name("Ultrabook")
                    .price(999.0)
                    .stock(5)
                    .category("laptops");

            var cmd = mapper.toCommand(req);

            assertThat(cmd).isNotNull();
            assertThat(cmd.version()).isZero();
            assertThat(cmd.price()).isEqualByComparingTo(BigDecimal.valueOf(999.0));
            assertThat(cmd.stock()).isEqualTo(5);
        }

        @Test
        @DisplayName("throws when price is null")
        void throwsWhenPriceIsNull() {
            var req = new com.jorgeandreu.products.infrastructure.api.model.UpdateProductRequest()
                    .sku("ACME-789")
                    .name("Pro")
                    .stock(1)
                    .category("laptops")
                    .version(1);

            org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> mapper.toCommand(req));
        }

        @Test
        @DisplayName("throws when price is negative")
        void throwsWhenPriceIsNegative() {
            var req = new com.jorgeandreu.products.infrastructure.api.model.UpdateProductRequest()
                    .sku("ACME-000")
                    .name("Bad")
                    .price(-1.0)
                    .stock(1)
                    .category("laptops")
                    .version(1);

            org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> mapper.toCommand(req));
        }

        @Test
        @DisplayName("throws when stock is negative")
        void throwsWhenStockIsNegative() {
            var req = new com.jorgeandreu.products.infrastructure.api.model.UpdateProductRequest()
                    .sku("ACME-001")
                    .name("Bad Stock")
                    .price(10.0)
                    .stock(-5)
                    .category("laptops")
                    .version(1);

            org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> mapper.toCommand(req));
        }

        @Test
        @DisplayName("returns null when request is null")
        void returnsNullWhenRequestIsNull() {
            var cmd = mapper.toCommand((com.jorgeandreu.products.infrastructure.api.model.UpdateProductRequest) null);
            assertThat(cmd).isNull();
        }
    }

}
