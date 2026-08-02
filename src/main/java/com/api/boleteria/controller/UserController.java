package com.api.boleteria.controller;

import com.api.boleteria.dto.detail.UserDetailDTO;
import com.api.boleteria.dto.list.UserListDTO;
import com.api.boleteria.dto.request.RegisterRequestDTO;
import com.api.boleteria.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para la gestión de usuarios.
 *
 * Permite obtener, actualizar usuarios y gestionar roles.
 * Algunos endpoints requieren que el usuario tenga rol ADMIN,
 * mientras que otros permiten acceso a CLIENT o ADMIN.
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/userManagement")
@CrossOrigin(origins = {"http://localhost:4200"})
public class UserController {
    private final UserService userService;


    //-------------------------------GET--------------------------------//

    /**
     * Obtiene la lista de todos los usuarios.
     *
     * @return ResponseEntity con la lista de usuarios o 204 No Content si no hay usuarios registrados.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserListDTO>> getAllUsers() {
        List<UserListDTO> users = userService.findAllUsers();

        if (users.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(users);
    }

    /**
     * Obtiene el detalle de un usuario por su username.
     *
     * @param username Nombre de usuario.
     * @return ResponseEntity con el detalle del usuario.
     */

    @GetMapping("/username/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDetailDTO> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.findByUsername(username));

    }

    /**
     * Obtiene el detalle de un usuario por su ID.
     *
     * @param id Identificador del usuario.
     * @return ResponseEntity con el detalle del usuario.
     */

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDetailDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    /**
     * Obtiene el perfil del usuario autenticado.
     *
     * @return ResponseEntity con el detalle del usuario autenticado.
     */

    @GetMapping("/me")
    @PreAuthorize("hasRole('ADMIN')or hasRole('CLIENT')")
    public ResponseEntity<UserDetailDTO> getMyProfile() {
        return ResponseEntity.ok(userService.findProfile());
    }



    //-------------------------------UPDATE--------------------------------//

    /**
     * Otorga rol ADMIN a un usuario existente identificado por su username.
     *
     * @param username Nombre de usuario a actualizar.
     * @return ResponseEntity con mensaje de éxito o error si el usuario no existe.
     */

    @PutMapping("/makeAdmin/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDetailDTO> makeUserAdmin(@PathVariable String username) {
        return ResponseEntity.ok(userService.makeUserAdmin(username));
    }

    /**
     * Actualiza la información del usuario autenticado (o de un usuario, según contexto).
     *
     * @param entity DTO con la información para actualizar.
     * @return ResponseEntity con el usuario actualizado.
     */

    @PutMapping("/me/update")
    @PreAuthorize("hasRole('ADMIN')or hasRole('CLIENT')")
    public ResponseEntity<UserDetailDTO> update(@RequestBody RegisterRequestDTO entity) {
        return ResponseEntity.ok(userService.update(entity));
    }
    @PutMapping("/me/points")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<UserDetailDTO> descontarPuntos(
            @RequestBody Map<String, Integer> body){

        Integer puntos = body.get("puntos");

        return ResponseEntity.ok(userService.descontarPuntos(puntos));
    }
}


