package com.jorgeandreu.products.infrastructure.db;

import com.jorgeandreu.products.domain.model.Product;
import com.jorgeandreu.products.domain.model.SearchCriteria;
import com.jorgeandreu.products.infrastructure.db.mapper.ProductEntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


class ProductRepositoryAdapterTest {

    private SpringDataProductRepository repository;
    private ProductEntityMapper mapper;
    private ProductRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = mock(SpringDataProductRepository.class);
        mapper = mock(ProductEntityMapper.class);
        adapter = new ProductRepositoryAdapter(repository, mapper);
    }

    @Test
    @DisplayName("existsBySku delegates to repository")
    void existsBySku() {
        when(repository.existsBySku("SKU-123")).thenReturn(true);

        boolean result = adapter.existsBySku("SKU-123");

        assertThat(result).isTrue();
        verify(repository).existsBySku("SKU-123");
        verifyNoMoreInteractions(repository, mapper);
    }

    @Test
    @DisplayName("save maps domain -> entity, saves, then maps back entity -> domain")
    void save() {
        Product domain = validProduct("SKU-1", "Name 1");
        ProductEntity entity = new ProductEntity();
        ProductEntity savedEntity = new ProductEntity();
        Product mappedBack = validProduct("SKU-1", "Name 1 (saved)");

        when(mapper.toEntity(domain)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(mappedBack);

        Product result = adapter.save(domain);

        assertThat(result).isSameAs(mappedBack);
        verify(mapper).toEntity(domain);
        verify(repository).save(entity);
        verify(mapper).toDomain(savedEntity);
        verifyNoMoreInteractions(repository, mapper);
    }

    @Test
    @DisplayName("findById returns mapped domain when entity exists")
    void findById_found() {
        UUID id = UUID.randomUUID();
        ProductEntity entity = new ProductEntity();
        Product mapped = validProduct("SKU-FOUND", "Found");

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(mapped);

        Optional<Product> result = adapter.findById(id);

        assertThat(result).contains(mapped);
        verify(repository).findById(id);
        verify(mapper).toDomain(entity);
        verifyNoMoreInteractions(repository, mapper);
    }

    @Test
    @DisplayName("findById returns empty when not found")
    void findById_notFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        Optional<Product> result = adapter.findById(id);

        assertThat(result).isEmpty();
        verify(repository).findById(id);
        verifyNoMoreInteractions(repository, mapper);
    }

    @Test
    @DisplayName("search builds pageable (page=1,size=20, sort=name ASC) and delegates to repository")
    void search_withCriteria() {
        SearchCriteria criteria = new SearchCriteria(
                1,
                20,
                "name,asc",
                "laptops",
                1000.0,
                2000.0,
                "gaming",
                false
        );

        ProductEntity entity = new ProductEntity();
        Page<ProductEntity> repoPage = new PageImpl<>(List.of(entity), PageRequest.of(1, 20), 1);
        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(repoPage);

        @SuppressWarnings("unchecked")
        com.jorgeandreu.products.domain.model.PageResult<Product> mappedPage =
                mock(com.jorgeandreu.products.domain.model.PageResult.class);
        when(mapper.toDomain(repoPage)).thenReturn(mappedPage);

        var result = adapter.search(criteria);

        // Assert pageable used
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(any(Specification.class), pageableCaptor.capture());
        Pageable usedPageable = pageableCaptor.getValue();

        assertThat(usedPageable.getPageNumber()).isEqualTo(1);
        assertThat(usedPageable.getPageSize()).isEqualTo(20);
        Sort.Order order = usedPageable.getSort().getOrderFor("name");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.ASC);

        // Assert mapping
        assertThat(result).isSameAs(mappedPage);
        verify(mapper).toDomain(repoPage);
        verifyNoMoreInteractions(repository, mapper);
    }

    @Test
    @DisplayName("search with defaults (sort=null) uses createdAt DESC")
    void search_withDefaults_usesCreatedAtDesc() {
        SearchCriteria criteria = new SearchCriteria(
                0,
                10,
                null,
                null,
                null,
                null,
                null,
                true
        );

        Page<ProductEntity> empty = Page.empty();
        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(empty);

        @SuppressWarnings("unchecked")
        com.jorgeandreu.products.domain.model.PageResult<Product> mapped =
                mock(com.jorgeandreu.products.domain.model.PageResult.class);
        when(mapper.toDomain(empty)).thenReturn(mapped);

        var result = adapter.search(criteria);

        assertThat(result).isSameAs(mapped);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(any(Specification.class), pageableCaptor.capture());
        Pageable usedPageable = pageableCaptor.getValue();

        Sort.Order order = usedPageable.getSort().getOrderFor("createdAt");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);

        verify(mapper).toDomain(empty);
        verifyNoMoreInteractions(repository, mapper);
    }


    private static Product validProduct(String sku, String name) {
        return new Product(
                UUID.randomUUID(),
                sku,
                name,
                BigDecimal.ZERO,
                0,
                "laptops",
                Instant.now(),
                Instant.now(),
                null,
                0L
        );
    }
}
