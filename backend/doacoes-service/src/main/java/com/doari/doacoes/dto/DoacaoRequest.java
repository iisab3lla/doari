package com.doari.doacoes.dto;

import jakarta.validation.constraints.NotBlank;

public record DoacaoRequest(
        @NotBlank String item,
        String quantidade,
        String descricao
) {
}
