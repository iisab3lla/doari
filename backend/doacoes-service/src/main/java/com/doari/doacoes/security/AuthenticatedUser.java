package com.doari.doacoes.security;

public record AuthenticatedUser(
        Long id,
        String email,
        TipoUsuario tipo
) {

    public void requireTipo(TipoUsuario expectedTipo) {
        if (tipo != expectedTipo) {
            throw new ForbiddenException("Perfil não autorizado para esta operação.");
        }
    }
}
