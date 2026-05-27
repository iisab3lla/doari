package com.doari.doacoes.dto;

import com.doari.doacoes.model.StatusItem;
import java.time.Instant;

public record PedidoResponse(
        Long id,
        Long ongId,
        String item,
        String quantidade,
        String descricao,
        StatusItem status,
        Instant criadoEm
) {
}
