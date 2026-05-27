package com.doari.usuarios.dto;

import com.doari.usuarios.model.TipoUsuario;

public record AuthResponse(
        Long usuarioId,
        String nome,
        String email,
        TipoUsuario tipo,
        String tokenType,
        String accessToken,
        long expiresIn
) {
}
