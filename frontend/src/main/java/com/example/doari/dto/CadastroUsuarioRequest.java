package com.example.doari.dto;

public record CadastroUsuarioRequest(
        String nome,
        String email,
        String senha,
        String telefone,
        String documento,
        String tipo
) {
}
