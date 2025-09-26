package com.jorgeandreu.products.infrastructure.db;


import com.jorgeandreu.products.domain.model.Product;
import com.jorgeandreu.products.domain.model.SearchCriteria;
import com.jorgeandreu.products.domain.port.out.ProductRepositoryPort;
import com.jorgeandreu.products.infrastructure.db.maper.ProductEntityMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;


@Repository
public class ProductRepositoryAdapter implements ProductRepositoryPort {

    private final SpringDataProductRepository repository;
    private final ProductEntityMapper mapper;

    public ProductRepositoryAdapter(SpringDataProductRepository repository, ProductEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public boolean existsBySku(String sku) {
        return repository.existsBySku(sku);
    }

    @Override
    public Product save(Product product) {
        var saved = repository.save(mapper.toEntity(product));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public com.jorgeandreu.products.domain.model.PageResult<Product> search(SearchCriteria criteria) {
        Sort sort = parseSort(criteria.sort());
        Pageable pageable = PageRequest.of(criteria.page(), criteria.size(), sort);

        Specification<ProductEntity> spec = Stream.of(
                        visibility(criteria.includeDeleted()),
                        category(criteria.category()),
                        priceGte(criteria.minPrice()),
                        priceLte(criteria.maxPrice()),
                        text(criteria.text())
                )
                .filter(Objects::nonNull)
                .reduce(Specification::and)
                .orElse((root, q, cb) -> cb.conjunction());

        Page<ProductEntity> page = repository.findAll(spec, pageable);
        return mapper.toDomain(page);
    }

    // --- Specifications (static helpers) ---
    private Specification<ProductEntity> visibility(boolean includeDeleted) {
        return (root, q, cb) -> includeDeleted ? cb.conjunction() : cb.isNull(root.get("deletedAt"));
    }
    private Specification<ProductEntity> category(String category) {
        return (category == null || category.isBlank())
                ? null
                : (root, q, cb) -> cb.equal(cb.lower(root.get("category")), category.toLowerCase());
    }
    private Specification<ProductEntity> priceGte(Double v) {
        return v == null ? null : (root, q, cb) -> cb.ge(root.get("price"), v);
    }
    private Specification<ProductEntity> priceLte(Double v) {
        return v == null ? null : (root, q, cb) -> cb.le(root.get("price"), v);
    }
    private Specification<ProductEntity> text(String text) {
        if (text == null || text.isBlank()) return null;
        String like = "%" + text.toLowerCase() + "%";
        return (root, q, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), like),
                cb.like(cb.lower(root.get("text")), like),
                cb.like(cb.lower(root.get("description")), like)
        );
    }

    private Sort parseSort(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) return Sort.by(Sort.Direction.DESC, "createdAt");
        String[] parts = sortParam.split(",");
        String prop = parts[0];
        Sort.Direction dir = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1])) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, prop);
    }
}
