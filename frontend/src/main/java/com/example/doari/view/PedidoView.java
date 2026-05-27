package com.example.doari.view;

import com.example.doari.dto.PedidoResponse;

public record PedidoView(
        Long id,
        Long ongId,
        String ongNome,
        String ongCidade,
        String item,
        String quantidade,
        String descricao,
        String status,
        String statusCss,
        String data,
        boolean podeExcluir
) {

    public static PedidoView from(PedidoResponse response) {
        return from(response, "ONG #" + response.ongId(), "Não informada");
    }

    public static PedidoView from(PedidoResponse response, String ongNome) {
        return from(response, ongNome, "Não informada");
    }

    public static PedidoView from(PedidoResponse response, String ongNome, String ongCidade) {
        String status = formatarStatus(response.status());
        return new PedidoView(
                response.id(),
                response.ongId(),
                ongNome == null || ongNome.isBlank() ? "ONG #" + response.ongId() : ongNome,
                ongCidade == null || ongCidade.isBlank() ? "Não informada" : ongCidade,
                response.item(),
                response.quantidade() == null ? "" : response.quantidade(),
                response.descricao() == null ? "" : response.descricao(),
                status,
                statusCss(response.status()),
                formatarData(response.criadoEm()),
                "DISPONIVEL".equals(response.status())
        );
    }

    private static String formatarStatus(String status) {
        if ("DISPONIVEL".equals(status)) {
            return "Disponível";
        }
        if ("SOLICITADA".equals(status)) {
            return "Solicitada";
        }
        if ("CONFIRMADA".equals(status)) {
            return "Confirmada";
        }
        if ("ENTREGUE".equals(status)) {
            return "Entregue";
        }
        if ("RECUSADA".equals(status)) {
            return "Recusada";
        }
        if ("REMOVIDO".equals(status)) {
            return "Removido";
        }
        return status == null ? "" : status;
    }

    private static String statusCss(String status) {
        if (status == null) {
            return "";
        }
        return status.toLowerCase();
    }

    private static String formatarData(String value) {
        if (value == null || value.length() < 10) {
            return "";
        }
        return value.substring(8, 10) + "/" + value.substring(5, 7) + "/" + value.substring(0, 4);
    }
}
