package com.doari.notificacoes.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class InternalTokenService {

    private final String expectedToken;

    public InternalTokenService(@Value("${notificacao.internal-token}") String expectedToken) {
        this.expectedToken = expectedToken;
    }

    public void validar(String receivedToken) {
        if (receivedToken == null || !tokensIguais(expectedToken, receivedToken)) {
            throw new UnauthorizedException("Token interno invalido.");
        }
    }

    private boolean tokensIguais(String expected, String received) {
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                received.getBytes(StandardCharsets.UTF_8)
        );
    }
}
