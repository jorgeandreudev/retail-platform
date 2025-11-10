package com.jorgeandreu.orders.infastructure.messaging.kafka;

import com.jorgeandreu.orders.infrastructure.messaging.kafka.KafkaOrderEventPublisher;
import com.jorgeandreu.orders.infrastructure.messaging.kafka.OrderCanceledEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class KafkaOrderEventPublisherTest {

    @SuppressWarnings("unchecked")
    private final KafkaTemplate<String, OrderCanceledEvent> template =
            (KafkaTemplate<String, OrderCanceledEvent>) mock(KafkaTemplate.class);

    @Test
    @DisplayName("publishOrderCanceled sends to default topic with correct key and payload")
    void publishOrderCanceled_defaultTopic_ok() {
        var publisher = new KafkaOrderEventPublisher(template);
        ReflectionTestUtils.setField(publisher, "topic", "order-canceled");

        UUID orderId = UUID.randomUUID();
        Instant canceledAt = Instant.parse("2025-10-16T10:58:11.275457300Z");

        publisher.publishOrderCanceled(orderId, canceledAt);

        ArgumentCaptor<String> topicCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<OrderCanceledEvent> valueCap = ArgumentCaptor.forClass(OrderCanceledEvent.class);

        verify(template, times(1)).send(topicCap.capture(), keyCap.capture(), valueCap.capture());

        assertThat(topicCap.getValue()).isEqualTo("order-canceled");
        assertThat(keyCap.getValue()).isEqualTo(orderId.toString());

        OrderCanceledEvent sent = valueCap.getValue();
        assertThat(sent).isNotNull();
        assertThat(sent.orderId()).isEqualTo(orderId);
        assertThat(sent.canceledAt()).isEqualTo(canceledAt);
    }

    @Test
    @DisplayName("publishOrderCanceled honors custom topic from @Value")
    void publishOrderCanceled_customTopic_ok() {
        var publisher = new KafkaOrderEventPublisher(template);
        ReflectionTestUtils.setField(publisher, "topic", "orders.cancelled.v1");

        UUID orderId = UUID.randomUUID();
        Instant canceledAt = Instant.now();

        publisher.publishOrderCanceled(orderId, canceledAt);

        ArgumentCaptor<String> topicCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<OrderCanceledEvent> valueCap = ArgumentCaptor.forClass(OrderCanceledEvent.class);

        verify(template).send(topicCap.capture(), keyCap.capture(), valueCap.capture());

        assertThat(topicCap.getValue()).isEqualTo("orders.cancelled.v1");
        assertThat(keyCap.getValue()).isEqualTo(orderId.toString());

        OrderCanceledEvent event = valueCap.getValue();
        assertThat(event.orderId()).isEqualTo(orderId);
        assertThat(event.canceledAt()).isEqualTo(canceledAt);
    }
}

