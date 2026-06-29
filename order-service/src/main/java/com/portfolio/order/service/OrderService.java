package com.portfolio.order.service;

import com.portfolio.order.domain.Order;
import com.portfolio.order.domain.OrderStatus;
import com.portfolio.order.exception.OrderNotFoundException;
import com.portfolio.order.messaging.OrderEventPublisher;
import com.portfolio.order.repository.OrderRepository;
import com.portfolio.order.web.dto.CreateOrderRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher eventPublisher;

    public OrderService(OrderRepository orderRepository, OrderEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        Order order = Order.create(request.customerId());
        request.items().forEach(item ->
                order.addItem(item.productId(), item.quantity(), item.unitPrice()));
        orderRepository.save(order);

        eventPublisher.publishOrderCreated(order);
        order.transitionTo(OrderStatus.PAYMENT_PENDING);

        return order;
    }

    @Transactional
    public void markPaid(UUID orderId) {
        Order order = findOrFail(orderId);
        order.transitionTo(OrderStatus.PAID);
    }

    @Transactional
    public void markPaymentFailed(UUID orderId) {
        Order order = findOrFail(orderId);
        order.transitionTo(OrderStatus.PAYMENT_FAILED);
    }

    @Transactional(readOnly = true)
    public Order getOrder(UUID id) {
        return orderRepository.findWithItemsById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Page<Order> listOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    private Order findOrFail(UUID id) {
        return orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }
}
