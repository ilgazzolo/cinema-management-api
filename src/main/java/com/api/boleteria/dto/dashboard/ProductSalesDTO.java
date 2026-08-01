package com.api.boleteria.dto.dashboard;

import java.math.BigDecimal;

public record ProductSalesDTO(
        Long productId,
        String productName,
        Integer quantitySold,
        BigDecimal revenue,
        BigDecimal cost,
        BigDecimal profit
) {}
