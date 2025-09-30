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

    @Test
    @DisplayName("softDeleteById returns true when repository updates 1 row")
    void softDeleteById_success() {
        UUID id = UUID.randomUUID();
        Instant deletedAt = Instant.parse("2025-09-30T10:00:00Z");

        when(repository.softDeleteIfNotDeleted(eq(id), eq(deletedAt))).thenReturn(1);

        boolean result = adapter.softDeleteById(id, deletedAt);

        assertThat(result).isTrue();

        ArgumentCaptor<UUID> idCaptor = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<Instant> instantCaptor = ArgumentCaptor.forClass(Instant.class);

        verify(repository).softDeleteIfNotDeleted(idCaptor.capture(), instantCaptor.capture());
        assertThat(idCaptor.getValue()).isEqualTo(id);
        assertThat(instantCaptor.getValue()).isEqualTo(deletedAt);

        verifyNoMoreInteractions(repository, mapper);
    }

    @Test
    @DisplayName("softDeleteById returns false when repository updates 0 rows (missing or already deleted)")
    void softDeleteById_noop() {
        UUID id = UUID.randomUUID();
        Instant deletedAt = Instant.parse("2025-09-30T11:00:00Z");

        when(repository.softDeleteIfNotDeleted(eq(id), eq(deletedAt))).thenReturn(0);

        boolean result = adapter.softDeleteById(id, deletedAt);

        assertThat(result).isFalse();
        verify(repository).softDeleteIfNotDeleted(id, deletedAt);
        verifyNoMoreInteractions(repository, mapper);
    }

    @Test
    @DisplayName("updateIfVersionMatches delegates to repository and returns updated row count")
    void updateIfVersionMatches_delegates() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.parse("2025-09-30T12:00:00Z");

        when(repository.updateIfVersionMatches(eq(id), eq("ACME-1"), eq("Name"), any(), eq(5), eq("laptops"), eq(2L), eq(now)))
                .thenReturn(1);

        int updated = adapter.updateIfVersionMatches(id, "ACME-1", "Name", BigDecimal.TEN, 5, "laptops", 2L, now);

        assertThat(updated).isEqualTo(1);
        verify(repository).updateIfVersionMatches(eq(id), eq("ACME-1"), eq("Name"), eq(BigDecimal.TEN), eq(5), eq("laptops"), eq(2L), eq(now));
        verifyNoMoreInteractions(repository, mapper);
    }

    @Test
    @DisplayName("updateIfVersionMatches propagates DataIntegrityViolationException (SKU unique)")
    void updateIfVersionMatches_propagatesDataIntegrityViolation() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.parse("2025-09-30T12:30:00Z");

        when(repository.updateIfVersionMatches(any(), anyString(), anyString(), any(), anyInt(), anyString(), anyLong(), any()))
                .thenThrow(new org.springframework.dao.DataIntegrityViolationException("unique"));

        org.springframework.dao.DataIntegrityViolationException ex =
                org.junit.jupiter.api.Assertions.assertThrows(
                        org.springframework.dao.DataIntegrityViolationException.class,
                        () -> adapter.updateIfVersionMatches(id, "ACME-1", "Name", BigDecimal.ONE, 1, "laptops", 0L, now)
                );

        assertThat(ex).hasMessageContaining("unique");
        verify(repository).updateIfVersionMatches(any(), anyString(), anyString(), any(), anyInt(), anyString(), anyLong(), any());
        verifyNoMoreInteractions(repository, mapper);
    }

    @Test
    @DisplayName("existsActiveById delegates to repository")
    void existsActiveById_delegates() {
        UUID id = UUID.randomUUID();
        when(repository.existsByIdAndDeletedAtIsNull(id)).thenReturn(true);

        boolean exists = adapter.existsActiveById(id);

        assertThat(exists).isTrue();
        verify(repository).existsByIdAndDeletedAtIsNull(id);
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
