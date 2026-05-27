package com.doari.notificacoes.dto;

import com.doari.notificacoes.model.DestinatarioTipo;
import java.time.Instant;

public record NotificacaoResponse(
        Long id,
        String tipo,
        DestinatarioTipo destinatarioTipo,
        Long destinatarioId,
        Long doacaoId,
        Long pedidoId,
        String mensagem,
        boolean lida,
        Instant criadaEm
) {
}
