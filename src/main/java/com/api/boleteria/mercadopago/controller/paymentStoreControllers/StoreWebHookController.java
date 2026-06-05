package com.api.boleteria.mercadopago.controller.paymentStoreControllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import com.api.boleteria.mercadopago.service.PaymentStoreService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/paymentStore/webhooks")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200"})
public class StoreWebHookController {
    
    private final PaymentStoreService paymentStoreService;

    
    @PostMapping("/notification")
    public ResponseEntity<String> handleNotification(@RequestBody Map<String, Object> payload) {
        try {
            // El campo "data.id" contiene el ID del pago de Mercado Pago
            if (payload.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) payload.get("data");
                String mpPaymentId = String.valueOf(data.get("id"));

                // Llamamos al servicio para procesar el pago
                paymentStoreService.processWebhookNotification(mpPaymentId);
            }

            return ResponseEntity.ok("Notification received");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error processing notification: " + e.getMessage());
        }
    }


    //-------------------------------REDIRECT--------------------------------//
    // Estas URLs deben coincidir con las rutas que esten definidas en Angular.

    /**
     * Redirect endpoint triggered when Mercado Pago confirms a successful payment.
     * <p>
     * It redirects the user to the Angular frontend’s success page.
     * </p>
     *
     * @return a {@link RedirectView} pointing to {@code /payment-success} in the frontend.
     */
    @GetMapping("/success")
    public RedirectView success() {
        // Redirige al frontend a la página de éxito
        return new RedirectView("http://localhost:4200/");
    }



    /**
     * Redirect endpoint triggered when a payment fails in Mercado Pago.
     * <p>
     * It redirects the user to the Angular frontend’s failure page.
     * </p>
     *
     * @return a {@link RedirectView} pointing to {@code /payment-failure} in the frontend.
     */
    @GetMapping("/failure")
    public RedirectView failure() {
        // Redirige al frontend a la página de fallo
        return new RedirectView("http://localhost:4200/");
    }



    /**
     * Redirect endpoint triggered when a payment remains pending in Mercado Pago.
     * <p>
     * It redirects the user to the Angular frontend’s pending page.
     * </p>
     *
     * @return a {@link RedirectView} pointing to {@code /payment-pending} in the frontend.
     */
    @GetMapping("/pending")
    public RedirectView pending() {
        // Redirige al frontend a la página pendiente
        return new RedirectView("http://localhost:4200/");
    }
}
