package com.example.doari.view;

import com.example.doari.dto.DoacaoResponse;

public record DoacaoView(
        Long id,
        Long ongId,
        String itemDoado,
        String destino,
        String status,
        String statusCss,
        String mensagem,
        String data,
        boolean podeExcluir
) {

    public static DoacaoView from(DoacaoResponse response) {
        return from(response, false, null);
    }

    public static DoacaoView from(DoacaoResponse response, String nomeOng) {
        return from(response, false, nomeOng);
    }

    public static DoacaoView fromOng(DoacaoResponse response) {
        return from(response, true, null);
    }

    private static DoacaoView from(DoacaoResponse response, boolean visaoOng, String nomeOng) {
        String quantidade = response.quantidade() == null || response.quantidade().isBlank()
                ? ""
                : " - " + response.quantidade();
        String status = formatarStatus(response.status(), visaoOng);
        return new DoacaoView(
                response.id(),
                response.ongId(),
                response.item() + quantidade,
                destino(response.ongId(), nomeOng),
                status,
                statusCss(response.status(), visaoOng),
                response.descricao() == null ? "" : response.descricao(),
                formatarData(response.criadoEm()),
                "DISPONIVEL".equals(response.status())
        );
    }

    private static String destino(Long ongId, String nomeOng) {
        if (ongId == null) {
            return "Disponível para ONGs";
        }
        if (nomeOng != null && !nomeOng.isBlank()) {
            return nomeOng;
        }
        return "ONG #" + ongId;
    }

    private static String formatarStatus(String status) {
        return formatarStatus(status, false);
    }

    private static String formatarStatus(String status, boolean visaoOng) {
        if ("DISPONIVEL".equals(status)) {
            return "Disponível";
        }
        if ("SOLICITADA".equals(status)) {
            return visaoOng ? "Pendente" : "Solicitada";
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

    private static String statusCss(String status, boolean visaoOng) {
        if ("SOLICITADA".equals(status) && visaoOng) {
            return "pendente";
        }
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
