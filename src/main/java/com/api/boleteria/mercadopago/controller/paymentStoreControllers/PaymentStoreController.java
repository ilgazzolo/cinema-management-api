package com.api.boleteria.mercadopago.controller.paymentStoreControllers;

import com.api.boleteria.mercadopago.dto.paymentStore.PaymentStoreRequestDTO;
import com.api.boleteria.mercadopago.dto.paymentStore.PaymentStoreResponseDTO;
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

    //-------------------------------CREATE--------------------------------//

    /**
     * Endpoint that creates a new payment preference in Mercado Pago based on client data.
     * <p>
     * It validates the incoming request, delegates preference creation to the
     * {@link PaymentService}, and returns a response with the URL to initiate
     * the payment in the Mercado Pago sandbox environment.
     * </p>
     * <p>
     * Only users with role {@code CLIENT} are authorized to access this operation.
     * </p>
     *
     * @param dto the {@link PaymentRequestDTO} containing product details,
     *            quantity, price, and user information for the payment request.
     * @return a {@link ResponseEntity} containing a {@link PaymentResponseDTO} with
     *         the generated preference data, or an appropriate error message if creation fails.
     */

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
            return ResponseEntity.internalServerError().body("Error generating payment preference.");
        }
    }


}





