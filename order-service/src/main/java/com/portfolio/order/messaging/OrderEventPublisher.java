package com.portfolio.order.messaging;

import com.portfolio.order.domain.Order;
import com.portfolio.order.messaging.event.OrderCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class OrderEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderCreated(Order order) {
        var items = order.getItems().stream()
                .map(i -> new OrderCreatedEvent.Item(i.getProductId(), i.getQuantity(), i.getUnitPrice()))
                .toList();

        var event = new OrderCreatedEvent(
                order.getId(), order.getCustomerId(), order.getTotalAmount(), items, Instant.now());

        // A chave é o id do pedido: garante que todos os eventos do mesmo pedido
        // caiam na mesma partição e sejam processados em ordem.
        kafkaTemplate.send(Topics.ORDER_CREATED, order.getId().toString(), event);
    }
}
