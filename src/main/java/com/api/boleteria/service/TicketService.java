package com.api.boleteria.service;

import com.api.boleteria.dto.detail.TicketDetailDTO;
import com.api.boleteria.dto.request.TicketRequestDTO;
import com.api.boleteria.exception.AccessDeniedExceptionPeronalized;
import com.api.boleteria.exception.BadRequestException;
import com.api.boleteria.exception.NotFoundException;
import com.api.boleteria.model.Ticket;
import com.api.boleteria.model.Function;
import com.api.boleteria.model.User;
import com.api.boleteria.repository.ITicketRepository;
import com.api.boleteria.repository.IFunctionRepository;
import com.api.boleteria.repository.IUserRepository;
import com.api.boleteria.validators.TicketValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para gestionar operaciones relacionadas con tickets.
 */
@Service
@RequiredArgsConstructor
public class TicketService {

    private final ITicketRepository ticketRepository;
    private final IUserRepository userRepository;
    private final IFunctionRepository functionRepository;
    private final UserService userService;



    //-------------------------------SAVE--------------------------------//

    /**
     * Crea un Ticket para una función específica.
     *
     * Este método realiza las siguientes operaciones de manera transaccional:
     * - Verifica disponibilidad de entradas.
     * - Reduce la capacidad disponible de la función.
     * - Crea el ticket correspondientes y lo asocia al usuario.
     *
     * Si alguna de estas operaciones falla, la transacción se revierte y no se guarda ningún cambio.
     *
     * @param dto DTO con los datos de la compra.
     * @return TicketDetailDTO con el ticket generado.
     * @throws NotFoundException si no se encuentra la función o la tarjeta del usuario.
     * @throws BadRequestException si no hay capacidad suficiente o fondos en la tarjeta.
     */
    @Transactional
    public Ticket createTicketFromPayment(User user, TicketRequestDTO dto) {
        TicketValidator.validateFields(dto);

        Function function = functionRepository.findById(dto.getFunctionId())
                .orElseThrow(() -> new NotFoundException("Función no encontrada."));

        if (!function.getCinema().getEnabled()) {
            throw new BadRequestException("La sala asociada a la función está inhabilitada.");
        }

        // Validar capacidad disponible
        TicketValidator.validateCapacity(function, dto.getQuantity());

        Ticket ticket = new Ticket();
        ticket.setUser(user);
        ticket.setFunction(function);
        ticket.setUnitPrice(dto.getUnitPrice());
        ticket.setQuantity(dto.getSeats().size());
        ticket.setTotalAmount(dto.getUnitPrice().multiply(BigDecimal.valueOf(dto.getSeats().size())));
        ticket.setPurchaseDateTime(LocalDateTime.now());
        // Copia la lista en vez de reusar la misma instancia que ya está gestionada por
        // Hibernate como colección de Payment.seats: si Ticket.seats y Payment.seats apuntan
        // al mismo objeto, Hibernate tira "Found shared references to a collection" al flushear.
        ticket.setSeats(new ArrayList<>(dto.getSeats()));

        // Actualiza la capacidad disponible
        function.setAvailableCapacity(function.getAvailableCapacity() - dto.getSeats().size());

        functionRepository.save(function);
        ticketRepository.save(ticket);

        return ticket;
    }



    //-------------------------------FIND--------------------------------//

    /**
     * Obtiene todos los tickets asociados al usuario autenticado.
     *
     * @return Lista de TicketDetailDTO con los tickets del usuario.
     * @throws NotFoundException si el usuario no tiene tickets asociados.
     */
    public List<TicketDetailDTO> findTicketsFromAuthenticatedUser() {
        User user = userService.findAuthenticatedUser();

        List<TicketDetailDTO> tickets = user.getTickets().stream()
                .map(this::mapToDetailDTO)
                .toList();
        if (tickets.isEmpty()) {
            throw new NotFoundException("El usuario " + user.getUsername() + " no tiene tickets asociados.");
        }

        return tickets;
    }



    /**
     * Obtiene un ticket específico por su ID, validando que el ID sea válido y que el ticket pertenezca al usuario autenticado.
     *
     * @param ticketId ID del ticket a buscar.
     * @return TicketDetailDTO con los datos del ticket.
     * @throws IllegalArgumentException si el ID del ticket es nulo o menor o igual a cero.
     * @throws NotFoundException si no se encuentra un ticket con el ID especificado.
     * @throws AccessDeniedExceptionPeronalized si el ticket no pertenece al usuario autenticado.
     */
    public TicketDetailDTO findTicketById(Long ticketId) {
        User user = userService.findAuthenticatedUser();
        TicketValidator.validateTicketId(ticketId);
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("No se encontró el ticket con ID: " + ticketId));

        if (!ticket.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedExceptionPeronalized("No tiene permiso para ver este ticket.");
        }

        return mapToDetailDTO(ticket);
    }



    //-------------------------------MAPS--------------------------------//

    /**
     * Convierte una entidad Ticket a un DTO detallado.
     * @param ticket entidad de ticket a convertir
     * @return TicketDetailDTO con los datos relevantes del ticket
     */
    public TicketDetailDTO mapToDetailDTO(Ticket ticket) {
        return new TicketDetailDTO(
                ticket.getId(),
                ticket.getFunction().getMovieName(),
                ticket.getFunction().getCinema().getId(),
                ticket.getFunction().getId(),
                ticket.getPurchaseDateTime().toLocalDate().toString(),
                ticket.getPurchaseDateTime().toLocalTime().toString(),
                ticket.getUnitPrice(),
                ticket.getTotalAmount(),
                ticket.getQuantity(),
                ticket.getSeats()
        );
    }



    /**
     * Mapea los datos necesarios para crear una entidad Ticket a partir de un usuario y una función.
     *
     * @param user     Usuario que compra el ticket.
     * @param function Función asociada al ticket.
     * @param dto DTO con datos de la compra
     * @return Nueva instancia de Ticket con los datos seteados.
     */
    public Ticket mapToEntity(User user, Function function, TicketRequestDTO dto) {
        Ticket ticket = new Ticket();
        ticket.setUnitPrice(dto.getUnitPrice());
        ticket.setTotalAmount(dto.getUnitPrice().multiply(BigDecimal.valueOf(dto.getSeats().size())));
        ticket.setQuantity(dto.getSeats().size());
        ticket.setPurchaseDateTime(LocalDateTime.now());
        ticket.setUser(user);
        ticket.setFunction(function);
        // Copia la lista en vez de reusar la misma instancia que ya está gestionada por
        // Hibernate como colección de Payment.seats: si Ticket.seats y Payment.seats apuntan
        // al mismo objeto, Hibernate tira "Found shared references to a collection" al flushear.
        ticket.setSeats(new ArrayList<>(dto.getSeats()));
        return ticket;
    }




}
