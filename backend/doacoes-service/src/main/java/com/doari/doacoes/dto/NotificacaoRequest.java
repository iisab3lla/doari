package com.doari.doacoes.dto;

public record NotificacaoRequest(
        String tipo,
        String destinatarioTipo,
        Long destinatarioId,
        Long doacaoId,
        Long pedidoId,
        String mensagem
) {
}
