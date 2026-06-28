package com.portfolio.order.domain;

import java.util.Map;
import java.util.Set;

public enum OrderStatus {

    CREATED,
    PAYMENT_PENDING,
    PAID,
    PAYMENT_FAILED,
    CONFIRMED,
    CANCELLED;

    // Máquina de estados: para cada status, os destinos válidos.
    // CONFIRMED e CANCELLED são terminais (sem saída).
    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = Map.of(
            CREATED,         Set.of(PAYMENT_PENDING, CANCELLED),
            PAYMENT_PENDING, Set.of(PAID, PAYMENT_FAILED, CANCELLED),
            PAID,            Set.of(CONFIRMED, CANCELLED),
            PAYMENT_FAILED,  Set.of(CANCELLED),
            CONFIRMED,       Set.of(),
            CANCELLED,       Set.of()
    );

    public boolean canTransitionTo(OrderStatus target) {
        return TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }

    public boolean isTerminal() {
        return TRANSITIONS.getOrDefault(this, Set.of()).isEmpty();
    }
}
