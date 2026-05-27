package com.doari.notificacoes.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

    private final ObjectMapper objectMapper;
    private final byte[] secret;

    public JwtService(ObjectMapper objectMapper, @Value("${jwt.secret}") String secret) {
        this.objectMapper = objectMapper;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    public AuthenticatedUser autenticarHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Token JWT ausente.");
        }
        return autenticarToken(authorizationHeader.substring("Bearer ".length()).trim());
    }

    public AuthenticatedUser autenticarToken(String token) {
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("Token JWT ausente.");
        }

        String[] partes = token.split("\\.");
        if (partes.length != 3) {
            throw new UnauthorizedException("Token JWT invalido.");
        }

        String unsignedToken = partes[0] + "." + partes[1];
        if (!assinaturaValida(unsignedToken, partes[2])) {
            throw new UnauthorizedException("Token JWT invalido.");
        }

        return autenticarPayload(partes[1]);
    }

    private boolean assinaturaValida(String unsignedToken, String assinaturaRecebida) {
        String assinaturaEsperada = assinar(unsignedToken);
        return MessageDigest.isEqual(
                assinaturaEsperada.getBytes(StandardCharsets.UTF_8),
                assinaturaRecebida.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String assinar(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return BASE64_URL_ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Falha ao validar JWT.", exception);
        }
    }

    private AuthenticatedUser autenticarPayload(String payloadBase64) {
        try {
            Map<String, Object> payload = decodePayload(payloadBase64);
            long exp = ((Number) payload.get("exp")).longValue();
            if (Instant.now().getEpochSecond() >= exp) {
                throw new UnauthorizedException("Token JWT expirado.");
            }

            return new AuthenticatedUser(
                    Long.valueOf((String) payload.get("sub")),
                    (String) payload.get("email"),
                    TipoUsuario.valueOf((String) payload.get("tipo"))
            );
        } catch (UnauthorizedException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new UnauthorizedException("Token JWT invalido.");
        }
    }

    private Map<String, Object> decodePayload(String payload) {
        try {
            byte[] decoded = BASE64_URL_DECODER.decode(payload);
            return objectMapper.readValue(decoded, new TypeReference<>() {
            });
        } catch (Exception exception) {
            throw new UnauthorizedException("Token JWT invalido.");
        }
    }
}
