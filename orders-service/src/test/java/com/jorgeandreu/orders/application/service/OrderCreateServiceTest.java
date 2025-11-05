package com.jorgeandreu.orders.application.service;

import com.jorgeandreu.orders.domain.model.Order;
import com.jorgeandreu.orders.domain.model.OrderItem;
import com.jorgeandreu.orders.domain.model.ProductSnapshot;
import com.jorgeandreu.orders.domain.port.in.CreateOrderCommand;
import com.jorgeandreu.orders.domain.port.in.CreateOrderCommand.Item;
import com.jorgeandreu.orders.domain.port.out.OrderRepositoryPort;
import com.jorgeandreu.orders.domain.port.out.ProductCatalogPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class OrderCreateServiceTest {

    @Mock private OrderRepositoryPort orderRepository;
    @Mock private ProductCatalogPort catalog;

    @InjectMocks
    private OrderCreateService service;

    private UUID productA;
    private UUID productB;

    @BeforeEach
    void setup() {
        productA = UUID.randomUUID();
        productB = UUID.randomUUID();
    }

    @Nested
    @DisplayName("create() - success cases")
    class SuccessCases {

        @Test
        @DisplayName("creates an order with single item and saves it")
        void createsSingleItemOrder() {
            var customerEmail = "john@example.com";
            var quantity = 2;

            var snap = new ProductSnapshot(productA, "SKU-A", "Laptop", new BigDecimal("499.99"));
            when(catalog.findSnapshotById(productA.toString())).thenReturn(snap);

            var cmd = new CreateOrderCommand(
                    customerEmail,
                    List.of(new Item(productA.toString(), quantity)),
                    "idem-key-123"
            );

            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            when(orderRepository.save(orderCaptor.capture()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Order result = service.create(cmd);

            verify(catalog).findSnapshotById(productA.toString());
            verify(orderRepository).save(any(Order.class));

            Order savedOrder = orderCaptor.getValue();
            assertThat(savedOrder).isNotNull();
            assertThat(savedOrder.items()).hasSize(1);
            OrderItem item = savedOrder.items().get(0);

            assertThat(item.productId()).isEqualTo(productA.toString());
            assertThat(item.sku()).isEqualTo("SKU-A");
            assertThat(item.name()).isEqualTo("Laptop");
            assertThat(item.unitPrice()).isEqualByComparingTo("499.99");
            assertThat(item.quantity()).isEqualTo(2);

            assertThat(savedOrder.status().name()).isEqualTo("PENDING");
            assertThat(savedOrder.customerEmail()).isEqualTo(customerEmail);
            assertThat(savedOrder.createdAt()).isNotNull();
            assertThat(savedOrder.total()).isEqualByComparingTo("999.98");

            assertThat(result).isSameAs(savedOrder);

            verifyNoMoreInteractions(orderRepository, catalog);
        }

        @Test
        @DisplayName("creates an order with multiple products and aggregates items correctly")
        void createsMultipleItemOrder() {

            var email = "multi@case.com";
            var cmd = new CreateOrderCommand(
                    email,
                    List.of(
                            new Item(productA.toString(), 1),
                            new Item(productB.toString(), 3)
                    ),
                    "idem-key-123"
            );

            var snapA = new ProductSnapshot(productA, "SKU-A", "Mouse", new BigDecimal("10.00"));
            var snapB = new ProductSnapshot(productB, "SKU-B", "Keyboard", new BigDecimal("30.00"));

            when(catalog.findSnapshotById(productA.toString())).thenReturn(snapA);
            when(catalog.findSnapshotById(productB.toString())).thenReturn(snapB);

            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            when(orderRepository.save(orderCaptor.capture()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Order created = service.create(cmd);

            verify(catalog).findSnapshotById(productA.toString());
            verify(catalog).findSnapshotById(productB.toString());
            verify(orderRepository).save(any(Order.class));

            Order orderToSave = orderCaptor.getValue();
            assertThat(orderToSave.customerEmail()).isEqualTo(email);
            assertThat(orderToSave.items()).hasSize(2);

            BigDecimal total = orderToSave.items().stream()
                    .map(OrderItem::lineTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            assertThat(total).isEqualByComparingTo(orderToSave.total());

            assertThat(created.status().name()).isEqualTo("PENDING");
            assertThat(created.createdAt()).isNotNull();

            verifyNoMoreInteractions(orderRepository, catalog);
        }
    }

    @Nested
    @DisplayName("create() - error cases")
    class ErrorCases {

        @Test
        @DisplayName("throws when catalog cannot find snapshot for product")
        void throwsWhenProductNotFound() {
            var cmd = new CreateOrderCommand(
                    "fail@example.com",
                    List.of(new Item(productA.toString(), 1)),
                    "idem-key-123"
            );
            when(catalog.findSnapshotById(productA.toString()))
                    .thenThrow(new RuntimeException("Product not found"));

            assertThatThrownBy(() -> service.create(cmd))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Product not found");

            verify(catalog).findSnapshotById(productA.toString());
            verifyNoInteractions(orderRepository);
        }
    }
}

