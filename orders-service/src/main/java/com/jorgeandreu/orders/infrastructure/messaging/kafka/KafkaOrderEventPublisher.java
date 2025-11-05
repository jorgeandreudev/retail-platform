package com.jorgeandreu.orders.infrastructure.messaging.kafka;

import com.jorgeandreu.orders.application.port.out.OrderEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class KafkaOrderEventPublisher implements OrderEventPublisher {

    private final KafkaTemplate<String, OrderCanceledEvent> kafkaTemplate;

    @Value("${orders.topics.order-canceled:order-canceled}")
    private String topic;

    @Override
    public void publishOrderCanceled(UUID orderId, Instant canceledAt) {
        OrderCanceledEvent event = new OrderCanceledEvent(orderId, canceledAt);
        kafkaTemplate.send(topic, orderId.toString(), event);
    }
}
