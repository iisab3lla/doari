package com.example.doari.dto;

public record PedidoRequest(
        String item,
        String quantidade,
        String descricao
) {
}
