package com.api.boleteria.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateItemQuantityRequestDTO {

    @NotNull(message = "Debe especificar la nueva cantidad del ítem.")
    @Min(value = 1, message = "La cantidad debe ser al menos 1. Para quitar el ítem, elimínalo del carrito.")
    private Integer quantity;
}
