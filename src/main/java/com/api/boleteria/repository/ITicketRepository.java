package com.api.boleteria.repository;

import com.api.boleteria.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Repository
public interface ITicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByUserId(Long userId);
    Optional<Ticket> findTopByUserIdAndFunctionIdOrderByPurchaseDateTimeDesc(Long userId, Long functionId);
    List<Ticket> findByPurchaseDateTimeBetween(LocalDateTime start, LocalDateTime end);

}
