package com.portfolio.order.service;

import com.portfolio.order.domain.Order;
import com.portfolio.order.exception.OrderNotFoundException;
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

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        Order order = Order.create(request.customerId());
        request.items().forEach(item ->
                order.addItem(item.productId(), item.quantity(), item.unitPrice()));
        return orderRepository.save(order);
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
}
