package com.doari.usuarios.service;

import com.doari.usuarios.dto.TokenValidationResponse;
import com.doari.usuarios.model.TipoUsuario;
import com.doari.usuarios.model.Usuario;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();

    private final ObjectMapper objectMapper;
    private final byte[] secret;
    private final long expirationSeconds;

    public JwtService(
            ObjectMapper objectMapper,
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-seconds}") long expirationSeconds
    ) {
        this.objectMapper = objectMapper;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationSeconds = expirationSeconds;
    }

    public String gerarToken(Usuario usuario) {
        Instant agora = Instant.now();
        Instant expiracao = agora.plusSeconds(expirationSeconds);

        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", usuario.getId().toString());
        payload.put("email", usuario.getEmail());
        payload.put("tipo", usuario.getTipo().name());
        payload.put("iat", agora.getEpochSecond());
        payload.put("exp", expiracao.getEpochSecond());

        String unsignedToken = encodeJson(header) + "." + encodeJson(payload);
        return unsignedToken + "." + assinar(unsignedToken);
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    public TokenValidationResponse validar(String token) {
        try {
            String[] partes = token.split("\\.");
            if (partes.length != 3) {
                return invalido();
            }

            String unsignedToken = partes[0] + "." + partes[1];
            if (!assinar(unsignedToken).equals(partes[2])) {
                return invalido();
            }

            Map<String, Object> payload = decodeJson(partes[1]);
            long exp = ((Number) payload.get("exp")).longValue();
            if (Instant.now().getEpochSecond() >= exp) {
                return invalido();
            }

            Long usuarioId = Long.valueOf((String) payload.get("sub"));
            String email = (String) payload.get("email");
            TipoUsuario tipo = TipoUsuario.valueOf((String) payload.get("tipo"));

            return new TokenValidationResponse(true, usuarioId, email, tipo);
        } catch (RuntimeException exception) {
            return invalido();
        }
    }

    private String encodeJson(Map<String, Object> value) {
        try {
            return BASE64_URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Falha ao gerar JWT.", exception);
        }
    }

    private Map<String, Object> decodeJson(String value) {
        try {
            byte[] decoded = BASE64_URL_DECODER.decode(value);
            return objectMapper.readValue(decoded, new TypeReference<>() {
            });
        } catch (Exception exception) {
            throw new IllegalArgumentException("JWT invalido.", exception);
        }
    }

    private String assinar(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return BASE64_URL_ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Falha ao assinar JWT.", exception);
        }
    }

    private TokenValidationResponse invalido() {
        return new TokenValidationResponse(false, null, null, null);
    }
}
