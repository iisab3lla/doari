package com.doari.usuarios.dto;

import com.doari.usuarios.model.TipoUsuario;
import java.time.Instant;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        String telefone,
        String documento,
        TipoUsuario tipo,
        Instant criadoEm
) {
}
