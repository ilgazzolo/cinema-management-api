package com.api.boleteria.validators;

import com.api.boleteria.dto.request.ProductRequestDTO;
import com.api.boleteria.exception.BadRequestException;


public class ProductValidator {

    public static void validateFields(ProductRequestDTO req){
        validateName(req.getName());
        validateStock(req.getStock());
        validateUnitCost(req.getUnitCost());
        validateUnitPrice(req.getUnitPrice());
        validatePriceInPoints(req.getPriceInPoints());
        validateImage(req.getImageURL());
        validateAvailable(req.getAvailable());
        validateProductType(req.getProductType());
        validateDescription(req.getDescription());
    }


    public static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto no puede estar vacío.");
        }
        if (name.length() > 50) {
            throw new IllegalArgumentException("El nombre del producto no puede superar los 50 caracteres.");
        }
    }

    public static void validateStock (Integer quantity){
        if(quantity == null || quantity < 0){
            throw new BadRequestException("La cantidad no puede ser nula o negativa.");
        }
    }

    public static void validateImage (String url){
        if(url == null){
            throw new BadRequestException("El producto debe tener una imagen de muestra.");
        }
    }

    public static void validateUnitPrice (Double price){
        if(price == null || price < 0.00){
            throw new BadRequestException("El precio no puede ser nulo o negativo.");
        }
    }

    public static void validateUnitCost (Double price){
        if(price == null || price < 0.00){
            throw new BadRequestException("El costo unitario no puede ser nulo o negativo.");
        }
    }

    public static void validatePriceInPoints (Integer priceInPoints){
        if(priceInPoints == null || priceInPoints < 0.00){
            throw new BadRequestException("El precio en puntos no puede ser nulo o negativo.");
        }
    }

    public static void validateAvailable (Boolean av){
        if(av == null){
            throw new BadRequestException("Debe indicar si el producto esta disponible o no.");
        }
    }

    public static void validateProductType(Object productType) {
        if (productType == null) {
            throw new BadRequestException("La categoria del producto no puede ser nula.");
        }
    }


    public static void validateDescription (String desc){
        if(desc == null){
            throw new BadRequestException("El producto debe tener una descripción.");
        }
    }


    public static void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID del producto debe ser mayor a 0 y no puede ser nulo.");
        }
    }


}
