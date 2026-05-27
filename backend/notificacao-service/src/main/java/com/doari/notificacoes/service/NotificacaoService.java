package com.doari.notificacoes.service;

import com.doari.notificacoes.dto.CriarNotificacaoRequest;
import com.doari.notificacoes.dto.NotificacaoResponse;
import com.doari.notificacoes.model.DestinatarioTipo;
import com.doari.notificacoes.model.Notificacao;
import com.doari.notificacoes.repository.NotificacaoRepository;
import com.doari.notificacoes.security.ForbiddenException;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;
    private final SseNotificacaoService sseNotificacaoService;

    public NotificacaoService(
            NotificacaoRepository notificacaoRepository,
            SseNotificacaoService sseNotificacaoService
    ) {
        this.notificacaoRepository = notificacaoRepository;
        this.sseNotificacaoService = sseNotificacaoService;
    }

    @Transactional
    public NotificacaoResponse criar(CriarNotificacaoRequest request) {
        Notificacao notificacao = new Notificacao();
        notificacao.setTipo(request.tipo());
        notificacao.setDestinatarioTipo(request.destinatarioTipo());
        notificacao.setDestinatarioId(request.destinatarioId());
        notificacao.setDoacaoId(request.doacaoId());
        notificacao.setPedidoId(request.pedidoId());
        notificacao.setMensagem(request.mensagem());

        NotificacaoResponse response = toResponse(notificacaoRepository.save(notificacao));
        sseNotificacaoService.publicar(response);
        return response;
    }

    @Transactional(readOnly = true)
    public List<NotificacaoResponse> listar(DestinatarioTipo destinatarioTipo, Long destinatarioId) {
        return notificacaoRepository
                .findByDestinatarioTipoAndDestinatarioIdOrderByCriadaEmDesc(destinatarioTipo, destinatarioId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public NotificacaoResponse marcarComoLida(Long id, DestinatarioTipo destinatarioTipo, Long destinatarioId) {
        Notificacao notificacao = notificacaoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Notificação não encontrada."));
        garantirDestinatario(notificacao, destinatarioTipo, destinatarioId);
        notificacao.setLida(true);
        return toResponse(notificacaoRepository.save(notificacao));
    }

    private void garantirDestinatario(
            Notificacao notificacao,
            DestinatarioTipo destinatarioTipo,
            Long destinatarioId
    ) {
        if (notificacao.getDestinatarioTipo() != destinatarioTipo
                || !notificacao.getDestinatarioId().equals(destinatarioId)) {
            throw new ForbiddenException("Notificação pertence a outro destinatário.");
        }
    }

    private NotificacaoResponse toResponse(Notificacao notificacao) {
        return new NotificacaoResponse(
                notificacao.getId(),
                notificacao.getTipo(),
                notificacao.getDestinatarioTipo(),
                notificacao.getDestinatarioId(),
                notificacao.getDoacaoId(),
                notificacao.getPedidoId(),
                notificacao.getMensagem(),
                notificacao.isLida(),
                notificacao.getCriadaEm()
        );
    }
}
