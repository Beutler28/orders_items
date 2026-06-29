package com.portfolio.order.messaging.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentCompletedEvent(
        UUID orderId,
        UUID paymentId,
        BigDecimal amount,
        Instant occurredAt
) {}
