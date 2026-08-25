package com.api.boleteria.mercadopago.dto.paymentStore;

import java.math.BigDecimal;
import java.util.List;

import com.api.boleteria.dto.detail.OrderItemsDetailDTO;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentStoreRequestDTO {

    @NotNull(message = "Debe detallar el numero de pedido.")
    private String title;     

    private Long storeOrderId;

    @Email(message = "El email debe tener un formato valido.")
    private String userEmail;       

    @NotNull(message = "Debe especificar la cantidad de productos.")
    @NotEmpty
    private List<OrderItemsDetailDTO> items;

    @NotNull(message = "Debe especificar el monto total.")
    @DecimalMin(value = "0.0", inclusive = false, message = "El monto debe ser positivo.")
    private BigDecimal TotalAmount;   

    @NotNull(message = "Debe especificar el monto total en puntos.")
    @DecimalMin(value = "0.0", inclusive = false, message = "El monto debe ser positivo.")
    private Integer TotalAmountInPoints; 

}
