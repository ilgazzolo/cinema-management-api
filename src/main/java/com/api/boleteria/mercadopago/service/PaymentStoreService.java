package com.api.boleteria.mercadopago.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;


import com.api.boleteria.exception.NotFoundException;
import com.api.boleteria.log.PaymentLog;
import com.mercadopago.MercadoPagoConfig;
import com.api.boleteria.mercadopago.dto.PaymentRequestDTO;
import com.api.boleteria.mercadopago.dto.PaymentResponseDTO;
import com.api.boleteria.mercadopago.dto.paymentStore.PaymentStoreRequestDTO;
import com.api.boleteria.mercadopago.dto.paymentStore.PaymentStoreResponseDTO;
import com.api.boleteria.model.OrderItems;
import com.api.boleteria.model.PaymentStore;
import com.api.boleteria.model.StoreOrder;
import com.api.boleteria.model.Ticket;
import com.api.boleteria.model.enums.StatusPayment;
import com.api.boleteria.repository.IPaymentLogRepository;
import com.api.boleteria.repository.IPaymentStoreRepository;
import com.api.boleteria.repository.IStoreOrderRepository;
import com.api.boleteria.repository.IUserRepository;
import com.api.boleteria.service.TicketService;
import com.api.boleteria.service.UserService;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.resources.preference.Preference;
import com.api.boleteria.model.*;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentStoreService {
    private final IPaymentStoreRepository paymentStoreRepository;
    private final IPaymentLogRepository paymentLogRepository;
    private final IUserRepository userRepository;
    private final UserService userService;
    private final IStoreOrderRepository storeOrderRepository;



    //-------------------------------SAVE--------------------------------//



    /**
     * Creates a new payment preference in Mercado Pago using the provided payment data.
     * The method builds the preference request, sets the item details, back URLs,
     * notification URL, and auto-return configuration. It then sends the request to
     * Mercado Pago to obtain a payment preference and stores the local record in the database.
     *
     * @param dto The {@link PaymentRequestDTO} containing information such as title,
     *            description, quantity, price, function ID, and selected seats.
     * @return A {@link PaymentResponseDTO} with the generated preference ID and the sandbox
     *         URL to redirect the user for payment.
     * @throws RuntimeException if any error occurs during preference creation or API communication.
     */
    @Transactional
public PaymentStoreResponseDTO createStorePreference(PaymentStoreRequestDTO dto) {
    try {

        // Inicializar SDK de Mercado Pago
            MercadoPagoConfig.setAccessToken(System.getenv("MP_ACCESS_TOKEN"));
            // Guarda URL de ngrok
            String tunel = System.getenv("MIAPP_NGROKURL");
            System.out.println("NGROK URL: " + tunel);
            System.out.println("TOKEN: " + System.getenv("MP_ACCESS_TOKEN"));


        // 2. Obtener usuario autenticado
        com.api.boleteria.model.User user = userService.findAuthenticatedUser();

        // 3. Sumar puntos acumulados por la compra de la tienda
        // Solucionamos la línea que estaba incompleta usando los puntos del DTO
        int puntosActuales = user.getPoints() == null ? 0 : user.getPoints();
        user.setPoints(puntosActuales + dto.getTotalAmountInPoints());
        userRepository.save(user);

        // 4. Crear y guardar el registro del Pago Local (adaptado a Tienda)
        PaymentStore payment = new PaymentStore();
        payment.setUserId(user.getId());
        payment.setUserEmail(user.getEmail());
        payment.setDate(LocalDateTime.now());
        payment.setAmount(dto.getTotalAmount()); // Monto final calculado en la orden
        payment.setStatus(StatusPayment.PENDING); // Lo ideal es que empiece PENDING hasta que MP apruebe

        if (dto.getStoreOrderId() != null) {
            StoreOrder order = storeOrderRepository.findById(dto.getStoreOrderId())
                    .orElseThrow(() -> new NotFoundException("StoreOrder no encontrada con ID: " + dto.getStoreOrderId()));
            payment.setStoreOrder(order);
        }
        
        // Calculamos la cantidad total sumando las cantidades de cada producto
        int cantidadTotal = dto.getItems().stream()
                .mapToInt(item -> item.quantity())
                .sum();
        payment.setQuantity(cantidadTotal);

        // Persistir el pago local inicial para generar el ID transaccional
        paymentStoreRepository.save(payment);

        // 5. Armar la lista de ítems dinámicos para Mercado Pago
        List<PreferenceItemRequest> mpItems = dto.getItems().stream()
                .map(item -> PreferenceItemRequest.builder()
                        .id(String.valueOf(item.id()))
                        .title(item.productName())
                        .quantity(item.quantity())
                        .currencyId("ARS")
                        .unitPrice(java.math.BigDecimal.valueOf(item.historicalPrice()))
                        .build())
                .collect(Collectors.toList());

        // 6. Configurar las URLs de Retorno (Back URLs)
        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(tunel + "/api/paymentStore/webhooks/success")
                .pending(tunel + "/api/paymentStore/webhooks/pending")
                .failure(tunel + "/api/paymentStore/webhooks/failure")
                .build();

        // 7. Crear la Preferencia de Mercado Pago
        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(mpItems) // Pasamos la lista completa de productos mapeados
                .backUrls(backUrls)
                .notificationUrl(tunel + "/api/paymentStore/webhooks/notification")
                .autoReturn("approved")
                .externalReference(payment.getId().toString()) // Clave para reconciliar en el webhook
                .build();

        PreferenceClient client = new PreferenceClient();
        Preference preference = client.create(preferenceRequest);

        // 8. Actualizar el pago local con el preferenceId obtenido
        payment.setPreferenceId(preference.getId());
        paymentStoreRepository.save(payment);

        // 9. Registrar el log de auditoría
        PaymentLog log = new PaymentLog();
        log.setStatus("STORE_PREFERENCE_CREATED");
        log.setUserEmail(user.getEmail());
        log.setTimestamp(LocalDateTime.now());
        paymentLogRepository.save(log);

        // 10. Retornar la respuesta al frontend de Angular
        return mapToResponse(preference);

    } catch (MPApiException apiException) {
        System.out.println("Status Code: " + apiException.getStatusCode());
        System.out.println("Error Details: " + apiException.getApiResponse().getContent());
        apiException.printStackTrace();
        throw new RuntimeException("Error generating store payment preference.");
    } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Error creating store payment preference: " + e.getMessage());
    }
    }


    //-------------------------------UPDATE--------------------------------//


    /**
     * Updates or creates a {@link Payment} record in the database based on data received
     * from Mercado Pago. It ensures that the payment status, user information, and logs
     * remain consistent with the latest update from the platform.
     *
     * @param mpPaymentId The Mercado Pago payment ID.
     * @param mpStatus    The payment status received from Mercado Pago (e.g. "approved", "pending", "rejected").
     * @param userEmail   The payer’s email address associated with the payment.
     * @return The updated or newly created {@link Payment} reflecting the current status.
     */
    @Transactional
    public PaymentStore updatePaymentStatus(String mpPaymentId, String mpStatus, String userEmail) {
        PaymentStore payment = paymentStoreRepository.findByMpPaymentId(mpPaymentId)
                .orElseGet(() -> {
                    PaymentStore p = new PaymentStore();
                    p.setMpPaymentId(mpPaymentId);
                    p.setUserEmail(userEmail);
                    p.setCreatedAt(LocalDateTime.now());
                    return p;
                });

        // Asegurar que queda seteado el mpPaymentId
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

        paymentStoreRepository.save(payment);

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
     * This method retrieves payment details from the Mercado Pago API, updates the
     * local payment record, and logs the notification event. If the payment is approved,
     * it also performs the following actions:
     * <ul>
     *   <li>Updates seat availability for the related function</li>
     *   <li>Decreases the function’s available capacity</li>
     *   <li>Creates and links a new ticket to the payment</li>
     * </ul>
     * Any errors are logged for debugging and consistency tracking.
     *
     * @param mpPaymentId The Mercado Pago payment ID included in the webhook notification.
     */
    @Transactional
    public void processWebhookNotification(String mpPaymentId) {
        try {
            // 1. Inicializar SDK de Mercado Pago
            com.mercadopago.MercadoPagoConfig.setAccessToken(System.getenv("MP_ACCESS_TOKEN"));

            // 2. Obtener pago real de la API de Mercado Pago
            PaymentClient paymentClient = new PaymentClient();
            com.mercadopago.resources.payment.Payment mpPayment = 
                    paymentClient.get(Long.parseLong(mpPaymentId));

            String mpStatus = mpPayment.getStatus();
            System.out.println("Store Payment updated from webhook: " + mpPaymentId + " - " + mpStatus);

            // 3. Vincular al PaymentStore local usando external_reference
            PaymentStore payment;
            String externalRef = mpPayment.getExternalReference();
        
            if (externalRef != null) {
                Long localPaymentId = Long.valueOf(externalRef);
                // Buscamos el registro de pago de la tienda que creamos antes
                payment = paymentStoreRepository.findById(localPaymentId)
                        .orElseThrow(() -> new NotFoundException("PaymentStore local no encontrado con ID: " + localPaymentId));
            } else {
                System.out.println("El pago de Mercado Pago no trajo external_reference. ID: " + mpPaymentId);
                return; // Cortamos la ejecución si no sabemos a qué pago local pertenece
            }

            // 4. Actualizar el ID de Mercado Pago y el estado en nuestra base de datos
            if (payment.getMpPaymentId() == null) {
                payment.setMpPaymentId(mpPaymentId);
            }

            // Mapeamos el estado de Mercado Pago a tu Enum local
            if ("approved".equalsIgnoreCase(mpStatus)) {
                payment.setStatus(StatusPayment.APPROVED);
            } else if ("pending".equalsIgnoreCase(mpStatus) || "in_process".equalsIgnoreCase(mpStatus)) {
                payment.setStatus(StatusPayment.PENDING);
            } else {
                payment.setStatus(StatusPayment.REJECTED);
            }

            paymentStoreRepository.save(payment);

            // 5. Acciones post-aprobación para la TIENDA
            if (StatusPayment.APPROVED.equals(payment.getStatus())) {
            
                // Traemos la orden asociada al pago mediante la relación @OneToOne
                StoreOrder order = payment.getStoreOrder();
            
                if (order != null) {
                    // Acá podés actualizar el estado de la orden a "Pagada" o "Completada"
                    // Ejemplo (descomentar y ajustar según cómo se llame el atributo de estado en tu clase StoreOrder):
                    order.setStatus(StatusPayment.APPROVED);
                    storeOrderRepository.save(order);
                
                    System.out.println("Store Order vinculada y actualizada con éxito para el pago: " + mpPaymentId);
                } else {
                    System.out.println("El PaymentStore fue aprobado, pero no tiene una StoreOrder vinculada. ID local: " + payment.getId());
                }
            }

        } catch (MPApiException e) {
            System.out.println("Error from Mercado Pago API: " + e.getApiResponse().getContent());
            e.printStackTrace();
        } catch (NotFoundException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Creates a new {@link StoreOrder} after a successful payment.
     * <p>
     * This method validates the user, retrieves the seats selected for the function,
     * marks those seats as occupied, and prepares the necessary data to delegate
     * the ticket creation to the {@link TicketService}.
     *
     * @param username The username of the purchaser (may be null if userId is used).
     * @param userId   The ID of the user who completed the payment.
     * @param function The function (movie show) for which the ticket is being generated.
     * @param seats    A list of seat codes in the format "R{row}C{column}" (e.g., "R1C5").
     * @param quantity The number of seats purchased.
     * @param mount    The total amount paid for the purchase.
     * @return The generated {@link Ticket}, or {@code null} if validation fails.
     * @throws NotFoundException If the user or seats cannot be found.
     */


    @Transactional
    public StoreOrder crearStoreOrder(com.api.boleteria.model.User user, PaymentStoreRequestDTO dto) {
        // 1. Validaciones
        if (user == null) {
            throw new IllegalArgumentException("El usuario es requerido para crear la orden.");
        }
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new IllegalArgumentException("La orden debe contener al menos un producto.");
        }

        // 2. Instanciar la entidad StoreOrder exacta a tu imagen
        StoreOrder order = new StoreOrder();
        order.setUser(user);
        order.setStatus(StatusPayment.PENDING); // Usando tu Enum StatusPayment
        order.setCreatedAt(LocalDateTime.now()); // Usando LocalDateTime como pide tu entidad
        order.setTotalAmount(dto.getTotalAmount().doubleValue());
        order.setTotalAmountInPoints(dto.getTotalAmountInPoints());

        // 3. Mapear los items del DTO hacia tu entidad OrderItems
        List<OrderItems> orderItemsList = dto.getItems().stream()
                .map(itemDTO -> {
                    OrderItems item = new OrderItems();
                    item.setStoreOrder(order); // Relación bidireccional clave para el CascadeType.ALL
                
                    // NOTA: Si necesitás la entidad Product real, deberías buscarla acá:
                    // Product product = productRepository.findById(itemDTO.id()).orElseThrow(...);
                    // item.setProduct(product);
                
                    item.setQuantity(itemDTO.quantity());
                    // Asumiendo que tenés estos campos en OrderItems:
                    item.setHistoricalPrice(itemDTO.historicalPrice());
                    item.setHistoricalUnitCost(itemDTO.historicalUnitCost());
                    item.setHistoricalPriceInPoints(itemDTO.historicalPriceInPoints());
                
                    return item;
                })
                .toList();

            order.setItems(orderItemsList);

            // 4. Guardar en BD. Gracias al cascade = CascadeType.ALL, guarda la orden y los items juntos.
            return storeOrderRepository.save(order);
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
    public PaymentStoreResponseDTO mapToResponse(Preference preference) {
        return new PaymentStoreResponseDTO(
                preference.getId(),
                preference.getSandboxInitPoint() // preference.getInitPoint() para produccion
        );
    }

}
