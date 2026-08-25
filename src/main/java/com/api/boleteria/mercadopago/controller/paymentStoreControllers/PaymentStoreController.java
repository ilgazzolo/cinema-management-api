package com.api.boleteria.mercadopago.controller.paymentStoreControllers;

import com.api.boleteria.mercadopago.dto.paymentStore.PaymentStoreRequestDTO;
import com.api.boleteria.mercadopago.dto.paymentStore.PaymentStoreResponseDTO;
import com.api.boleteria.mercadopago.dto.paymentStore.PaymentStorePointsResponseDTO;
import com.api.boleteria.mercadopago.service.PaymentStoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/paymentStore")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200"})
public class PaymentStoreController {

    private final PaymentStoreService paymentStoreService;

    @PostMapping("/points/{storeOrderId}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<PaymentStorePointsResponseDTO> payWithPoints(
            @PathVariable Long storeOrderId) {
        return ResponseEntity.ok(paymentStoreService.payWithPoints(storeOrderId));
    }

    //-------------------------------CREATE--------------------------------//



    @PostMapping("/create")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<?> createPreference(@Valid @RequestBody PaymentStoreRequestDTO dto) {
        try{
            PaymentStoreResponseDTO response = paymentStoreService.createStorePreference(dto);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }


}

