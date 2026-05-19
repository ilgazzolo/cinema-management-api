package com.api.boleteria.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDTO {
    @NotBlank(message = "El nombre no puede ser nulo")
    @Size(min = 1,max = 50, message = "Min 1 caracter, Max 50 caracteres")
    private String name;

    @NotBlank(message = "El apellido no puede ser nulo")
    @Size(min = 1,max = 50, message = "Min 1 caracter, Max 50 caracteres")
    private String surname;

    @NotBlank(message = "El nombre de usuario no puede ser nulo")
    @Size(min = 1,max = 50, message = "Min 1 caracter, Max 50 caracteres")
    private String username;

    @Email(message = "El email debe tener un formato valido")
    private String email;

    @NotBlank(message = "La contrasenia es obligatoria")
    @Size(min = 6, max = 20, message = "La contraseña debe tener entre 6 y 20 caracteres. ")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()\\-_=+\\[\\]{}|;:'\",.<>?/\\\\`~])[A-Za-z\\d!@#$%^&*()\\-_=+\\[\\]{}|;:'\",.<>?/\\\\`~]{8,64}$",
            message = "La contraseña debe contener mayúsculas, minúsculas, números y caracteres especiales"
    )
    @ToString.Exclude
    private String password;
    private String currentPassword;




}
