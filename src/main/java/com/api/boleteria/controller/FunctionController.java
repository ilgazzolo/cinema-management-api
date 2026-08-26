package com.api.boleteria.controller;

import com.api.boleteria.dto.detail.FunctionDetailDTO;
import com.api.boleteria.dto.list.FunctionListDTO;
import com.api.boleteria.dto.list.SeatListDTO;
import com.api.boleteria.dto.request.FunctionRequestDTO;
import com.api.boleteria.model.enums.ScreenType;
import com.api.boleteria.service.FunctionService;
import com.api.boleteria.service.SeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de funciones (proyecciones de películas).
 *
 * Permite crear, consultar, actualizar y eliminar funciones, así como obtener
 * funciones disponibles por película o por tipo de pantalla.
 *
 * La mayoría de las operaciones requieren rol ADMIN, aunque la consulta está permitida
 * también para usuarios con rol CLIENT.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/functions")
@Validated
@CrossOrigin(origins = {"http://localhost:4200"})
public class FunctionController {
    private final FunctionService functionService;
    private final SeatService seatService;


    //-------------------------------CREATE--------------------------------//

    /**
     * Crea una función nueva.
     *
     * @param entity DTO con los datos necesarios para crear una función.
     * @return ResponseEntity con los detalles de la función creada.
     */
    @PostMapping("/create")
   // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FunctionDetailDTO> create(@Valid @RequestBody FunctionRequestDTO entity) {
        return ResponseEntity.ok(functionService.create(entity));
    }


    //-------------------------------GET--------------------------------//

    /**
     * Obtiene la lista de todas las funciones.
     *
     * @return ResponseEntity con una lista de funciones o 204 No Content si no hay funciones.
     */
    // Sin @PreAuthorize a propósito: la cartelera de funciones es pública (la usa
    // la home sin login para armar "Próximos estrenos"), igual que /api/movies.
    // Coincide con SecurityConfig, que ya marca /api/functions/** como permitAll().
    @GetMapping
    public ResponseEntity<List<FunctionDetailDTO>> getAll() {
        List<FunctionDetailDTO> list = functionService.findAll();
        return ResponseEntity.ok(list);
    }

    /**
     * Obtiene todas las butacas asociadas a una función específica.
     *
     * @param functionId ID de la función.
     * @return Lista de butacas con su estado (ocupada/libre).
     */
    @GetMapping("/{functionId}/seats")
    public ResponseEntity<List<SeatListDTO>> getSeatsByFunction(@PathVariable Long functionId) {
        List<SeatListDTO> seats = seatService.getSeatsByFunction(functionId);
        return ResponseEntity.ok(seats);
    }


    /**
     * Obtiene el detalle de una función específica por su ID.
     *
     * @param id Identificador de la función.
     * @return ResponseEntity con el detalle de la función.
     */
    @GetMapping("/{id}")
    public ResponseEntity<FunctionDetailDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(functionService.findById(id));
    }



    /**
     * Obtiene la lista de funciones disponibles para una película específica,
     * considerando únicamente aquellas con capacidad disponible.
     * <p>
     * Si no se encuentran funciones disponibles para la película indicada,
     * se devuelve una respuesta con código 204 (No Content).
     *
     * @param movieId Identificador de la película.
     * @return ResponseEntity con la lista de funciones disponibles o 204 si está vacía.
     */
    @GetMapping("/available/{movieId}")
    public ResponseEntity<List<FunctionDetailDTO>> getAvailableFunctionsPerMovie(@PathVariable Long movieId) {
        List<FunctionDetailDTO> functions = functionService.findByMovieIdAndAvailableCapacity(movieId);
        return ResponseEntity.ok(functions);
    }



    /**
     * Obtiene la lista de funciones filtradas por tipo de pantalla.
     * <p>
     *
     * @param screenType Tipo de pantalla para filtrar las funciones.
     * @return ResponseEntity con la lista de funciones que coinciden con el tipo de pantalla,
     * o estado 204 si no hay resultados.
     */
    @GetMapping("/screentype/{screenType}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT')")
    public ResponseEntity<List<FunctionDetailDTO>> getByScreenType(@PathVariable ScreenType screenType) {
        List<FunctionDetailDTO> functions = functionService.findByScreenType(screenType);
        return ResponseEntity.ok(functions);
    }



    /**
     * Endpoint para obtener funciones disponibles de una sala específica.
     *
     * @param cinemaId ID de la sala a consultar.
     * @return Lista de funciones detalladas.
     */
    @GetMapping("/cinema/{cinemaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<List<FunctionDetailDTO>> getFunctionsByCinema(@PathVariable Long cinemaId) {
        List<FunctionDetailDTO> result = functionService.findByCinemaId(cinemaId);
        return ResponseEntity.ok(result);
    }


    //-------------------------------UPDATE--------------------------------//

    /**
     * Actualiza una función por su ID.
     *
     * @param id Identificador de la función a actualizar.
     * @param entity DTO con los nuevos datos de la función.
     * @return ResponseEntity con el detalle actualizado de la función.
     */
    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FunctionDetailDTO> update (@PathVariable Long id,
                                                     @Valid @RequestBody FunctionRequestDTO entity){
        return ResponseEntity.ok(functionService.updateById(id, entity));
    }


    //-------------------------------DELETE--------------------------------//

    /**
     * Elimina una función por su ID.
     *
     * @param id Identificador de la función a eliminar.
     * @return ResponseEntity con mensaje confirmando la eliminación.
     */
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> delete(@PathVariable Long id){
        functionService.deleteById(id);
        return ResponseEntity.ok("Función eliminada correctamente.");
    }
}

