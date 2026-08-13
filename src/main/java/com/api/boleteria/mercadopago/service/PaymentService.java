package com.api.boleteria.mercadopago.service;

import com.api.boleteria.dto.request.TicketRequestDTO;
import com.api.boleteria.exception.BadRequestException;
import com.api.boleteria.exception.NotFoundException;
import com.api.boleteria.log.PaymentLog;
import com.api.boleteria.mercadopago.dto.PaymentRequestDTO;
import com.api.boleteria.mercadopago.dto.PaymentResponseDTO;
import com.api.boleteria.model.*;
import com.api.boleteria.model.enums.StatusPayment;
import com.api.boleteria.repository.*;
import com.api.boleteria.service.TicketService;
import com.api.boleteria.service.UserService;
import com.api.boleteria.validators.TicketValidator;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.resources.preference.Preference;
import com.mercadopago.MercadoPagoConfig;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class responsible for handling all payment-related operations and
 * communication with the Mercado Pago API. It manages payment preference creation,
 * payment status synchronization through webhooks, and ticket generation upon
 * successful payments. It also keeps a detailed log of all payment events for
 * auditing and debugging purposes.
 *
 * La butaca, el ticket y los puntos del usuario recién se confirman cuando Mercado Pago
 * notifica por webhook que el pago fue realmente aprobado (ver {@link #processWebhookNotification}).
 * Al crear la preferencia sólo se valida disponibilidad; nada queda reservado en firme
 * todavía, así que si el pago falla o se abandona el checkout la butaca sigue libre.
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final IPaymentRepository paymentRepository;
    private final IPaymentLogRepository paymentLogRepository;
    private final ITicketRepository ticketRepository;
    private final IFunctionRepository functionRepository;
    private final ISeatRepository seatRepository;
    private final IUserRepository userRepository;
    private final TicketService ticketService;
    private final UserService userService;



    //-------------------------------SAVE--------------------------------//



    /**
     * Creates a new payment preference in Mercado Pago using the provided payment data.
     * Valida que la función y las butacas elegidas existan y sigan disponibles, guarda un
     * {@link Payment} local en estado {@code PENDING} (usado como external_reference) y crea
     * la preferencia en Mercado Pago. No ocupa butacas, no genera el ticket ni suma puntos:
     * eso sólo ocurre si el pago es aprobado (ver {@link #processWebhookNotification}).
     *
     * @param dto The {@link PaymentRequestDTO} containing information such as title,
     *            description, quantity, price, function ID, and selected seats.
     * @return A {@link PaymentResponseDTO} with the generated preference ID and the sandbox
     *         URL to redirect the user for payment.
     * @throws NotFoundException   si la función o alguna butaca enviada no existen.
     * @throws BadRequestException si no hay capacidad suficiente o alguna butaca ya está ocupada.
     * @throws RuntimeException    si ocurre un error de comunicación con Mercado Pago.
     */
    @Transactional
    public PaymentResponseDTO createPreference(PaymentRequestDTO dto) {
        try {

            // Inicializar SDK de Mercado Pago
            MercadoPagoConfig.setAccessToken(System.getenv("MP_ACCESS_TOKEN"));
            // Guarda URL de ngrok
            String tunel = System.getenv("MIAPP_NGROKURL");

            // Obtener usuario autenticado
            User user = userService.findAuthenticatedUser();

            Function function = functionRepository.findById(dto.getFunctionId())
                    .orElseThrow(() -> new NotFoundException("Function with ID: " + dto.getFunctionId() + " not found"));

            TicketValidator.validateCapacity(function, dto.getQuantity());

            // Validar que las butacas elegidas existan y sigan libres. La ocupación real
            // se confirma recién en el webhook, cuando se sabe que el pago fue aprobado.
            List<Seat> seatsRequested = seatRepository.findByFunctionId(function.getId()).stream()
                    .filter(seat -> dto.getSeats().contains("R" + seat.getSeatRowNumber() + "C" + seat.getSeatColumnNumber()))
                    .toList();

            if (seatsRequested.size() != dto.getSeats().size()) {
                throw new NotFoundException("Una o más butacas seleccionadas no existen para esta función.");
            }
            if (seatsRequested.stream().anyMatch(Seat::getOccupied)) {
                throw new BadRequestException("Una o más butacas seleccionadas ya no están disponibles.");
            }

            // Crear y guardar Payment local primero (para obtener ID y usarlo como
            // external_reference). Queda en PENDING hasta que el webhook confirme el pago real.
            Payment payment = new Payment();
            payment.setUserId(user.getId());
            payment.setUserEmail(user.getEmail());
            payment.setQuantity(dto.getSeats().size());
            payment.setDate(LocalDateTime.now());
            payment.setAmount(dto.getUnitPrice().multiply(BigDecimal.valueOf(dto.getSeats().size())));
            payment.setStatus(StatusPayment.PENDING);
            payment.setSeats(dto.getSeats());
            payment.setFunction(function);
            payment.prePersist();

            // Persistir pago local
            paymentRepository.save(payment);

            // Armar item
            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .title(dto.getTitle())
                    .description(dto.getDescription())
                    .quantity(dto.getSeats().size())
                    .currencyId("ARS")
                    .unitPrice(dto.getUnitPrice())
                    .build();

            // Back URLs
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(tunel + "/api/payments/webhooks/success")
                    .pending(tunel + "/api/payments/webhooks/pending")
                    .failure(tunel + "/api/payments/webhooks/failure")
                    .build();

            // Crear preferencia con external_reference = ID del Payment local
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(List.of(itemRequest))
                    .backUrls(backUrls)
                    .notificationUrl(tunel + "/api/payments/webhooks/notification")
                    .autoReturn("approved")
                    .externalReference(payment.getId().toString())
                    .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            // Guardar preferenceId en el Payment local
            payment.setPreferenceId(preference.getId());
            paymentRepository.save(payment);

            // Log del intento
            PaymentLog log = new PaymentLog();
            log.setStatus("PREFERENCE_CREATED");
            log.setUserEmail(dto.getUserEmail());
            log.setTimestamp(LocalDateTime.now());
            paymentLogRepository.save(log);

            // Respuesta
            return mapToResponse(preference);

        } catch (BadRequestException | NotFoundException e) {
            throw e;
        } catch (MPApiException apiException) {
            System.out.println("Status Code: " + apiException.getStatusCode());
            System.out.println("Error Details: " + apiException.getApiResponse().getContent());
            apiException.printStackTrace();
            throw new RuntimeException("Error generating payment preference.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error creating payment preference: " + e.getMessage());
        }
    }



    //-------------------------------UPDATE--------------------------------//


    /**
     * Busca (o crea) el {@link Payment} local asociado a un pago de Mercado Pago a partir de su
     * ID de Mercado Pago, y sincroniza su estado con el informado por la plataforma.
     * <p>
     * Se usa como respaldo cuando la notificación no trae un external_reference utilizable;
     * el camino principal del webhook busca el Payment por external_reference y llama
     * directamente a {@link #syncPaymentStatus} para evitar crear un registro duplicado.
     *
     * @param mpPaymentId The Mercado Pago payment ID.
     * @param mpStatus    The payment status received from Mercado Pago (e.g. "approved", "pending", "rejected").
     * @param userEmail   The payer’s email address associated with the payment.
     * @return The updated or newly created {@link Payment} reflecting the current status.
     */
    @Transactional
    public Payment updatePaymentStatus(String mpPaymentId, String mpStatus, String userEmail) {
        Payment payment = paymentRepository.findByMpPaymentId(mpPaymentId)
                .orElseGet(() -> {
                    Payment p = new Payment();
                    p.setMpPaymentId(mpPaymentId);
                    p.setUserEmail(userEmail);
                    p.setCreatedAt(LocalDateTime.now());
                    return p;
                });

        return syncPaymentStatus(payment, mpPaymentId, mpStatus, userEmail);
    }

    /**
     * Aplica el estado real informado por Mercado Pago sobre un {@link Payment} local ya
     * resuelto (encontrado por ID o por mpPaymentId) y deja constancia en el log de pagos.
     *
     * @param payment     Payment local a actualizar.
     * @param mpPaymentId ID del pago en Mercado Pago.
     * @param mpStatus    Estado informado por Mercado Pago.
     * @param userEmail   Email del pagador, si está disponible.
     * @return El Payment con el estado sincronizado y ya persistido.
     */
    private Payment syncPaymentStatus(Payment payment, String mpPaymentId, String mpStatus, String userEmail) {
        if (payment.getMpPaymentId() == null) {
            payment.setMpPaymentId(mpPaymentId);
        }
        if (payment.getUserEmail() == null && userEmail != null) {
            payment.setUserEmail(userEmail);
        }

        StatusPayment newStatusEnum;
        try {
            newStatusEnum = StatusPayment.valueOf(mpStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            newStatusEnum = StatusPayment.PENDING;
        }

        payment.setStatus(newStatusEnum);
        payment.preUpdate();

        if (payment.getAmount() == null) payment.setAmount(BigDecimal.ZERO);
        if (payment.getQuantity() == null) payment.setQuantity(1);

        paymentRepository.save(payment);

        PaymentLog log = new PaymentLog();
        log.setMpOperationId(mpPaymentId);
        log.setStatus(newStatusEnum.name());
        log.setUserEmail(userEmail);
        log.setTimestamp(LocalDateTime.now());
        paymentLogRepository.save(log);

        return payment;
    }



    /**
     * Processes incoming webhook notifications from Mercado Pago.
     * <p>
     * Busca el {@link Payment} local por external_reference y sincroniza su estado con el
     * informado por Mercado Pago (a diferencia de la versión anterior, esto pasa siempre,
     * no sólo cuando el Payment todavía no existía). Sólo si el estado sincronizado es
     * {@code APPROVED} se confirma la compra:
     * <ul>
     *   <li>Se revalida que las butacas sigan libres (protege contra ventas dobles si dos
     *       pagos por las mismas butacas llegaran a aprobarse casi en simultáneo)</li>
     *   <li>Se marcan esas butacas como ocupadas</li>
     *   <li>Se suman los puntos del usuario</li>
     *   <li>Se crea el ticket y se lo vincula al pago</li>
     * </ul>
     * Si el pago no fue aprobado, no se toca ninguna butaca ni se genera ticket: quedan
     * disponibles para que el mismo u otro usuario puedan volver a intentarlo.
     * <p>
     * Es idempotente: si Mercado Pago reenvía la misma notificación para un pago que ya
     * tiene un ticket vinculado, no se vuelve a procesar.
     *
     * @param mpPaymentId The Mercado Pago payment ID included in the webhook notification.
     */
    @Transactional
    public void processWebhookNotification(String mpPaymentId) {
        try {
            // SDK MP
            MercadoPagoConfig.setAccessToken(System.getenv("MP_ACCESS_TOKEN"));

            // Obtener pago real de MP
            PaymentClient paymentClient = new PaymentClient();
            com.mercadopago.resources.payment.Payment mpPayment =
                    paymentClient.get(Long.parseLong(mpPaymentId));

            String mpStatus = mpPayment.getStatus();
            String userEmail = (mpPayment.getPayer() != null) ? mpPayment.getPayer().getEmail() : null;

            System.out.println("Payment updated from webhook: " + mpPaymentId + " - " + mpStatus);

            // Vincular al Payment local usando external_reference y sincronizar su estado real
            Payment payment;
            String externalRef = mpPayment.getExternalReference();
            if (externalRef != null) {
                Long localPaymentId = Long.valueOf(externalRef);
                Payment localPayment = paymentRepository.findById(localPaymentId).orElse(null);
                payment = (localPayment != null)
                        ? syncPaymentStatus(localPayment, mpPaymentId, mpStatus, userEmail)
                        : updatePaymentStatus(mpPaymentId, mpStatus, userEmail);
            } else {
                payment = updatePaymentStatus(mpPaymentId, mpStatus, userEmail);
            }

            // El pago no fue aprobado (rechazado, pendiente, cancelado, etc.): no se ocupa
            // ninguna butaca, no se genera ticket ni se suman puntos.
            if (!StatusPayment.APPROVED.equals(payment.getStatus())) {
                return;
            }

            // Notificación duplicada de Mercado Pago sobre un pago ya procesado: no reprocesar.
            if (payment.getTicket() != null) {
                return;
            }

            if (payment.getFunction() == null) {
                System.err.println("Payment " + payment.getId() + " aprobado pero sin función asociada.");
                return;
            }

            // Buscar usuario por ID o email
            User user = null;
            if (payment.getUserId() != null) {
                user = userRepository.findById(payment.getUserId()).orElse(null);
            }
            if (user == null && userEmail != null) {
                user = userRepository.findByEmail(userEmail).orElse(null);
            }
            if (user == null) {
                System.err.println("Payment " + payment.getId() + " aprobado pero no se pudo resolver el usuario.");
                return;
            }

            Function function = payment.getFunction();
            List<String> selectedSeats = payment.getSeats();

            List<Seat> seatsToOccupy = seatRepository.findByFunctionId(function.getId()).stream()
                    .filter(seat -> selectedSeats.contains("R" + seat.getSeatRowNumber() + "C" + seat.getSeatColumnNumber()))
                    .toList();

            // Puede pasar si otra compra se adelantó a confirmar alguna de estas mismas butacas
            // mientras este pago estaba en proceso. No se vende dos veces la misma butaca:
            // se deja constancia para revisión y devolución manual.
            boolean seatsUnavailable = seatsToOccupy.size() != selectedSeats.size()
                    || seatsToOccupy.stream().anyMatch(Seat::getOccupied);
            if (seatsUnavailable) {
                System.err.println("No se pudo confirmar el pago " + mpPaymentId
                        + ": una o más butacas ya no están disponibles. Requiere revisión manual.");
                return;
            }

            seatsToOccupy.forEach(seat -> seat.setOccupied(true));
            seatRepository.saveAll(seatsToOccupy);

            // Sumar puntos recién ahora que el pago está confirmado
            int currentPoints = user.getPoints() == null ? 0 : user.getPoints();
            user.setPoints(currentPoints + selectedSeats.size() * 10);
            userRepository.save(user);

            BigDecimal unitPrice = (payment.getQuantity() != null && payment.getQuantity() > 0)
                    ? payment.getAmount().divide(BigDecimal.valueOf(payment.getQuantity()), 2, java.math.RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            TicketRequestDTO ticketDTO = new TicketRequestDTO();
            ticketDTO.setFunctionId(function.getId());
            ticketDTO.setQuantity(payment.getQuantity());
            ticketDTO.setUnitPrice(unitPrice);
            ticketDTO.setTotalAmount(payment.getAmount());
            ticketDTO.setSeats(payment.getSeats());

            Ticket ticket = ticketService.createTicketFromPayment(user, ticketDTO);

            payment.setTicket(ticket);
            paymentRepository.save(payment);

            System.out.println("Ticket created and linked to payment ID: " + mpPaymentId);

        } catch (MPApiException e) {
            System.out.println("Error from Mercado Pago API: " + e.getApiResponse().getContent());
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    //-------------------------------MAPS--------------------------------//


    /**
     * Maps a Mercado Pago {@link Preference} object to a {@link PaymentResponseDTO}.
     * This method extracts the preference ID and sandbox initialization URL
     * (used for testing environments) to build the response object.
     *
     * @param preference The Mercado Pago {@link Preference} generated after creating a payment preference.
     * @return A {@link PaymentResponseDTO} containing the preference ID and the sandbox payment URL.
     */
    public PaymentResponseDTO mapToResponse(Preference preference) {
        return new PaymentResponseDTO(
                preference.getId(),
                preference.getSandboxInitPoint() // preference.getInitPoint() para produccion
        );
    }


}
