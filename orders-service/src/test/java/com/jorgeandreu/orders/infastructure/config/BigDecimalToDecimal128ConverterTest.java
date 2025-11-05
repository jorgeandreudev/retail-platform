package com.jorgeandreu.orders.infastructure.config;

import com.jorgeandreu.orders.infrastructure.config.BigDecimalToDecimal128Converter;
import org.bson.types.Decimal128;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BigDecimalToDecimal128ConverterTest {

    private final BigDecimalToDecimal128Converter converter = new BigDecimalToDecimal128Converter();

    @Test
    @DisplayName("converts simple positive value")
    void convertsSimplePositive() {
        BigDecimal in = new BigDecimal("123.45");
        Decimal128 out = converter.convert(in);

        assertThat(out).isNotNull();
        assertThat(out.bigDecimalValue()).isEqualByComparingTo(in);
    }

    @Test
    @DisplayName("preserves scale and sign for negative values")
    void preservesScaleAndSign() {
        BigDecimal in = new BigDecimal("-0.0001000");
        Decimal128 out = converter.convert(in);

        assertThat(out.bigDecimalValue()).isEqualByComparingTo("-0.0001000");
    }

    @Test
    @DisplayName("supports big integer magnitudes within Decimal128 range")
    void supportsBigMagnitudes() {
        BigDecimal in = new BigDecimal("1E100");
        Decimal128 out = converter.convert(in);

        assertThat(out.bigDecimalValue()).isEqualByComparingTo(in);
    }

    @Test
    @DisplayName("null input throws NullPointerException")
    void nullInputThrows() {
        assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(NullPointerException.class);
    }
}

