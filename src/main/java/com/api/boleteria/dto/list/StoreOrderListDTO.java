package com.api.boleteria.dto.list;

public record StoreOrderListDTO(
    Long id,
    String createdAtDate,
    String createdAtTime,
    Double totalAmount,
    Integer totalAmountInPoints
) {}


