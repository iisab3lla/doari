package com.example.doari.dto;

public record DoacaoResponse(
        Long id,
        Long usuarioId,
        Long ongId,
        Long pedidoId,
        String item,
        String quantidade,
        String descricao,
        String status,
        String criadoEm,
        String atualizadoEm
) {
}
