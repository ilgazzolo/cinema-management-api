package com.api.boleteria.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.boleteria.model.OrderItems;

public interface IOrderItemsRepository extends JpaRepository <OrderItems, Long> {
    
}   

