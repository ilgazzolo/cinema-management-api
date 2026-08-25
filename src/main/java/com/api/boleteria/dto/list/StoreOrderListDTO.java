package com.api.boleteria.dto.list;

import java.util.List;
import com.api.boleteria.dto.detail.OrderItemsDetailDTO;

public record StoreOrderListDTO(
    Long id,
    String createdAtDate,
    String createdAtTime,
    Double totalAmount,
    Integer totalAmountInPoints,
    Boolean paidPoints,
    String purchaseCode,
    List<OrderItemsDetailDTO> items
) {}
