package com.api.boleteria.mercadopago.dto.paymentStore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentStoreResponseDTO {
    private String preferenceId;
    private String initPoint;

    public PaymentStoreResponseDTO(String preferenceId, String initPoint) {
        this.preferenceId = preferenceId;
        this.initPoint = initPoint;
    }

}
