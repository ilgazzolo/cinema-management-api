package com.api.boleteria.dto.detail;

public record OrderItemsDetailDTO(
    Long id,
    String productName,
    String imageURL,
    Integer quantity,
    Double historicalPrice,
    Double historicalUnitCost,
    Integer historicalPriceInPoints, 
    Double subtotal,
    Integer subtotalInPoints
) {}
