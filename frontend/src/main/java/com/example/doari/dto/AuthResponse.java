package com.example.doari.dto;

public record AuthResponse(
        Long usuarioId,
        String nome,
        String email,
        String tipo,
        String tokenType,
        String accessToken,
        long expiresIn
) {
}
