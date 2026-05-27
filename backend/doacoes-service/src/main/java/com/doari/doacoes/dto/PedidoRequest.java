package com.doari.doacoes.dto;

import jakarta.validation.constraints.NotBlank;

public record PedidoRequest(
        @NotBlank String item,
        String quantidade,
        String descricao
) {
}
