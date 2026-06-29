package com.portfolio.inventory.messaging.event;

import java.time.Instant;
import java.util.UUID;

public record InventoryReservedEvent(
        UUID orderId,
        Instant occurredAt
) {}
