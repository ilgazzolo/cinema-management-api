package com.api.boleteria.controller;

import com.api.boleteria.dto.request.ForgotPasswordRequestDTO;
import com.api.boleteria.dto.request.LoginRequestDTO;
import com.api.boleteria.dto.request.RegisterRequestDTO;
import com.api.boleteria.dto.request.ResetPasswordRequestDTO;
import com.api.boleteria.service.PasswordResetService;
import com.api.boleteria.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controlador REST para autenticación y registro de usuarios.
 *
 * Permite a los usuarios autenticarse (login) y registrarse en el sistema.
 */
@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200"})
public class AuthController {

    private final AuthenticationManager authManager;
    private final UserService userService;
    private final PasswordResetService passwordResetService;

    /**
     * Autentica a un usuario con las credenciales proporcionadas.
     *
     * @param entity DTO con username y password para autenticación.
     * @return ResponseEntity con un mapa que contiene el token JWT u otra información de sesión.
     */

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequestDTO entity) {
        return ResponseEntity.ok(userService.login(entity, authManager));
    }

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param entity DTO con los datos para registrar al usuario.
     * @return ResponseEntity con mensaje de éxito o conflicto si el username ya existe.
     */

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid RegisterRequestDTO entity) {
        userService.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body("Usuario registrado con éxito");
    }

    /**
     * Solicita la recuperación de contraseña. Si el email está registrado, se envía un correo
     * con un enlace para restablecer la contraseña. Siempre responde 200 para no revelar
     * si el email existe o no en el sistema.
     *
     * @param entity DTO con el email del usuario.
     * @return ResponseEntity con mensaje genérico de confirmación.
     */

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO entity) {
        passwordResetService.requestReset(entity);
        return ResponseEntity.ok("Si el email está registrado, recibirás un correo con instrucciones para recuperar tu contraseña.");
    }

    /**
     * Restablece la contraseña de un usuario a partir de un token de recuperación válido.
     *
     * @param entity DTO con el token y la nueva contraseña.
     * @return ResponseEntity con mensaje de éxito.
     */

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO entity) {
        passwordResetService.resetPassword(entity);
        return ResponseEntity.ok("Contraseña actualizada con éxito.");
    }

}
