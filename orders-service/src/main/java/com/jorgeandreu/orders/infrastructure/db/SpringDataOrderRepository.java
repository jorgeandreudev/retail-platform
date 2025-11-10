package com.jorgeandreu.orders.infrastructure.db;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataOrderRepository extends MongoRepository<OrderDocument, String> {

    boolean existsByIdempotencyKey(String idempotencyKey);
}