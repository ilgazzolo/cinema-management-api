package com.api.boleteria.dto.detail;

import com.api.boleteria.model.enums.ProductType;; 

public record ProductDetailDTO(
    Long id,
    String name,
    Double unitPrice,
    Integer priceInPoints,
    Integer stock,
    Double totalCostStock,
    String imageURL,
    String description,
    Boolean available,
    ProductType productType

) {}
