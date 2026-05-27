package com.doari.notificacoes.service;

import com.doari.notificacoes.dto.NotificacaoResponse;
import com.doari.notificacoes.model.DestinatarioTipo;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class SseNotificacaoService {

    private static final long TIMEOUT_MILLIS = 30L * 60L * 1000L;

    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter conectar(DestinatarioTipo destinatarioTipo, Long destinatarioId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MILLIS);
        String key = key(destinatarioTipo, destinatarioId);

        emitters.computeIfAbsent(key, ignored -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> remover(key, emitter));
        emitter.onTimeout(() -> remover(key, emitter));
        emitter.onError(ignored -> remover(key, emitter));

        try {
            emitter.send(SseEmitter.event()
                    .name("conectado")
                    .data("stream de notificações conectado"));
        } catch (IOException exception) {
            remover(key, emitter);
        }

        return emitter;
    }

    public void publicar(NotificacaoResponse notificacao) {
        String key = key(notificacao.destinatarioTipo(), notificacao.destinatarioId());
        List<SseEmitter> destino = emitters.getOrDefault(key, List.of());

        for (SseEmitter emitter : destino) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notificacao")
                        .data(notificacao));
            } catch (IOException exception) {
                remover(key, emitter);
            }
        }
    }

    private void remover(String key, SseEmitter emitter) {
        List<SseEmitter> destino = emitters.get(key);
        if (destino == null) {
            return;
        }

        destino.remove(emitter);
        if (destino.isEmpty()) {
            emitters.remove(key);
        }
    }

    private String key(DestinatarioTipo destinatarioTipo, Long destinatarioId) {
        return destinatarioTipo.name() + ":" + destinatarioId;
    }
}
