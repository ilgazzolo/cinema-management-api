package com.api.boleteria.dto.detail;

import java.util.List;

import com.api.boleteria.model.enums.StatusPayment;

public record StoreOrderDetailDTO(
    Long id,
    StatusPayment status,
    String createdAtDate,
    String createdAtTime,
    List<OrderItemsDetailDTO> items,
    Double totalAmount,
    Integer totalAmountInPoints
) {}