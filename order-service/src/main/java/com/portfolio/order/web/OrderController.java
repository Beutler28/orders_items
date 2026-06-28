package com.portfolio.order.web;

import com.portfolio.order.service.OrderService;
import com.portfolio.order.web.dto.CreateOrderRequest;
import com.portfolio.order.web.dto.OrderResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request,
                                                UriComponentsBuilder uriBuilder) {
        OrderResponse response = OrderResponse.from(orderService.createOrder(request));
        URI location = uriBuilder.path("/api/orders/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    public OrderResponse getById(@PathVariable UUID id) {
        return OrderResponse.from(orderService.getOrder(id));
    }

    @GetMapping
    public Page<OrderResponse> list(Pageable pageable) {
        return orderService.listOrders(pageable).map(OrderResponse::from);
    }
}
