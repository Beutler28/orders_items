package com.portfolio.payment.messaging.event;

import java.time.Instant;
import java.util.UUID;

public record PaymentFailedEvent(
        UUID orderId,
        String reason,
        Instant occurredAt
) {}
