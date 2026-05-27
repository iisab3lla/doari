package com.example.doari.dto;

public record AtenderPedidoRequest(
        String quantidade,
        String unidade,
        String mensagem,
        String nomeContato,
        String emailContato,
        String telefoneContato
) {
}
