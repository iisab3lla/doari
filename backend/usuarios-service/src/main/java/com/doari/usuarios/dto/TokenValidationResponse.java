package com.doari.usuarios.dto;

import com.doari.usuarios.model.TipoUsuario;

public record TokenValidationResponse(
        boolean valido,
        Long usuarioId,
        String email,
        TipoUsuario tipo
) {
}
