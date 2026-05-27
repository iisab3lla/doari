package com.doari.notificacoes.security;

import com.doari.notificacoes.model.DestinatarioTipo;

public record AuthenticatedUser(
        Long id,
        String email,
        TipoUsuario tipo
) {

    public DestinatarioTipo destinatarioTipo() {
        return DestinatarioTipo.valueOf(tipo.name());
    }
}
