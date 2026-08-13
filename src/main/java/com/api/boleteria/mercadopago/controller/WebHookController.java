package com.api.boleteria.mercadopago.controller;

import com.api.boleteria.mercadopago.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

/**
 * REST controller that handles webhook notifications and redirection endpoints
 * related to Mercado Pago payment events.
 * <p>
 * This controller serves two main purposes:
 * <ul>
 *   <li>Receives asynchronous webhook notifications from Mercado Pago after a payment event occurs.</li>
 *   <li>Manages redirect endpoints for the frontend (Angular) when a payment
 *       is completed, fails, or remains pending.</li>
 * </ul>
 * </p>
 * <p>
 * It delegates business logic and payment status handling to the {@link PaymentService}.
 * </p>
 */
@RestController
@RequestMapping("/api/payments/webhooks")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200"})
public class WebHookController {

    private final PaymentService paymentService;

    /**
     * Receives and processes webhook notifications sent by Mercado Pago.
     * <p>
     * When Mercado Pago sends a POST request with payment information,
     * this endpoint extracts the payment ID and delegates processing
     * to the {@link PaymentService}, which updates the payment status
     * and triggers ticket creation if necessary.
     * </p>
     *
     * @param payload a {@link Map} containing the notification data sent by Mercado Pago,
     *                where {@code data.id} represents the Mercado Pago payment ID.
     * @return a {@link ResponseEntity} indicating whether the notification was successfully received or if an error occurred.
     */
    @PostMapping("/notification")
    public ResponseEntity<String> handleNotification(@RequestBody Map<String, Object> payload) {
        try {
            // El campo "data.id" contiene el ID del pago de Mercado Pago
            if (payload.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) payload.get("data");
                String mpPaymentId = String.valueOf(data.get("id"));

                // Llamamos al servicio para procesar el pago
                paymentService.processWebhookNotification(mpPaymentId);
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
     * Mercado Pago incluye el {@code payment_id} en esta misma URL de retorno, así que
     * aprovechamos para intentar procesar el pago acá también, como respaldo del webhook
     * asíncrono ({@link #handleNotification}). El webhook puede demorar, fallar o no llegar
     * nunca (por ejemplo, en desarrollo detrás de un túnel de ngrok), y sin esto la compra
     * quedaba "aprobada" en Mercado Pago pero la butaca seguía libre y no se generaba el
     * ticket. {@link PaymentService#processWebhookNotification} es idempotente, así que no
     * hay problema si el webhook real también llega y lo procesa de nuevo.
     * </p>
     *
     * @param paymentId    ID del pago en Mercado Pago (param {@code payment_id}).
     * @param collectionId Alias legado del mismo ID (param {@code collection_id}), usado como
     *                     respaldo si {@code payment_id} no viene presente.
     * @return a {@link RedirectView} pointing to the Angular frontend.
     */
    @GetMapping("/success")
    public RedirectView success(
            @RequestParam(value = "payment_id", required = false) String paymentId,
            @RequestParam(value = "collection_id", required = false) String collectionId
    ) {
        String mpPaymentId = (paymentId != null) ? paymentId : collectionId;

        if (mpPaymentId != null) {
            try {
                paymentService.processWebhookNotification(mpPaymentId);
            } catch (Exception e) {
                // No dejamos que un problema al confirmar el pago le rompa el redirect al
                // usuario: el webhook asíncrono todavía puede llegar y confirmarlo igual.
                e.printStackTrace();
            }
        }

        // Redirige al frontend al perfil, con un flag para que muestre la confirmación de compra
        return new RedirectView("http://localhost:4200/profile?compra=exito");
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
