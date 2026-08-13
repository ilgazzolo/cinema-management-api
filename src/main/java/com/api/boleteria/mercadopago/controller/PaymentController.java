package com.api.boleteria.mercadopago.controller;


import com.api.boleteria.mercadopago.dto.PaymentRequestDTO;
import com.api.boleteria.mercadopago.dto.PaymentResponseDTO;
import com.api.boleteria.mercadopago.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


/**
 * REST controller that manages operations related to payments within the system.
 * <p>
 * It provides endpoints for initiating payment processes through Mercado Pago,
 * allowing authenticated users to create payment preferences that will redirect
 * them to the Mercado Pago checkout page.
 * </p>
 * <p>
 * This controller acts as an entry point for the frontend (Angular)
 * and delegates all business logic to the {@link PaymentService}.
 * </p>
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200"})
public class PaymentController {

    private final PaymentService paymentService;

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
    public ResponseEntity<PaymentResponseDTO> createPreference(@Valid @RequestBody PaymentRequestDTO dto) {
        PaymentResponseDTO response = paymentService.createPreference(dto);
        return ResponseEntity.ok(response);
    }


}




