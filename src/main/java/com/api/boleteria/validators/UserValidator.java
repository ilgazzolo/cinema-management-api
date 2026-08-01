package com.api.boleteria.validators;

import com.api.boleteria.dto.request.RegisterRequestDTO;

import java.util.regex.Pattern;

/**
 * Clase que valida los campos del DTO de registro de usuario.
 *
 * Valida que los campos obligatorios no estén vacíos, que cumplan con
 * restricciones de longitud y formato, y que la contraseña cumpla con las
 * reglas de complejidad requeridas.
 *
 * Lanza IllegalArgumentException con mensajes específicos si alguna validación falla.
 */
public class UserValidator {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()\\-_=+\\[\\]{}|;:'\",.<>?/\\\\`~])[A-Za-z\\d!@#$%^&*()\\-_=+\\[\\]{}|;:'\",.<>?/\\\\`~]{6,20}$"
    );

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$"
    );

    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-zÀ-ÿ\\s]+$");

    /**
     * Valida todos los campos obligatorios y sus formatos en el DTO de registro.
     *
     * @param dto DTO con los datos de registro a validar.
     * @throws IllegalArgumentException si algún campo no cumple las reglas de validación.
     */
    public static void validateFields(RegisterRequestDTO dto) {
        validateName(dto.getName());
        validateSurname(dto.getSurname());
        validateUsername(dto.getUsername());
        validateEmail(dto.getEmail());
        validatePassword(dto.getPassword());
    }

    /**
     * Valida campos para actualización de usuario.
     * La contraseña es OPCIONAL: solo se valida si viene informada.
     */
    public static void validateUpdateFields(RegisterRequestDTO dto) {
        validateName(dto.getName());
        validateSurname(dto.getSurname());
        validateUsername(dto.getUsername());
        validateEmail(dto.getEmail());

        // Contraseña sólo si viene con contenido
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            validatePassword(dto.getPassword());
        }
    }

    /**
     * Valida el nombre: no vacío, longitud máxima y caracteres permitidos.
     *
     * @param name Nombre a validar.
     * @throws IllegalArgumentException si la validación falla.
     */
    public static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }
        if (name.length() > 50) {
            throw new IllegalArgumentException("El nombre no puede superar los 50 caracteres.");
        }
        if (!NAME_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException("El nombre solo puede contener letras y espacios.");
        }
    }

    /**
     * Valida el apellido: no vacío, longitud máxima y caracteres permitidos.
     *
     * @param surname Apellido a validar.
     * @throws IllegalArgumentException si la validación falla.
     */
    public static void validateSurname(String surname) {
        if (surname == null || surname.isBlank()) {
            throw new IllegalArgumentException("El apellido no puede estar vacío.");
        }
        if (surname.length() > 50) {
            throw new IllegalArgumentException("El apellido no puede superar los 50 caracteres.");
        }
        if (!NAME_PATTERN.matcher(surname).matches()) {
            throw new IllegalArgumentException("El apellido solo puede contener letras y espacios.");
        }
    }

    /**
     * Valida el nombre de usuario: no vacío y longitud máxima.
     *
     * @param username Nombre de usuario a validar.
     * @throws IllegalArgumentException si la validación falla.
     */
    public static void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("El nombre de usuario no puede estar vacío.");
        }
        if (username.length() > 50) {
            throw new IllegalArgumentException("El nombre de usuario no puede superar los 50 caracteres.");
        }
    }

    /**
     * Valida el email: no vacío y formato válido.
     *
     * @param email Email a validar.
     * @throws IllegalArgumentException si la validación falla.
     */
    public static void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El email no puede estar vacío.");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("El email no es válido.");
        }
    }

    /**
     * Valida la contraseña: no vacía, longitud entre 6 y 20, y cumple con el patrón de complejidad.
     *
     * @param password Contraseña a validar.
     * @throws IllegalArgumentException si la validación falla.
     */
    public static void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía.");
        }
        if (password.length() < 6 || password.length() > 20) {
            throw new IllegalArgumentException("La contraseña debe tener entre 6 y 20 caracteres.");
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException("La contraseña debe contener mayúsculas, minúsculas, números y caracteres especiales.");
        }
    }

    /**
     * Valida que el ID del usuario sea un número positivo y no nulo.
     *
     * @param id ID del usuario a validar.
     * @throws IllegalArgumentException si el ID es nulo o menor o igual a cero.
     */
    public static void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID del usuario debe ser un número positivo.");
        }
    }
}
