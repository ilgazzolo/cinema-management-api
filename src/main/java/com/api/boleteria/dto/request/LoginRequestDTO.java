package com.api.boleteria.dto.request;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Getter
@Setter


public class LoginRequestDTO {

    @NotBlank(message = "El usuario o email es obligatorio")
    @Size(min = 1, max = 100, message = "Debe tener entre 1 y 100 caracteres")
    private String usernameOrEmail;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 20, message = "La contraseña debe tener entre 8 y 20 caracteres")
    private String password;

}
