package com.portfolio.inventory.messaging.event;

import java.time.Instant;
import java.util.UUID;

public record InventoryRejectedEvent(
        UUID orderId,
        String reason,
        Instant occurredAt
) {}
