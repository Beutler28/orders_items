package com.portfolio.order.exception;

import com.portfolio.order.domain.OrderStatus;

import java.util.UUID;

public class InvalidOrderTransitionException extends RuntimeException {

    public InvalidOrderTransitionException(UUID orderId, OrderStatus from, OrderStatus to) {
        super("Transição inválida para o pedido %s: %s -> %s".formatted(orderId, from, to));
    }
}
