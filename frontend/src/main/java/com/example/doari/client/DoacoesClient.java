package com.example.doari.client;

import com.example.doari.dto.AtenderPedidoRequest;
import com.example.doari.dto.DoacaoRequest;
import com.example.doari.dto.DoacaoResponse;
import com.example.doari.dto.PedidoRequest;
import com.example.doari.dto.PedidoResponse;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class DoacoesClient {

    private final RestClient restClient;

    public DoacoesClient(@Value("${doari.services.doacoes-url}") String doacoesUrl) {
        restClient = RestClient.builder()
                .baseUrl(doacoesUrl)
                .build();
    }

    public List<DoacaoResponse> listarDoacoesDisponiveis(String token) {
        return getList("/doacoes", token, DoacaoResponse[].class);
    }

    public List<DoacaoResponse> listarDoacoesDoUsuario(String token, Long usuarioId) {
        return getList("/doacoes/usuario/" + usuarioId, token, DoacaoResponse[].class);
    }

    public List<DoacaoResponse> listarDoacoesDaOng(String token, Long ongId) {
        return getList("/doacoes/ong/" + ongId, token, DoacaoResponse[].class);
    }

    public DoacaoResponse cadastrarDoacao(String token, DoacaoRequest request) {
        try {
            return restClient.post()
                    .uri("/doacoes")
                    .header("Authorization", bearer(token))
                    .body(request)
                    .retrieve()
                    .body(DoacaoResponse.class);
        } catch (RestClientResponseException exception) {
            throw new ApiException("Não foi possível cadastrar a doação.");
        }
    }

    public void removerDoacao(String token, Long id) {
        try {
            restClient.delete()
                    .uri("/doacoes/{id}", id)
                    .header("Authorization", bearer(token))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException exception) {
            throw new ApiException("Não foi possível excluir a doação.");
        }
    }

    public void confirmarDoacao(String token, Long id) {
        alterarStatusDoacao(token, id, "confirmar", "Não foi possível confirmar a doação.");
    }

    public void recusarDoacao(String token, Long id) {
        alterarStatusDoacao(token, id, "recusar", "Não foi possível recusar a doação.");
    }

    public void entregarDoacao(String token, Long id) {
        alterarStatusDoacao(token, id, "entregar", "Não foi possível marcar a doação como entregue.");
    }

    public List<PedidoResponse> listarPedidosDisponiveis(String token) {
        return getList("/pedidos", token, PedidoResponse[].class);
    }

    public List<PedidoResponse> listarPedidosDaOng(String token, Long ongId) {
        return getList("/pedidos/ong/" + ongId, token, PedidoResponse[].class);
    }

    public PedidoResponse cadastrarPedido(String token, PedidoRequest request) {
        try {
            return restClient.post()
                    .uri("/pedidos")
                    .header("Authorization", bearer(token))
                    .body(request)
                    .retrieve()
                    .body(PedidoResponse.class);
        } catch (RestClientResponseException exception) {
            throw new ApiException("Não foi possível cadastrar o pedido.");
        }
    }

    public void removerPedido(String token, Long id) {
        try {
            restClient.delete()
                    .uri("/pedidos/{id}", id)
                    .header("Authorization", bearer(token))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException exception) {
            throw new ApiException("Não foi possível excluir o pedido.");
        }
    }

    public void atenderPedido(String token, Long pedidoId, AtenderPedidoRequest request) {
        try {
            restClient.post()
                    .uri("/pedidos/{pedidoId}/atender", pedidoId)
                    .header("Authorization", bearer(token))
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException exception) {
            throw new ApiException("Não foi possível atender este pedido.");
        }
    }

    private <T> List<T> getList(String uri, String token, Class<T[]> responseType) {
        try {
            T[] response = restClient.get()
                    .uri(uri)
                    .header("Authorization", bearer(token))
                    .retrieve()
                    .body(responseType);
            return response == null ? List.of() : Arrays.asList(response);
        } catch (RestClientResponseException exception) {
            throw new ApiException("Não foi possível carregar dados do serviço de doações.");
        }
    }

    private void alterarStatusDoacao(String token, Long id, String acao, String mensagemErro) {
        try {
            restClient.post()
                    .uri("/doacoes/{id}/{acao}", id, acao)
                    .header("Authorization", bearer(token))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException exception) {
            throw new ApiException(mensagemErro);
        }
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
