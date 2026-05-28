package com.api.boleteria.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.api.boleteria.model.enums.StatusPayment;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "storeOrder")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class StoreOrder {
    @Id    
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    @OneToMany(mappedBy = "storeOrder", cascade = CascadeType.ALL, orphanRemoval = false)
    @JsonManagedReference("orderItem-storeOrder")
    List<OrderItems> items = new ArrayList<>();

    @OneToOne(mappedBy = "storeOrder", fetch = FetchType.LAZY)
    private PaymentStore payment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPayment status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Double totalAmount;

    @Column(nullable = false)
    private Integer totalAmountInPoints;

}
