package com.jorgeandreu.orders.infastructure.config;

import com.jorgeandreu.orders.infrastructure.config.Decimal128ToBigDecimalConverter;
import org.bson.types.Decimal128;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Decimal128ToBigDecimalConverterTest {

    private final Decimal128ToBigDecimalConverter converter = new Decimal128ToBigDecimalConverter();

    @Test
    @DisplayName("converts simple decimal128 to BigDecimal")
    void convertsSimple() {
        Decimal128 in = new Decimal128(new BigDecimal("42.000"));
        BigDecimal out = converter.convert(in);

        assertThat(out).isEqualByComparingTo("42.000");
    }

    @Test
    @DisplayName("preserves sign and scale")
    void preservesSignAndScale() {
        Decimal128 in = new Decimal128(new BigDecimal("-0.0100"));
        BigDecimal out = converter.convert(in);

        assertThat(out).isEqualByComparingTo("-0.0100");
    }

    @Test
    @DisplayName("supports large exponents within Decimal128 range")
    void supportsLargeExponent() {
        Decimal128 in = new Decimal128(new BigDecimal("9.99E+100"));
        BigDecimal out = converter.convert(in);

        assertThat(out).isEqualByComparingTo("9.99E+100");
    }

    @Test
    @DisplayName("null input throws NullPointerException")
    void nullInputThrows() {
        assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(NullPointerException.class);
    }
}

