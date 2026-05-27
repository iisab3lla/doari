package com.example.doari.dto;

public record NotificacaoResponse(
        Long id,
        String tipo,
        String destinatarioTipo,
        Long destinatarioId,
        Long doacaoId,
        Long pedidoId,
        String mensagem,
        boolean lida,
        String criadaEm
) {
}
