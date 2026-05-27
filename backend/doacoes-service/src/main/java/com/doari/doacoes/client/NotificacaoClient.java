package com.doari.doacoes.client;

import com.doari.doacoes.dto.NotificacaoRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class NotificacaoClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificacaoClient.class);

    private final RestClient restClient;
    private final String internalToken;

    public NotificacaoClient(
            @Value("${notificacao.service.url}") String notificacaoServiceUrl,
            @Value("${notificacao.internal-token}") String internalToken
    ) {
        restClient = RestClient.builder()
                .baseUrl(notificacaoServiceUrl)
                .build();
        this.internalToken = internalToken;
    }

    public void enviar(NotificacaoRequest request) {
        try {
            restClient.post()
                    .uri("/notificacoes")
                    .header("X-Internal-Token", internalToken)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            LOGGER.warn("Falha ao enviar notificacao: {}", exception.getMessage());
        }
    }
}
