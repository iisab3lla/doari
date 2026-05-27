package com.doari.doacoes.dto;

import jakarta.validation.constraints.NotBlank;

public record AtenderPedidoRequest(
        @NotBlank String quantidade,
        String unidade,
        String mensagem,
        @NotBlank String nomeContato,
        @NotBlank String emailContato,
        @NotBlank String telefoneContato
) {
}
