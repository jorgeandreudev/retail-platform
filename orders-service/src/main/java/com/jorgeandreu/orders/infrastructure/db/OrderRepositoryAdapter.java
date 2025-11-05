package com.jorgeandreu.orders.infrastructure.db;

import com.jorgeandreu.orders.application.exception.OrderSearchBadRequest;
import com.jorgeandreu.orders.domain.model.Order;
import com.jorgeandreu.orders.domain.model.OrderStatus;
import com.jorgeandreu.orders.domain.model.PageResult;
import com.jorgeandreu.orders.domain.model.SearchCriteria;
import com.jorgeandreu.orders.domain.port.out.OrderRepositoryPort;
import com.jorgeandreu.orders.infrastructure.db.mapper.OrderDocumentMapper;
import com.mongodb.client.result.UpdateResult;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class OrderRepositoryAdapter implements OrderRepositoryPort {

    private final SpringDataOrderRepository repository;
    private final OrderDocumentMapper mapper;
    private final MongoTemplate mongoTemplate;

    private static final int MAX_PAGE_SIZE = 200;

    public OrderRepositoryAdapter(SpringDataOrderRepository repository, OrderDocumentMapper mapper, MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.mapper = mapper;
        this.mongoTemplate = mongoTemplate;
    }


    @Override
    public Order save(Order order) {
        var toSave = mapper.toDocument(order);
        var saved  = repository.save(toSave);
        return mapper.toDomain(saved);
    }

    @Override
    public boolean existsByIdempotencyKey(String key) {
        if (key == null || key.isBlank()) return false;
        return repository.existsByIdempotencyKey(key);
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return repository.findById(String.valueOf(id)).map(mapper::toDomain);
    }

    @Override
    public Optional<StatusVersion> findStatusAndVersion(UUID id) {
        Query query = new Query(new Criteria().orOperator(
                Criteria.where("_id").is(id),
                Criteria.where("_id").is(id.toString())
        ));
        query.fields().include("status").include("version");

        OrderDocument doc = mongoTemplate.findOne(query, OrderDocument.class);
        if (doc == null) return Optional.empty();

        return Optional.of(new StatusVersion(
                OrderStatus.valueOf(doc.getStatus()),
                doc.getVersion() == null ? 0L : doc.getVersion()
        ));
    }


    @Override
    public boolean cancelIfPendingAndVersionMatches(UUID id, long expectedVersion, Instant ts) {
        Criteria idCriteria = new Criteria().orOperator(
                Criteria.where("_id").is(id),
                Criteria.where("_id").is(id.toString())
        );

        Criteria criteria = new Criteria().andOperator(
                idCriteria,
                Criteria.where("status").is(OrderStatus.PENDING),
                Criteria.where("version").is(expectedVersion)
        );

        Query q = Query.query(criteria);

        Update u = new Update()
                .set("status", OrderStatus.CANCELED)
                .set("updatedAt", ts)
                .inc("version", 1);

        UpdateResult r = mongoTemplate.updateFirst(q, u, OrderDocument.class);
        return r.getModifiedCount() == 1;
    }

    @Override
    public PageResult<Order> search(SearchCriteria criteria) {
        BigDecimal minTotal = criteria.minTotal();
        BigDecimal maxTotal = criteria.maxTotal();
        if (minTotal != null && maxTotal != null && minTotal.compareTo(maxTotal) > 0) {
            throw new OrderSearchBadRequest(criteria);
        }
        Instant from = criteria.from();
        Instant to = criteria.to();
        if (from != null && to != null && from.isAfter(to)) {
            throw new OrderSearchBadRequest(criteria);
        }

        List<Criteria> parts = new ArrayList<>();

        if (criteria.status() != null)
            parts.add(Criteria.where("status").is(criteria.status()));

        if (criteria.customerEmail() != null && !criteria.customerEmail().isBlank())
            parts.add(Criteria.where("customerEmail").is(criteria.customerEmail()));

        if (minTotal != null) parts.add(Criteria.where("total").gte(minTotal));
        if (maxTotal != null) parts.add(Criteria.where("total").lte(maxTotal));
        if (from != null) parts.add(Criteria.where("createdAt").gte(from));
        if (to != null) parts.add(Criteria.where("createdAt").lte(to));

        Criteria root = parts.isEmpty()
                ? new Criteria()
                : new Criteria().andOperator(parts.toArray(Criteria[]::new));
        Query q = new Query(root);

        int page = Math.max(criteria.page(), 0);
        int size = Math.max(1, Math.min(criteria.size(), 200));
        Sort sort = parseSort(criteria.sort());
        Pageable pageable = PageRequest.of(page, size, sort);
        q.with(pageable);

        List<OrderDocument> docs = mongoTemplate.find(q, OrderDocument.class);

        long totalElements = mongoTemplate.count(new Query(root), OrderDocument.class);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        var items = docs.stream().map(mapper::toDomain).toList();
        return new PageResult<>(items, page, size, totalElements, totalPages);
    }

    private Sort parseSort(String raw) {
        String[] parts = raw.split(",", 2);
        String field = parts[0].isBlank() ? "createdAt" : parts[0].trim();
        Sort.Direction dir = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim()))
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        return switch (field) {
            case "createdAt", "updatedAt", "total", "status", "customerEmail" -> Sort.by(dir, field);
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }
}

