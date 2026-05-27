package com.example.doari.session;

public record SessaoUsuario(
        Long id,
        String nome,
        String email,
        String tipo,
        String accessToken
) {

    public boolean isDoador() {
        return "DOADOR".equals(tipo);
    }

    public boolean isOng() {
        return "ONG".equals(tipo);
    }
}
