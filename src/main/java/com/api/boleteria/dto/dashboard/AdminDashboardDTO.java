package com.api.boleteria.dto.dashboard;

import java.time.LocalDate;
import java.util.List;

public record AdminDashboardDTO(
        LocalDate from,
        LocalDate to,
        AdminDashboardSummaryDTO summary,
        List<TicketSalesDTO> ticketSales,
        List<ProductSalesDTO> productSales
) {}
