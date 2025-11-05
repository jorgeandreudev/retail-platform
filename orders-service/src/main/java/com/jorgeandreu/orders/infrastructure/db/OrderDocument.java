package com.jorgeandreu.orders.infrastructure.db;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.springframework.data.mongodb.core.mapping.FieldType.DECIMAL128;

@Document(collection = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDocument {

    @Id
    private String id;

    @Field("status")
    private String status;

    @Field("customer_email")
    private String customerEmail;

    @Field("items")
    private List<ItemDocument> items;

    @Field(targetType = DECIMAL128)
    private BigDecimal total;

    @Field("created_at")
    private Instant createdAt;

    @Field("updated_at")
    private Instant updatedAt;

    @Indexed(unique = true, sparse = true)
    @Field("idempotency_key")
    private String idempotencyKey;

    @Version
    private Long version;
}

