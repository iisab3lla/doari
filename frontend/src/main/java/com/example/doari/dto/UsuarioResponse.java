package com.example.doari.dto;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        String telefone,
        String documento,
        String tipo,
        String criadoEm
) {
}
