package com.example.doari.client;

import com.example.doari.dto.NotificacaoResponse;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class NotificacoesClient {

    private final RestClient restClient;

    public NotificacoesClient(@Value("${doari.services.notificacoes-url}") String notificacoesUrl) {
        restClient = RestClient.builder()
                .baseUrl(notificacoesUrl)
                .build();
    }

    public List<NotificacaoResponse> listar(String token, String destinatarioTipo, Long destinatarioId) {
        try {
            NotificacaoResponse[] response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/notificacoes")
                            .queryParam("destinatarioTipo", destinatarioTipo)
                            .queryParam("destinatarioId", destinatarioId)
                            .build())
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(NotificacaoResponse[].class);
            return response == null ? List.of() : Arrays.asList(response);
        } catch (RestClientResponseException exception) {
            throw new ApiException("Não foi possível carregar notificações.");
        }
    }
}
