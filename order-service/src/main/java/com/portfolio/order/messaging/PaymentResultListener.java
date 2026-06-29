package com.portfolio.order.messaging;

import com.portfolio.order.messaging.event.PaymentCompletedEvent;
import com.portfolio.order.messaging.event.PaymentFailedEvent;
import com.portfolio.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentResultListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentResultListener.class);

    private final OrderService orderService;

    public PaymentResultListener(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(topics = Topics.PAYMENT_COMPLETED, groupId = "order-service")
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        log.info("Pagamento aprovado para o pedido {}", event.orderId());
        orderService.markPaid(event.orderId());
    }

    @KafkaListener(topics = Topics.PAYMENT_FAILED, groupId = "order-service")
    public void onPaymentFailed(PaymentFailedEvent event) {
        log.info("Pagamento recusado para o pedido {}: {}", event.orderId(), event.reason());
        orderService.markPaymentFailed(event.orderId());
    }
}
