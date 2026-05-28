package com.api.boleteria.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

/**
 * Maneja de forma global las excepciones lanzadas en los controladores REST.
 *
 * Proporciona métodos específicos para manejar distintas excepciones y devolver
 * respuestas HTTP con el código y mensaje adecuado.
 */

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones lanzadas cuando el cuerpo de la solicitud no se puede leer o parsear correctamente.
     *
     * Esta excepción se lanza, por ejemplo, cuando el cliente envía un valor incompatible con el tipo esperado
     * (por ejemplo, un texto en lugar de un número).
     *
     * Si la causa es un {@link com.fasterxml.jackson.databind.exc.InvalidFormatException}, se extrae el campo específico
     * y el tipo esperado para construir un mensaje de error más claro.
     *
     * @param ex la excepción {@link HttpMessageNotReadableException} capturada.
     * @return ResponseEntity con un mensaje de error personalizado y estado HTTP 400 (Bad Request).
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();

        if (cause instanceof InvalidFormatException formatEx && !formatEx.getPath().isEmpty()) {
            // Arma el path completo por si está anidado (ej: pelicula.duracion)
            String fieldPath = formatEx.getPath().stream()
                    .map(ref -> ref.getFieldName())
                    .filter(name -> name != null)
                    .reduce((a, b) -> a + "." + b)
                    .orElse("campo desconocido");

            String expectedType = formatEx.getTargetType().getSimpleName();

            String message = "El campo '" + fieldPath + "' debe ser de tipo " + expectedType + ".";

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(message);
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Error en el cuerpo de la solicitud.");
    }

    /**
     * Maneja excepciones de tipo NullPointerException.
     * Se lanza cuando se intenta acceder o invocar un método sobre un objeto nulo.
     *
     * Ejemplo común: cuando el campo "enabled" es null y se llama a enabled.booleanValue().
     *
     * @param ex Excepción capturada.
     * @return ResponseEntity con mensaje descriptivo y código 500 (INTERNAL_SERVER_ERROR).
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<String> handleNullPointerException(NullPointerException ex) {
        String message = "Faltan datos obligatorios o se está accediendo a un valor nulo.";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
    }

    /**
     * Maneja violaciones de integridad de datos en la base.
     * Se lanza cuando se intenta insertar datos duplicados o violar restricciones únicas o foráneas.
     *
     * Ejemplo común: cuando se intenta asociar más de una tarjeta a un usuario que solo puede tener una.
     *
     * @param ex Excepción capturada.
     * @return ResponseEntity con mensaje amigable y código 409 (CONFLICT).
     */
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        // Extraemos la causa raíz exacta del error de SQL
        String realError = ex.getMostSpecificCause().getMessage();
    
        // Lo imprimimos en la consola del backend para que puedas leerlo completo
        System.out.println("❌ ERROR DE INTEGRIDAD EN BD: " + realError);
    
        // Le mandamos el error real al frontend temporalmente para debugear
        String message = "Error de base de datos: " + realError;
    
        return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
    }

    /**
     * Maneja excepciones cuando las credenciales proporcionadas son incorrectas.
     * Se lanza durante el proceso de autenticación si el usuario o la contraseña no son válidos.
     *
     * @param ex Excepción capturada de tipo BadCredentialsException.
     * @return ResponseEntity con mensaje claro y estado 401 (UNAUTHORIZED).
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> handleBadCredentialsException(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Credenciales incorrectas. Verifique su nombre de usuario y contraseña.");
    }

    /**
     * Maneja excepciones cuando un parámetro recibido en la URL (por ejemplo, vía @RequestParam o @PathVariable)
     * no puede convertirse al tipo esperado.
     *
     * Esta excepción ocurre comúnmente cuando se espera un número (como Long o Integer) y se recibe una cadena de texto,
     * o cualquier valor que no se pueda mapear correctamente.
     *
     * Ejemplo: si un endpoint espera `?id=5` pero se recibe `?id=abc`, se lanza esta excepción.
     *
     * @param ex la excepción {@link MethodArgumentTypeMismatchException} capturada.
     * @return ResponseEntity con un mensaje claro y estado HTTP 400 (Bad Request).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String name = ex.getName();
        return ResponseEntity
                .badRequest()
                .body("El parámetro '" + name + "' debe ser válido.");
    }



    /**
     * Maneja excepciones lanzadas cuando se violan restricciones de validación declaradas con anotaciones
     * como {@code @NotNull}, {@code @Min}, {@code @Max}, {@code @Pattern}, etc. en parámetros del controlador.
     *
     * Esta excepción típicamente ocurre en validaciones de parámetros de métodos del controlador cuando se
     * utiliza {@code @Validated} a nivel de clase o método.
     *
     * Ejemplo: si un parámetro anotado con {@code @Min(1)} recibe el valor 0, se lanza esta excepción.
     *
     * @param ex la excepción {@link ConstraintViolationException} capturada.
     * @return ResponseEntity con el mensaje del primer error y estado HTTP 400 (Bad Request).
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException ex) {
        String mensajeError = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElse("Error de validación");

        return ResponseEntity.badRequest().body(mensajeError);
    }


    /**
     * Maneja la excepción BadRequestException y devuelve una respuesta con estado 400.
     *
     * @param ex Excepción BadRequestException capturada.
     * @return ResponseEntity con mensaje de error y estado HTTP 400 (Bad Request).
     */

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<String> BadRequestExceptionHandler(BadRequestException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }


    /**
     * Maneja la excepción IllegalArgumentException y devuelve una respuesta con estado 400.
     *
     * @param ex Excepción IllegalArgumentException capturada.
     * @return ResponseEntity con mensaje de error y estado HTTP 400 (Bad Request).
     */

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> IllegalArgumentExceptionHandler(IllegalArgumentException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }


    /**
     * Maneja la excepción AccesDeniedException y devuelve una respuesta con estado 401.
     *
     * @param ex Excepción AccesDeniedException capturada.
     * @return ResponseEntity con mensaje de error y estado HTTP 403 (Forbidden).
     */

    @ExceptionHandler(AccessDeniedExceptionPeronalized.class)
    public ResponseEntity<String> AccesDeniedExceptionsHandler(AccessDeniedExceptionPeronalized ex){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> AccesDeniedExceptionsHandler(AccessDeniedException ex){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    /**
     * Maneja la excepción UsernameNotFoundException y devuelve una respuesta con estado 404.
     *
     * @param ex Excepción UsernameNotFoundException capturada.
     * @return ResponseEntity con mensaje de error y estado HTTP 404 (Not Found).
     */

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<String> userNameNotFoundExceptionHandler(UsernameNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getLocalizedMessage());
    }


    /**
     * Maneja la excepción NotFoundException y devuelve una respuesta con estado 404.
     *
     * @param ex Excepción NotFoundException capturada.
     * @return ResponseEntity con mensaje de error y estado HTTP 404 (Not Found).
     */

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> NotFoundExceptionHandler(NotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /**
     * Maneja excepciones lanzadas cuando fallan las validaciones de bean (@Valid) en los objetos del cuerpo
     * de la solicitud (por ejemplo, DTOs anotados con {@code @NotBlank}, {@code @Size}, etc.).
     *
     * Esta excepción se lanza automáticamente cuando se utiliza {@code @Valid} en parámetros del controlador
     * y alguna restricción de validación no se cumple.
     *
     * Este handler extrae y devuelve solo el primer mensaje de error detectado, simplificando la respuesta.
     *
     * @param ex Excepción {@link MethodArgumentNotValidException} capturada.
     * @return ResponseEntity con el primer mensaje de error y estado HTTP 400 (Bad Request).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationException(MethodArgumentNotValidException ex) {
        // Obtener solo el primer error para simplificar
        String mensajeError = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(err -> err.getDefaultMessage())
                .orElse("Error de validación");

        return ResponseEntity.badRequest().body(mensajeError);
    }

    /**
     * Maneja las excepciones de tipo {@link AuthorizationDeniedException} lanzadas cuando
     * un usuario intenta acceder a un recurso o realizar una acción sin los permisos necesarios.
     *
     * @param ex la excepción {@code AuthorizationDeniedException} capturada.
     * @return una {@link ResponseEntity} con código 403 (Forbidden) y un mensaje personalizado.
     */
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<String> handleAuthorizationDeniedException(AuthorizationDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN) // Código 403
                .body("No tienes autorización para realizar esta acción"); // Mensaje personalizado
    }




    /**
     * Maneja excepciones generales no capturadas específicamente.
     *
     * @param ex Excepción general capturada.
     * @return ResponseEntity con mensaje de error y estado HTTP 500 (Internal Server Error).
     */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> otherExceptionHandler(Exception ex) {
        ex.printStackTrace(); // Muestra en consola el error completo con origen
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error inesperado: " + ex.getMessage());
    }





}
