package com.portfolio.payment.messaging;

import com.portfolio.payment.messaging.event.OrderCreatedEvent;
import com.portfolio.payment.messaging.event.PaymentCompletedEvent;
import com.portfolio.payment.messaging.event.PaymentFailedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Component
public class PaymentProcessor {

    private static final Logger log = LoggerFactory.getLogger(PaymentProcessor.class);

    // Regra simulada: pedidos acima deste valor são recusados (representa "limite/saldo").
    private static final BigDecimal APPROVAL_LIMIT = new BigDecimal("10000.00");

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentProcessor(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = Topics.ORDER_CREATED, groupId = "payment-service")
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Processando pagamento do pedido {} (valor {})", event.orderId(), event.totalAmount());

        boolean approved = event.totalAmount().compareTo(APPROVAL_LIMIT) <= 0;
        String key = event.orderId().toString();

        if (approved) {
            var completed = new PaymentCompletedEvent(
                    event.orderId(), UUID.randomUUID(), event.totalAmount(), Instant.now());
            kafkaTemplate.send(Topics.PAYMENT_COMPLETED, key, completed);
            log.info("Pagamento aprovado para o pedido {}", event.orderId());
        } else {
            var failed = new PaymentFailedEvent(
                    event.orderId(), "valor acima do limite permitido", Instant.now());
            kafkaTemplate.send(Topics.PAYMENT_FAILED, key, failed);
            log.info("Pagamento recusado para o pedido {}", event.orderId());
        }
    }
}
