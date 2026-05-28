package com.api.boleteria.validators;

import com.api.boleteria.model.Product;
import com.api.boleteria.model.StoreOrder;
import com.api.boleteria.model.enums.StatusPayment;

public class StoreOrderValidator {
    public static void validateStock(Product product, Integer requestedQuantity) {
        if (product.getStock() < requestedQuantity) {
            throw new RuntimeException("No hay stock suficiente para el producto: " + product.getName());
        }
    }

    public static void validateActiveCart(StoreOrder order) {
        if (order.getStatus() != StatusPayment.PENDING) {
            throw new RuntimeException("Esta orden ya fue procesada o cancelada.");
        }
    }
}
