package com.api.boleteria.repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.api.boleteria.model.StoreOrder;
import com.api.boleteria.model.User;
import com.api.boleteria.model.enums.StatusPayment;

public interface IStoreOrderRepository extends JpaRepository <StoreOrder, Long> {
    // Busca el carrito activo del usuario
    Optional<StoreOrder> findByUserAndStatus(User user, StatusPayment status);
    
    // Busca el historial de compras finalizadas
    List<StoreOrder> findAllByUserAndStatusOrderByCreatedAtDesc(User user, StatusPayment status);

    @EntityGraph(attributePaths = {"items", "items.product"})
    List<StoreOrder> findByStatusAndCreatedAtBetween(StatusPayment status, LocalDateTime start, LocalDateTime end);
}

