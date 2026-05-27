package com.doari.doacoes.dto;

import com.doari.doacoes.model.StatusItem;
import java.time.Instant;

public record DoacaoResponse(
        Long id,
        Long usuarioId,
        Long ongId,
        Long pedidoId,
        String item,
        String quantidade,
        String descricao,
        StatusItem status,
        Instant criadoEm,
        Instant atualizadoEm
) {
}
