package com.portfolio.inventory.messaging;

import com.portfolio.inventory.messaging.event.InventoryReservedEvent;
import com.portfolio.inventory.messaging.event.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class InventoryReservationListener {

    private static final Logger log = LoggerFactory.getLogger(InventoryReservationListener.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InventoryReservationListener(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // Fase 2: apenas registra a reserva. A lógica real de estoque com locking
    // (e o encaixe na ordem da saga) entra na Fase 3.
    @KafkaListener(topics = Topics.ORDER_CREATED, groupId = "inventory-service")
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Reservando estoque para o pedido {} ({} itens)",
                event.orderId(), event.items().size());

        var reserved = new InventoryReservedEvent(event.orderId(), Instant.now());
        kafkaTemplate.send(Topics.INVENTORY_RESERVED, event.orderId().toString(), reserved);
    }
}
