package com.example.doari.view;

import com.example.doari.dto.NotificacaoResponse;

public record NotificacaoView(
        Long id,
        String mensagem,
        boolean lida,
        String data
) {

    public static NotificacaoView from(NotificacaoResponse response) {
        return new NotificacaoView(
                response.id(),
                response.mensagem(),
                response.lida(),
                formatarData(response.criadaEm())
        );
    }

    private static String formatarData(String value) {
        if (value == null || value.length() < 10) {
            return "";
        }
        return value.substring(8, 10) + "/" + value.substring(5, 7) + "/" + value.substring(0, 4);
    }
}
