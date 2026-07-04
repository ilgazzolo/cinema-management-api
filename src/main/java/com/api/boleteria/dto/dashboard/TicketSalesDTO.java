package com.api.boleteria.dto.dashboard;

import java.math.BigDecimal;

public record TicketSalesDTO(
        Long movieId,
        String movieName,
        Integer ticketsSold,
        BigDecimal revenue
) {}
