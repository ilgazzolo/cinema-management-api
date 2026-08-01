package com.api.boleteria.dto.dashboard;

import java.math.BigDecimal;

public record AdminDashboardSummaryDTO(
        Integer ticketsSold,
        BigDecimal ticketRevenue,
        Integer productsSold,
        BigDecimal storeRevenue,
        BigDecimal productCost,
        BigDecimal netProfit
) {}
