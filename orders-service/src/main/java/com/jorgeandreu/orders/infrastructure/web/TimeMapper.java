package com.jorgeandreu.orders.infrastructure.web;

import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
public class TimeMapper {

    @Named("instantToOffset")
    public OffsetDateTime instantToOffset(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }
}