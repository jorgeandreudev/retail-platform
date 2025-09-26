package com.jorgeandreu.products.infrastructure.web;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;


@Component
public class TimeMapper {
    public OffsetDateTime toOffsetDateTime(Instant i) {
        return i == null ? null : i.atOffset(ZoneOffset.UTC);
    }
    public Instant toInstant(OffsetDateTime odt) {
        return odt == null ? null : odt.toInstant();
    }
}
