package com.jorgeandreu.products.infrastructure.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;


class TimeMapperTest {

    private final TimeMapper mapper = new TimeMapper();

    @Test
    @DisplayName("toOffsetDateTime returns null when input Instant is null")
    void toOffsetDateTime_null() {
        assertThat(mapper.toOffsetDateTime(null)).isNull();
    }

    @Test
    @DisplayName("toInstant returns null when input OffsetDateTime is null")
    void toInstant_null() {
        assertThat(mapper.toInstant(null)).isNull();
    }

    @Test
    @DisplayName("toOffsetDateTime maps Instant to UTC OffsetDateTime preserving the instant")
    void toOffsetDateTime_utc() {
        Instant instant = Instant.parse("2025-01-02T03:04:05Z");

        OffsetDateTime odt = mapper.toOffsetDateTime(instant);

        assertThat(odt).isNotNull();
        assertThat(odt.getOffset()).isEqualTo(ZoneOffset.UTC);
        // Same point in time
        assertThat(odt.toInstant()).isEqualTo(instant);
    }

    @Test
    @DisplayName("Round-trip: Instant -> OffsetDateTime(UTC) -> Instant yields the same Instant")
    void roundTrip_instant_to_odt_to_instant() {
        Instant original = Instant.ofEpochSecond(1_700_000_000L); // any fixed instant

        OffsetDateTime odt = mapper.toOffsetDateTime(original);
        Instant back = mapper.toInstant(odt);

        assertThat(back).isEqualTo(original);
    }

    @Test
    @DisplayName("Round-trip: OffsetDateTime(UTC) -> Instant -> OffsetDateTime(UTC) preserves value and offset")
    void roundTrip_odt_to_instant_to_odt() {
        OffsetDateTime original = OffsetDateTime.of(2025, 9, 29, 11, 19, 26, 0, ZoneOffset.UTC);

        Instant asInstant = mapper.toInstant(original);
        OffsetDateTime back = mapper.toOffsetDateTime(asInstant);

        assertThat(back).isEqualTo(original);
        assertThat(back.getOffset()).isEqualTo(ZoneOffset.UTC);
    }
}