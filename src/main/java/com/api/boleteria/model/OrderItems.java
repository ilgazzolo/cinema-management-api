package com.api.boleteria.model;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "orderItems")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class OrderItems {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)    
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_order_id", nullable = false)
    @JsonBackReference
    private StoreOrder storeOrder; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    @JsonBackReference
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double historicalPrice;

    @Column(nullable = true)
    private Double historicalUnitCost;

    @Column(nullable = false)
    private Integer historicalPriceInPoints;

    @Column(nullable = false)
    private Double subtotal;

    @Column(nullable = false)
    private Integer subtotalInPoints;
}
