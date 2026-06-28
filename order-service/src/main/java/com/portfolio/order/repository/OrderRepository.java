package com.portfolio.order.repository;

import com.portfolio.order.domain.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    // Carrega os itens na mesma query, evitando o problema N+1.
    @EntityGraph(attributePaths = "items")
    Optional<Order> findWithItemsById(UUID id);
}
