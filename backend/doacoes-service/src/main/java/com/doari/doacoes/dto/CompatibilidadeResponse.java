package com.doari.doacoes.dto;

public record CompatibilidadeResponse(
        Long doacaoId,
        Long pedidoId,
        Long doadorId,
        Long ongId,
        String item
) {
}
