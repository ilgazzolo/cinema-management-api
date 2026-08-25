package com.api.boleteria.mercadopago.dto.paymentStore;

public record PaymentStorePointsResponseDTO(
        Long paymentStoreId,
        Long storeOrderId,
        String status,
        Boolean paidPoints,
        String purchaseCode,
        Integer remainingPoints
) {}
