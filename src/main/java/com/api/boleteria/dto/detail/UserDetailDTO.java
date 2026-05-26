package com.api.boleteria.dto.detail;

public record UserDetailDTO(
        Long id,
        String name,
        String surname,
        String username,
        String email,
        String role,
        Integer puntos
) {}

