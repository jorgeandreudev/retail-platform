package com.jorgeandreu.products.infrastructure.db.mapper;

import com.jorgeandreu.products.domain.model.PageResult;
import com.jorgeandreu.products.domain.model.Product;
import com.jorgeandreu.products.infrastructure.db.ProductEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProductEntityMapperTest {

    private ProductEntityMapperImpl mapper;

    @BeforeEach
    void setUp() {
        mapper = new ProductEntityMapperImpl();
    }

    @Nested
    class ToDomainEntity {

        @Test
        @DisplayName("returns null when entity is null")
        void nullEntity() {
            assertThat(mapper.toDomain((ProductEntity) null)).isNull();
        }

        @Test
        @DisplayName("maps all fields from entity to domain")
        void mapsAllFields() {
            UUID id = UUID.randomUUID();
            Instant now = Instant.now();

            ProductEntity entity = ProductEntity.builder()
                    .id(id)
                    .sku("ACME-1")
                    .name("Laptop")
                    .price(BigDecimal.valueOf(999.99))
                    .stock(10)
                    .category("laptops")
                    .createdAt(now)
                    .updatedAt(now)
                    .deletedAt(null)
                    .version(5L)
                    .build();

            Product domain = mapper.toDomain(entity);

            assertThat(domain.id()).isEqualTo(id);
            assertThat(domain.sku()).isEqualTo("ACME-1");
            assertThat(domain.name()).isEqualTo("Laptop");
            assertThat(domain.price()).isEqualByComparingTo("999.99");
            assertThat(domain.stock()).isEqualTo(10);
            assertThat(domain.category()).isEqualTo("laptops");
            assertThat(domain.createdAt()).isEqualTo(now);
            assertThat(domain.updatedAt()).isEqualTo(now);
            assertThat(domain.deletedAt()).isNull();
            assertThat(domain.version()).isEqualTo(5L);
        }
    }

    @Nested
    class ToEntityDomain {

        @Test
        @DisplayName("returns null when domain is null")
        void nullDomain() {
            assertThat(mapper.toEntity(null)).isNull();
        }

        @Test
        @DisplayName("maps fields from domain to entity, ignoring id and timestamps, forcing version=0")
        void mapsAllFields() {
            Product domain = new Product(
                    UUID.randomUUID(),
                    "ACME-2",
                    "Ultra Laptop",
                    BigDecimal.valueOf(1299.99),
                    3,
                    "computers",
                    Instant.now(),
                    Instant.now(),
                    null,
                    7L
            );

            ProductEntity entity = mapper.toEntity(domain);

            assertThat(entity.getId()).isNull(); // ignored
            assertThat(entity.getSku()).isEqualTo("ACME-2");
            assertThat(entity.getName()).isEqualTo("Ultra Laptop");
            assertThat(entity.getPrice()).isEqualByComparingTo("1299.99");
            assertThat(entity.getStock()).isEqualTo(3);
            assertThat(entity.getCategory()).isEqualTo("computers");
            assertThat(entity.getCreatedAt()).isNull(); // ignored
            assertThat(entity.getUpdatedAt()).isNull(); // ignored
            assertThat(entity.getDeletedAt()).isNull(); // ignored
            assertThat(entity.getVersion()).isZero(); // forced
        }
    }

    @Nested
    class ToDomainPage {

        @Test
        @DisplayName("returns null when page is null")
        void nullPage() {
            assertThat(mapper.toDomain((Page<ProductEntity>) null)).isNull();
        }

        @Test
        @DisplayName("maps empty page (hasContent=false) -> content=null, keeps metadata")
        void emptyPage() {
            Page<ProductEntity> page = Page.empty(PageRequest.of(0, 5));

            PageResult<Product> result = mapper.toDomain(page);

            assertThat(result.content()).isNull(); // impl sets null when no content
            assertThat(result.page()).isZero();
            assertThat(result.size()).isEqualTo(5);
            assertThat(result.totalElements()).isZero();
            assertThat(result.totalPages()).isZero();
        }

        @Test
        @DisplayName("maps non-empty page with entities")
        void mapsNonEmptyPage() {
            UUID id = UUID.randomUUID();
            Instant now = Instant.now();

            ProductEntity entity = ProductEntity.builder()
                    .id(id)
                    .sku("ACME-3")
                    .name("Gaming Laptop")
                    .price(BigDecimal.valueOf(1500))
                    .stock(8)
                    .category("gaming")
                    .createdAt(now)
                    .updatedAt(now)
                    .deletedAt(null)
                    .version(2L)
                    .build();

            Page<ProductEntity> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);

            PageResult<Product> result = mapper.toDomain(page);

            assertThat(result.page()).isZero(); // impl hardcodes page=0
            assertThat(result.size()).isEqualTo(10);
            assertThat(result.totalElements()).isEqualTo(1);
            assertThat(result.totalPages()).isEqualTo(1);
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().getFirst().id()).isEqualTo(id);
            assertThat(result.content().getFirst().sku()).isEqualTo("ACME-3");
        }

        @Test
        @DisplayName("maps page with null entity in content -> keeps null in list")
        void mapsPageWithNullEntity() {
            Page<ProductEntity> page = new PageImpl<>(Collections.singletonList(null), PageRequest.of(0, 1), 1);

            PageResult<Product> result = mapper.toDomain(page);

            assertThat(result.content()).hasSize(1);
            assertThat(result.content().getFirst()).isNull();
        }
    }
}
