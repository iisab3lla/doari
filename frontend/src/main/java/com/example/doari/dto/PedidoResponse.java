package com.example.doari.dto;

public record PedidoResponse(
        Long id,
        Long ongId,
        String item,
        String quantidade,
        String descricao,
        String status,
        String criadoEm
) {
}
