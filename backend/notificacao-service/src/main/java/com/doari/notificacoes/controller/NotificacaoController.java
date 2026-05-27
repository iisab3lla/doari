package com.doari.notificacoes.controller;

import com.doari.notificacoes.dto.CriarNotificacaoRequest;
import com.doari.notificacoes.dto.NotificacaoResponse;
import com.doari.notificacoes.model.DestinatarioTipo;
import com.doari.notificacoes.security.AuthenticatedUser;
import com.doari.notificacoes.security.ForbiddenException;
import com.doari.notificacoes.security.InternalTokenService;
import com.doari.notificacoes.security.JwtService;
import com.doari.notificacoes.service.NotificacaoService;
import com.doari.notificacoes.service.SseNotificacaoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/notificacoes")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;
    private final SseNotificacaoService sseNotificacaoService;
    private final JwtService jwtService;
    private final InternalTokenService internalTokenService;

    public NotificacaoController(
            NotificacaoService notificacaoService,
            SseNotificacaoService sseNotificacaoService,
            JwtService jwtService,
            InternalTokenService internalTokenService
    ) {
        this.notificacaoService = notificacaoService;
        this.sseNotificacaoService = sseNotificacaoService;
        this.jwtService = jwtService;
        this.internalTokenService = internalTokenService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificacaoResponse criar(
            @RequestHeader(value = "X-Internal-Token", required = false) String internalToken,
            @Valid @RequestBody CriarNotificacaoRequest request
    ) {
        internalTokenService.validar(internalToken);
        return notificacaoService.criar(request);
    }

    @GetMapping
    public List<NotificacaoResponse> listar(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam DestinatarioTipo destinatarioTipo,
            @RequestParam Long destinatarioId
    ) {
        AuthenticatedUser usuario = jwtService.autenticarHeader(authorization);
        garantirMesmoDestinatario(usuario, destinatarioTipo, destinatarioId);
        return notificacaoService.listar(destinatarioTipo, destinatarioId);
    }

    @PostMapping("/{id}/lida")
    public NotificacaoResponse marcarComoLida(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id
    ) {
        AuthenticatedUser usuario = jwtService.autenticarHeader(authorization);
        return notificacaoService.marcarComoLida(id, usuario.destinatarioTipo(), usuario.id());
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(
            @RequestParam String token,
            @RequestParam DestinatarioTipo destinatarioTipo,
            @RequestParam Long destinatarioId
    ) {
        AuthenticatedUser usuario = jwtService.autenticarToken(token);
        garantirMesmoDestinatario(usuario, destinatarioTipo, destinatarioId);
        return sseNotificacaoService.conectar(destinatarioTipo, destinatarioId);
    }

    private void garantirMesmoDestinatario(
            AuthenticatedUser usuario,
            DestinatarioTipo destinatarioTipo,
            Long destinatarioId
    ) {
        if (usuario.destinatarioTipo() != destinatarioTipo || !usuario.id().equals(destinatarioId)) {
            throw new ForbiddenException("Não é permitido acessar notificações de outro destinatário.");
        }
    }
}
