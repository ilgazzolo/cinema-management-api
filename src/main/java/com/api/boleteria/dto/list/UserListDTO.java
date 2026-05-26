package com.api.boleteria.dto.list;

public record UserListDTO(
        Long id,
        String username,
        String email,
        String role,
        Integer puntos
) {
}
