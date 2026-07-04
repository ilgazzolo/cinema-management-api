package com.api.boleteria.dto.request;


import com.api.boleteria.model.enums.ProductType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class ProductRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 1, max = 100, message = "El nombre debe tener entre 1 y 100 caracteres")
    private String name;
    
    @NotNull(message = "Debe especificar el precio unitario del producto.")
    @DecimalMin(value = "0.0", inclusive = false, message = "El monto debe ser positivo.")
    private Double unitPrice;

    @NotNull(message = "Debe especificar el precio en puntos del producto.")
    @DecimalMin(value = "0.0", inclusive = false, message = "El monto debe ser positivo.")
    private Integer priceInPoints;
    
    @NotNull(message = "Debe especificar la cantidad de unidades del producto.")
    @Min(value = 0, message = "No puede poner un stock negativo.")
    private Integer stock;

    @NotNull(message = "Debe especificar el costo unitario del producto.")
    @DecimalMin(value = "0.0", inclusive = false, message = "El monto debe ser positivo.")
    private Double unitCost;

    @NotBlank(message = "La imagen del producto es obligatoria")
    @NotNull(message = "El producto debe tener una imagen.")
    private String imageURL;

    @NotBlank(message = "La descripción del producto es obligatoria")
    @NotNull(message = "El producto debe tener una descripción.")
    private String description;
    
    @NotNull(message = "Debe indicar si el producto esta disponible.")
    private Boolean available;

    @NotNull(message = "La categoria del producto es obligatoria.")
    private ProductType productType;
}
