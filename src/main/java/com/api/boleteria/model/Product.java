package com.api.boleteria.model;

import com.api.boleteria.model.enums.ProductType;
import com.api.boleteria.model.enums.ScreenType;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @DecimalMin(value = "0.0", inclusive = false, message = "El monto debe ser positivo.")
    private Double unitPrice;

    @Column(nullable = false)
    @DecimalMin(value = "0.0", inclusive = false, message = "El monto debe ser positivo.")
    private Integer priceInPoints;

    @Column(nullable = false)
    @Min(value=0)
    private Integer stock;

    @Column(nullable = false)
    private String imageURL;

    @Column (nullable = false)
    private String description;

    @Column(nullable = false)
    private Boolean available;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductType productType;

    @OneToOne(mappedBy = "product", fetch = FetchType.LAZY)
    private PaymentStore payment;

}
