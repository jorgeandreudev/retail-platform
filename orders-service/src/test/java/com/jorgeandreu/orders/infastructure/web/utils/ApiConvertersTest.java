package com.jorgeandreu.orders.infastructure.web.utils;

import com.jorgeandreu.orders.domain.model.OrderStatus;
import com.jorgeandreu.orders.infrastructure.api.model.Order;
import com.jorgeandreu.orders.infrastructure.web.utils.ApiConverters;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ApiConvertersTest {

    private final ApiConverters mapper = new ApiConverters() {};

    @Nested
    @DisplayName("Instant ↔ OffsetDateTime")
    class InstantOffsetMapping {

        @Test
        void mapOffsetDateTimeToInstant() {
            OffsetDateTime odt = OffsetDateTime.parse("2025-01-01T12:00:00Z");
            Instant result = mapper.map(odt);
            assertThat(result).isEqualTo(Instant.parse("2025-01-01T12:00:00Z"));
        }

        @Test
        void mapInstantToOffsetDateTime() {
            Instant instant = Instant.parse("2025-02-02T00:00:00Z");
            OffsetDateTime result = mapper.map(instant);
            assertThat(result).isEqualTo(OffsetDateTime.ofInstant(instant, ZoneOffset.UTC));
        }

        @Test
        void mapOffsetDateTimeToInstant_nullReturnsNull() {
            assertThat(mapper.map((OffsetDateTime) null)).isNull();
        }

        @Test
        void mapInstantToOffsetDateTime_nullReturnsNull() {
            assertThat(mapper.map((Instant) null)).isNull();
        }
    }

    @Nested
    @DisplayName("Double ↔ BigDecimal")
    class NumberMapping {

        @Test
        void mapDoubleToBigDecimal() {
            Double d = 123.45;
            BigDecimal result = mapper.map(d);
            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(123.45));
        }

        @Test
        void mapBigDecimalToDouble() {
            BigDecimal bd = new BigDecimal("456.78");
            Double result = mapper.map(bd);
            assertThat(result).isEqualTo(456.78);
        }

        @Test
        void mapDoubleToBigDecimal_nullReturnsNull() {
            assertThat(mapper.map((Double) null)).isNull();
        }

        @Test
        void mapBigDecimalToDouble_nullReturnsNull() {
            assertThat(mapper.map((BigDecimal) null)).isNull();
        }
    }

    @Nested
    @DisplayName("Optional ↔ value")
    class OptionalMapping {

        @Test
        void mapValueToOptional_present() {
            String value = "test";
            Optional<String> result = mapper.map(value);
            assertThat(result).contains("test");
        }

        @Test
        void mapValueToOptional_nullReturnsEmpty() {
            Optional<Object> result = mapper.map((Object) null);
            assertThat(result).isEmpty();
        }

        @Test
        void mapOptionalToValue_present() {
            Optional<String> opt = Optional.of("hello");
            String result = mapper.map(opt);
            assertThat(result).isEqualTo("hello");
        }

        @Test
        void mapOptionalToValue_emptyReturnsNull() {
            Optional<String> opt = Optional.empty();
            String result = mapper.map(opt);
            assertThat(result).isNull();
        }

        @Test
        void mapOptionalToValue_nullReturnsNull() {
            assertThat(mapper.map((Optional<String>) null)).isNull();
        }
    }

    @Nested
    @DisplayName("OrderStatus ↔ Order.StatusEnum")
    class EnumMapping {

        @Test
        void mapApiEnumToDomainEnum() {
            Order.StatusEnum apiEnum = Order.StatusEnum.PENDING;
            OrderStatus domain = mapper.map(apiEnum);
            assertThat(domain).isEqualTo(OrderStatus.PENDING);
        }

        @Test
        void mapDomainEnumToApiEnum() {
            OrderStatus domain = OrderStatus.CONFIRMED;
            Order.StatusEnum api = mapper.map(domain);
            assertThat(api).isEqualTo(Order.StatusEnum.CONFIRMED);
        }

        @Test
        void mapApiEnumToDomainEnum_nullReturnsNull() {
            assertThat(mapper.map((Order.StatusEnum) null)).isNull();
        }

        @Test
        void mapDomainEnumToApiEnum_nullReturnsNull() {
            assertThat(mapper.map((OrderStatus) null)).isNull();
        }
    }
}

