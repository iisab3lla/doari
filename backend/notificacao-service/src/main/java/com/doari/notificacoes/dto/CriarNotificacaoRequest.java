package com.doari.notificacoes.dto;

import com.doari.notificacoes.model.DestinatarioTipo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CriarNotificacaoRequest(
        @NotBlank String tipo,
        @NotNull DestinatarioTipo destinatarioTipo,
        @NotNull Long destinatarioId,
        Long doacaoId,
        Long pedidoId,
        @NotBlank String mensagem
) {
}
