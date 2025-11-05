package com.jorgeandreu.orders.infastructure.products;

import com.jorgeandreu.orders.application.exception.ProductNotFoundInOrderException;
import com.jorgeandreu.orders.domain.model.ProductSnapshot;
import com.jorgeandreu.orders.infrastructure.products.FeignProductCatalogAdapter;
import com.jorgeandreu.orders.infrastructure.products.ProductDetailsDto;
import com.jorgeandreu.orders.infrastructure.products.ProductsFeignClient;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class FeignProductCatalogAdapterTest {

    private final ProductsFeignClient client = mock(ProductsFeignClient.class);
    private final FeignProductCatalogAdapter adapter = new FeignProductCatalogAdapter(client);

    @Test
    @DisplayName("findSnapshotById - maps Feign DTO to ProductSnapshot")
    void findSnapshotById_mapsDto() {
        UUID id = UUID.randomUUID();
        String idStr = id.toString();

        var dto = new ProductDetailsDto(
                idStr,
                "SKU-123456",
                "Gaming Laptop",
                new BigDecimal("1499.99"),
                "Electronics"
        );

        given(client.getById(idStr)).willReturn(dto);

        ProductSnapshot snapshot = adapter.findSnapshotById(idStr);

        assertThat(snapshot).isNotNull();
        assertThat(snapshot.productId()).isEqualTo(id);
        assertThat(snapshot.sku()).isEqualTo("SKU-123456");
        assertThat(snapshot.name()).isEqualTo("Gaming Laptop");
        assertThat(snapshot.price()).isEqualByComparingTo(new BigDecimal("1499.99"));
    }

    @Test
    @DisplayName("findSnapshotById - Feign 404 -> ProductNotFoundInOrderException")
    void findSnapshotById_notFound_translatesToDomainException() {

        UUID id = UUID.randomUUID();
        String idStr = id.toString();

        Request req = Request.create(
                Request.HttpMethod.GET,
                "http://products-service/products/" + idStr,
                Map.of(), null, StandardCharsets.UTF_8, null
        );
        byte[] body = new byte[0];
        Map<String, Collection<String>> headers = Map.of();

        FeignException.NotFound notFound =
                new FeignException.NotFound("404 from products-service", req, body, headers);

        given(client.getById(idStr)).willThrow(notFound);

        assertThatThrownBy(() -> adapter.findSnapshotById(idStr))
                .isInstanceOf(ProductNotFoundInOrderException.class)
                .hasMessageContaining(idStr);
    }

    @Test
    @DisplayName("findSnapshotById - other Feign errors propagate")
    void findSnapshotById_otherFeignErrors_propagate() {
        String idStr = UUID.randomUUID().toString();

        Request req = Request.create(
                Request.HttpMethod.GET,
                "http://products-service/products/" + idStr,
                Map.of(), null, StandardCharsets.UTF_8, null
        );
        byte[] body = "forbidden".getBytes(StandardCharsets.UTF_8);
        Map<String, Collection<String>> headers = Map.of();

        FeignException.Forbidden forbidden =
                new FeignException.Forbidden("403 from products-service", req, body, headers);

        given(client.getById(idStr)).willThrow(forbidden);

        assertThatThrownBy(() -> adapter.findSnapshotById(idStr))
                .isInstanceOf(FeignException.Forbidden.class)
                .hasMessageContaining("403");
    }
}

