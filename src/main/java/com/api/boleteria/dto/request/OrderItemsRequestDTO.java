package com.api.boleteria.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemsRequestDTO{
    
    @NotNull(message = "La orden de compra debe tener un producto asociado.")
    @Positive(message = "El ID de la función debe ser un valor positivo.")
    private Long productId;
    
    @NotNull(message = "Debe especificar la cantidad de unidades a comprar.")
    @Min(value = 1, message = "Debe comprar al menos una unidad.")
    private Integer quantity;
}
