package com.api.boleteria.model;

import com.api.boleteria.model.enums.StatusPayment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "paymentsProducts")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class PaymentStore {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String mpPaymentId; // ID de Mercado Pago

    @Column(unique = true, nullable = true)
    private String preferenceId;  // ID de la preferencia

    private Long userId;    // Id del usuario que realizó el pago

    private String userEmail;   // Email del usuario que realizó el pago

    @Column(nullable = false)
    private Integer quantity;  // Cantidad de productos comprados

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPayment status;   // Estado del pago (approved, pending, failure)

    private BigDecimal amount;  // Monto del pago
    private LocalDateTime createdAt;    // Fecha de creación del pago
    private LocalDateTime updatedAt;    // Fecha de actualización del pago
    private LocalDateTime date;

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "product_id", unique = true, nullable = true)
    private Product product;

    @ElementCollection
    @CollectionTable(name = "payment_store", joinColumns = @JoinColumn(name = "paymentStore_id"))
    @Column(name = "products")
    private List<String> products = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    } 
}
