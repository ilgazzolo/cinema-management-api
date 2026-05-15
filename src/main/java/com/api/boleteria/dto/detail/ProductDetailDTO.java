package com.api.boleteria.dto.detail;


public record ProductDetailDTO(
    Long id,
    String name,
    Double unitPrice,
    Integer stock,
    String imageURL,
    String description,
    Boolean available
) {}
