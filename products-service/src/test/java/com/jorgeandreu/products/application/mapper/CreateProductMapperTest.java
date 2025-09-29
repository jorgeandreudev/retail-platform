package com.jorgeandreu.products.application.mapper;

import com.jorgeandreu.products.domain.model.Product;
import com.jorgeandreu.products.domain.port.in.CreateProductCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CreateProductMapperTest {

    private CreateProductMapperImpl mapper;

    @BeforeEach
    void setUp() {
        mapper = new CreateProductMapperImpl();
    }

    @Test
    @DisplayName("returns null when command is null")
    void returnsNullWhenCmdIsNull() {
        Product p = mapper.toDomain(null, 7L);
        assertThat(p).isNull();
    }

    @Test
    @DisplayName("maps fields, sets version from initialVersion, generates id/createdAt/updatedAt and deletedAt=null")
    void mapsFieldsAndGeneratesMeta() {
        CreateProductCommand cmd = new CreateProductCommand(
                "ACME-123", "Laptop Pro", BigDecimal.valueOf(1299.99), 5, "laptops"
        );
        long initialVersion = 5L;
        Instant t0 = Instant.now();

        Product p = mapper.toDomain(cmd, initialVersion);
        Instant t1 = Instant.now();

        assertThat(p).isNotNull();
        assertThat(p.sku()).isEqualTo("ACME-123");
        assertThat(p.name()).isEqualTo("Laptop Pro");
        assertThat(p.price()).isEqualByComparingTo("1299.99");
        assertThat(p.stock()).isEqualTo(5);
        assertThat(p.category()).isEqualTo("laptops");
        assertThat(p.version()).isEqualTo(5L);
        assertThat(p.id()).isInstanceOf(UUID.class);
        assertThat(p.createdAt()).isBetween(t0, t1);
        assertThat(p.updatedAt()).isBetween(t0, t1);
        assertThat(!p.updatedAt().isBefore(p.createdAt())).isTrue();
        assertThat(p.deletedAt()).isNull();
    }

    @Test
    @DisplayName("generates a new id on each call (ids must differ)")
    void generatesDifferentIdsEachCall() {
        CreateProductCommand cmd = new CreateProductCommand(
                "ACME-001", "X", BigDecimal.ONE, 1, "cat"
        );
        Product p1 = mapper.toDomain(cmd, 1L);
        Product p2 = mapper.toDomain(cmd, 1L);

        assertThat(p1.id()).isNotNull();
        assertThat(p2.id()).isNotNull();
        assertThat(p1.id()).isNotEqualTo(p2.id());
    }

    @Test
    @DisplayName("handles nullable fields from command (e.g., category=null)")
    void handlesNullablesFromCommand() {
        CreateProductCommand cmd = new CreateProductCommand(
                "ACME-002", "Basic", BigDecimal.ZERO, 0, null
        );

        Product p = mapper.toDomain(cmd, 0L);

        assertThat(p.sku()).isEqualTo("ACME-002");
        assertThat(p.name()).isEqualTo("Basic");
        assertThat(p.price()).isZero();
        assertThat(p.stock()).isZero();
        assertThat(p.category()).isNull();
    }
}
