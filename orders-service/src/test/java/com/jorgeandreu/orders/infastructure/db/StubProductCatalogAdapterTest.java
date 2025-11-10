package com.jorgeandreu.orders.infastructure.db;

import com.jorgeandreu.orders.domain.model.ProductSnapshot;
import com.jorgeandreu.orders.domain.port.out.ProductCatalogPort;
import com.jorgeandreu.orders.infrastructure.db.StubProductCatalogAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StubProductCatalogAdapterTest {

    private final ProductCatalogPort adapter = new StubProductCatalogAdapter();

    @Test
    @DisplayName("returns a deterministic snapshot from UUID string")
    void returnsDeterministicSnapshot() {
        String uuid = "3fa85f64-5717-4562-b3fc-2c963f66afa6";

        ProductSnapshot snap = adapter.findSnapshotById(uuid);

        assertThat(snap).isNotNull();
        assertThat(snap.productId()).isEqualTo(UUID.fromString(uuid));
        assertThat(snap.sku()).isEqualTo("SKU-3fa85f");
        assertThat(snap.name()).isEqualTo("Product 3fa85f");
        assertThat(snap.price()).isEqualByComparingTo(new BigDecimal("49.99"));
    }

    @Test
    @DisplayName("different UUIDs yield different SKU/Name while keeping static price")
    void differentUuidsProduceDifferentDerivedFields() {
        String u1 = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
        String u2 = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb";

        ProductSnapshot s1 = adapter.findSnapshotById(u1);
        ProductSnapshot s2 = adapter.findSnapshotById(u2);

        assertThat(s1.sku()).isEqualTo("SKU-aaaaaa");
        assertThat(s2.sku()).isEqualTo("SKU-bbbbbb");
        assertThat(s1.name()).isEqualTo("Product aaaaaa");
        assertThat(s2.name()).isEqualTo("Product bbbbbb");
        assertThat(s1.price()).isEqualByComparingTo("49.99");
        assertThat(s2.price()).isEqualByComparingTo("49.99");
        assertThat(s1).isNotEqualTo(s2);
    }

    @Test
    @DisplayName("invalid UUID string -> IllegalArgumentException from UUID.fromString")
    void invalidUuidThrows() {
        assertThatThrownBy(() -> adapter.findSnapshotById("not-a-uuid"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

