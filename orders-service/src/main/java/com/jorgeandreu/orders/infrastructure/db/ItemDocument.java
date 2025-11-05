package com.jorgeandreu.orders.infrastructure.db;

import lombok.*;

import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemDocument {

    @Field("product_id")
    private String productId;

    @Field("sku")
    private String sku;

    @Field("name")
    private String name;

    @Field("unit_price")
    private BigDecimal unitPrice;

    @Field("quantity")
    private Integer quantity;

    @Field("line_total")
    private BigDecimal lineTotal;
}

