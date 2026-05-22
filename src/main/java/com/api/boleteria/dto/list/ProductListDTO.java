package com.api.boleteria.dto.list;

public record ProductListDTO(
    Long id,
    String name,
    Double unitPrice,
    Integer priceInPoints,
    String imageURL
) {}
