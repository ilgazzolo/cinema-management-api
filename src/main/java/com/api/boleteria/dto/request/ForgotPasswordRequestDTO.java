package com.api.boleteria.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPasswordRequestDTO {

    @NotBlank(message = "El email es obligatorio")
    private String email;
}
