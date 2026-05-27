package com.doari.doacoes.service;

import com.doari.doacoes.client.NotificacaoClient;
import com.doari.doacoes.dto.AtenderPedidoRequest;
import com.doari.doacoes.dto.CompatibilidadeResponse;
import com.doari.doacoes.dto.DoacaoRequest;
import com.doari.doacoes.dto.DoacaoResponse;
import com.doari.doacoes.dto.NotificacaoRequest;
import com.doari.doacoes.dto.PedidoRequest;
import com.doari.doacoes.dto.PedidoResponse;
import com.doari.doacoes.model.Doacao;
import com.doari.doacoes.model.Pedido;
import com.doari.doacoes.model.StatusItem;
import com.doari.doacoes.repository.DoacaoRepository;
import com.doari.doacoes.repository.PedidoRepository;
import com.doari.doacoes.security.ForbiddenException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DoacaoService {

    private final DoacaoRepository doacaoRepository;
    private final PedidoRepository pedidoRepository;
    private final NotificacaoClient notificacaoClient;

    public DoacaoService(
            DoacaoRepository doacaoRepository,
            PedidoRepository pedidoRepository,
            NotificacaoClient notificacaoClient
    ) {
        this.doacaoRepository = doacaoRepository;
        this.pedidoRepository = pedidoRepository;
        this.notificacaoClient = notificacaoClient;
    }

    @Transactional
    public DoacaoResponse cadastrarDoacao(DoacaoRequest request, Long usuarioId) {
        Doacao doacao = new Doacao();
        preencherDoacao(doacao, request, usuarioId);
        Doacao salva = doacaoRepository.save(doacao);
        notificarPedidosCompativeis(salva);
        return toResponse(salva);
    }

    @Transactional(readOnly = true)
    public List<DoacaoResponse> listarDoacoesDisponiveis() {
        return doacaoRepository.findByStatus(StatusItem.DISPONIVEL)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DoacaoResponse> listarDoacoesDoUsuario(Long usuarioId) {
        return doacaoRepository.findByUsuarioIdAndPedidoIdIsNotNullAndStatusNot(usuarioId, StatusItem.REMOVIDO)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DoacaoResponse> listarDoacoesDaOng(Long ongId) {
        return doacaoRepository.findByOngIdAndStatusNot(ongId, StatusItem.REMOVIDO)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public DoacaoResponse editarDoacao(Long id, DoacaoRequest request, Long usuarioId) {
        Doacao doacao = doacaoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Doação não encontrada."));
        garantirDonoDaDoacao(doacao, usuarioId);
        preencherDoacao(doacao, request, usuarioId);
        doacao.marcarAtualizado();
        Doacao salva = doacaoRepository.save(doacao);
        notificarPedidosCompativeis(salva);
        return toResponse(salva);
    }

    @Transactional
    public void removerDoacao(Long id, Long usuarioId) {
        Doacao doacao = doacaoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Doação não encontrada."));
        garantirDonoDaDoacao(doacao, usuarioId);
        doacao.setStatus(StatusItem.REMOVIDO);
        doacao.marcarAtualizado();
        doacaoRepository.save(doacao);
    }

    @Transactional
    public void removerPedido(Long id, Long ongId) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Pedido não encontrado."));
        garantirDonoDoPedido(pedido, ongId);
        if (pedido.getStatus() != StatusItem.DISPONIVEL) {
            throw new IllegalArgumentException("Somente pedidos disponíveis podem ser excluídos.");
        }
        pedido.setStatus(StatusItem.REMOVIDO);
        pedidoRepository.save(pedido);
    }

    @Transactional
    public PedidoResponse cadastrarPedido(PedidoRequest request, Long ongId) {
        Pedido pedido = new Pedido();
        preencherPedido(pedido, request, ongId);
        Pedido salvo = pedidoRepository.save(pedido);
        notificarDoacoesCompativeis(salvo);
        return toResponse(salvo);
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPedidosDisponiveis() {
        return pedidoRepository.findByStatus(StatusItem.DISPONIVEL)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPedidosDaOng(Long ongId) {
        return pedidoRepository.findByOngIdAndStatusNot(ongId, StatusItem.REMOVIDO)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public DoacaoResponse atenderPedido(Long pedidoId, AtenderPedidoRequest request, Long usuarioId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new NoSuchElementException("Pedido não encontrado."));

        if (pedido.getStatus() != StatusItem.DISPONIVEL) {
            throw new IllegalArgumentException("Este pedido não está mais disponível.");
        }

        Doacao doacao = new Doacao();
        doacao.setUsuarioId(usuarioId);
        doacao.setOngId(pedido.getOngId());
        doacao.setPedidoId(pedido.getId());
        doacao.setItem(pedido.getItem());
        doacao.setQuantidade(juntarQuantidade(request.quantidade(), request.unidade()));
        doacao.setDescricao(montarDescricaoAtendimento(request));
        doacao.setStatus(StatusItem.SOLICITADA);

        pedido.setStatus(StatusItem.SOLICITADA);

        Doacao salva = doacaoRepository.save(doacao);
        pedidoRepository.save(pedido);

        notificacaoClient.enviar(new NotificacaoRequest(
                "SOLICITACAO",
                "ONG",
                pedido.getOngId(),
                salva.getId(),
                pedido.getId(),
                "Um doador quer atender seu pedido: " + pedido.getItem()
        ));

        return toResponse(salva);
    }

    @Transactional
    public DoacaoResponse confirmarDoacao(Long doacaoId, Long ongId) {
        Doacao doacao = buscarDoacaoDaOng(doacaoId, ongId);
        Pedido pedido = buscarPedidoVinculado(doacao);
        doacao.setStatus(StatusItem.CONFIRMADA);
        doacao.marcarAtualizado();
        pedido.setStatus(StatusItem.CONFIRMADA);
        pedidoRepository.save(pedido);
        return toResponse(doacaoRepository.save(doacao));
    }

    @Transactional
    public DoacaoResponse recusarDoacao(Long doacaoId, Long ongId) {
        Doacao doacao = buscarDoacaoDaOng(doacaoId, ongId);
        Pedido pedido = buscarPedidoVinculado(doacao);
        doacao.setStatus(StatusItem.RECUSADA);
        doacao.marcarAtualizado();
        pedido.setStatus(StatusItem.DISPONIVEL);
        pedidoRepository.save(pedido);
        return toResponse(doacaoRepository.save(doacao));
    }

    @Transactional
    public DoacaoResponse entregarDoacao(Long doacaoId, Long ongId) {
        Doacao doacao = buscarDoacaoDaOng(doacaoId, ongId);
        Pedido pedido = buscarPedidoVinculado(doacao);
        if (doacao.getStatus() != StatusItem.CONFIRMADA) {
            throw new IllegalArgumentException("Somente doações confirmadas podem ser marcadas como entregues.");
        }
        doacao.setStatus(StatusItem.ENTREGUE);
        doacao.marcarAtualizado();
        pedido.setStatus(StatusItem.ENTREGUE);
        pedidoRepository.save(pedido);
        return toResponse(doacaoRepository.save(doacao));
    }

    @Transactional(readOnly = true)
    public List<CompatibilidadeResponse> listarCompatibilidades() {
        List<CompatibilidadeResponse> compatibilidades = new ArrayList<>();
        List<Doacao> doacoes = doacaoRepository.findByStatus(StatusItem.DISPONIVEL);

        for (Doacao doacao : doacoes) {
            List<Pedido> pedidos = pedidoRepository.findByItemIgnoreCaseAndStatus(
                    doacao.getItem(),
                    StatusItem.DISPONIVEL
            );

            for (Pedido pedido : pedidos) {
                compatibilidades.add(toCompatibilidade(doacao, pedido));
            }
        }

        return compatibilidades;
    }

    private void preencherDoacao(Doacao doacao, DoacaoRequest request, Long usuarioId) {
        doacao.setUsuarioId(usuarioId);
        doacao.setItem(normalizarItem(request.item()));
        doacao.setQuantidade(request.quantidade());
        doacao.setDescricao(request.descricao());
    }

    private void preencherPedido(Pedido pedido, PedidoRequest request, Long ongId) {
        pedido.setOngId(ongId);
        pedido.setItem(normalizarItem(request.item()));
        pedido.setQuantidade(request.quantidade());
        pedido.setDescricao(request.descricao());
    }

    private String normalizarItem(String item) {
        return item.trim();
    }

    private void garantirDonoDaDoacao(Doacao doacao, Long usuarioId) {
        if (!doacao.getUsuarioId().equals(usuarioId)) {
            throw new ForbiddenException("Doação pertence a outro usuário.");
        }
    }

    private void garantirDonoDoPedido(Pedido pedido, Long ongId) {
        if (!pedido.getOngId().equals(ongId)) {
            throw new ForbiddenException("Pedido pertence a outra ONG.");
        }
    }

    private Doacao buscarDoacaoDaOng(Long doacaoId, Long ongId) {
        Doacao doacao = doacaoRepository.findById(doacaoId)
                .orElseThrow(() -> new NoSuchElementException("Doação não encontrada."));
        if (doacao.getOngId() == null || !doacao.getOngId().equals(ongId)) {
            throw new ForbiddenException("Doação pertence a outra ONG.");
        }
        return doacao;
    }

    private Pedido buscarPedidoVinculado(Doacao doacao) {
        if (doacao.getPedidoId() == null) {
            throw new IllegalArgumentException("Doação não está vinculada a um pedido.");
        }
        return pedidoRepository.findById(doacao.getPedidoId())
                .orElseThrow(() -> new NoSuchElementException("Pedido não encontrado."));
    }

    private void notificarPedidosCompativeis(Doacao doacao) {
        List<Pedido> pedidos = pedidoRepository.findByItemIgnoreCaseAndStatus(
                doacao.getItem(),
                StatusItem.DISPONIVEL
        );

        for (Pedido pedido : pedidos) {
            notificacaoClient.enviar(new NotificacaoRequest(
                    "COMPATIBILIDADE",
                    "ONG",
                    pedido.getOngId(),
                    doacao.getId(),
                    pedido.getId(),
                    "Uma doação disponível combina com seu pedido: " + doacao.getItem()
            ));
        }
    }

    private void notificarDoacoesCompativeis(Pedido pedido) {
        List<Doacao> doacoes = doacaoRepository.findByItemIgnoreCaseAndStatus(
                pedido.getItem(),
                StatusItem.DISPONIVEL
        );

        for (Doacao doacao : doacoes) {
            notificacaoClient.enviar(new NotificacaoRequest(
                    "COMPATIBILIDADE",
                    "DOADOR",
                    doacao.getUsuarioId(),
                    doacao.getId(),
                    pedido.getId(),
                    "Uma ONG precisa de um item que você cadastrou: " + pedido.getItem()
            ));
        }
    }

    private DoacaoResponse toResponse(Doacao doacao) {
        return new DoacaoResponse(
                doacao.getId(),
                doacao.getUsuarioId(),
                doacao.getOngId(),
                doacao.getPedidoId(),
                doacao.getItem(),
                doacao.getQuantidade(),
                doacao.getDescricao(),
                doacao.getStatus(),
                doacao.getCriadoEm(),
                doacao.getAtualizadoEm()
        );
    }

    private PedidoResponse toResponse(Pedido pedido) {
        return new PedidoResponse(
                pedido.getId(),
                pedido.getOngId(),
                pedido.getItem(),
                pedido.getQuantidade(),
                pedido.getDescricao(),
                pedido.getStatus(),
                pedido.getCriadoEm()
        );
    }

    private CompatibilidadeResponse toCompatibilidade(Doacao doacao, Pedido pedido) {
        return new CompatibilidadeResponse(
                doacao.getId(),
                pedido.getId(),
                doacao.getUsuarioId(),
                pedido.getOngId(),
                doacao.getItem()
        );
    }

    private String juntarQuantidade(String quantidade, String unidade) {
        if (quantidade == null || quantidade.isBlank()) {
            return "";
        }
        if (unidade == null || unidade.isBlank()) {
            return quantidade;
        }
        return quantidade + " " + unidade;
    }

    private String montarDescricaoAtendimento(AtenderPedidoRequest request) {
        StringBuilder descricao = new StringBuilder();
        if (request.mensagem() != null && !request.mensagem().isBlank()) {
            descricao.append(request.mensagem().trim()).append(" | ");
        }
        descricao.append("Nome: ")
                .append(request.nomeContato().trim())
                .append(" | E-mail: ")
                .append(request.emailContato().trim())
                .append(" | Telefone: ")
                .append(request.telefoneContato().trim());
        return descricao.toString();
    }
}
