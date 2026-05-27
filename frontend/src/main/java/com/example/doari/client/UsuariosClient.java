package com.example.doari.client;

import com.example.doari.dto.AuthResponse;
import com.example.doari.dto.CadastroUsuarioRequest;
import com.example.doari.dto.LoginRequest;
import com.example.doari.dto.UsuarioResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class UsuariosClient {

    private final RestClient restClient;

    public UsuariosClient(@Value("${doari.services.usuarios-url}") String usuariosUrl) {
        restClient = RestClient.builder()
                .baseUrl(usuariosUrl)
                .build();
    }

    public UsuarioResponse cadastrar(CadastroUsuarioRequest request) {
        try {
            return restClient.post()
                    .uri("/usuarios/cadastro")
                    .body(request)
                    .retrieve()
                    .body(UsuarioResponse.class);
        } catch (RestClientResponseException exception) {
            throw new ApiException(extrairMensagemErro(exception, "Não foi possível realizar o cadastro."));
        }
    }

    public AuthResponse login(LoginRequest request) {
        try {
            return restClient.post()
                    .uri("/auth/login")
                    .body(request)
                    .retrieve()
                    .body(AuthResponse.class);
        } catch (RestClientResponseException exception) {
            throw new ApiException(extrairMensagemErro(exception, "E-mail ou senha inválidos."));
        }
    }

    public UsuarioResponse buscarPerfil(String token, Long usuarioId) {
        try {
            return restClient.get()
                    .uri("/usuarios/{id}/perfil", usuarioId)
                    .header("Authorization", token)
                    .retrieve()
                    .body(UsuarioResponse.class);
        } catch (RestClientResponseException exception) {
            throw new ApiException(extrairMensagemErro(exception, "Não foi possível buscar o perfil."));
        }
    }

    private String extrairMensagemErro(RestClientResponseException exception, String fallback) {
        String resposta = exception.getResponseBodyAsString();
        String chave = "\"erro\":\"";
        int inicio = resposta.indexOf(chave);

        if (inicio >= 0) {
            inicio += chave.length();
            int fim = resposta.indexOf("\"", inicio);
            if (fim > inicio) {
                return resposta.substring(inicio, fim);
            }
        }

        return fallback;
    }
}
