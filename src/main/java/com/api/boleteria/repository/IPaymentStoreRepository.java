package com.api.boleteria.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.boleteria.model.PaymentStore;

public interface IPaymentStoreRepository extends JpaRepository <PaymentStore, Long> {
    Optional<PaymentStore> findByMpPaymentId(String mpPaymentId);
}
