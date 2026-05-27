package com.example.doari.controller;

import com.example.doari.client.ApiException;
import com.example.doari.client.DoacoesClient;
import com.example.doari.client.NotificacoesClient;
import com.example.doari.client.UsuariosClient;
import com.example.doari.dto.AtenderPedidoRequest;
import com.example.doari.dto.CadastroUsuarioRequest;
import com.example.doari.dto.DoacaoRequest;
import com.example.doari.dto.LoginRequest;
import com.example.doari.dto.PedidoRequest;
import com.example.doari.dto.PedidoResponse;
import com.example.doari.dto.UsuarioResponse;
import com.example.doari.session.SessaoUsuario;
import com.example.doari.view.DoacaoView;
import com.example.doari.view.NotificacaoView;
import com.example.doari.view.PedidoView;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WebController {

    private static final String SESSAO_USUARIO = "usuarioLogado";

    private final UsuariosClient usuariosClient;
    private final DoacoesClient doacoesClient;
    private final NotificacoesClient notificacoesClient;
    private final String notificacoesUrl;

    public WebController(
            UsuariosClient usuariosClient,
            DoacoesClient doacoesClient,
            NotificacoesClient notificacoesClient,
            @Value("${doari.public.notificacoes-url}") String notificacoesUrl
    ) {
        this.usuariosClient = usuariosClient;
        this.doacoesClient = doacoesClient;
        this.notificacoesClient = notificacoesClient;
        this.notificacoesUrl = notificacoesUrl;
    }

    @GetMapping("/")
    public String index(HttpSession session) {
        SessaoUsuario usuario = usuarioLogado(session);
        if (usuario == null) {
            return "redirect:/login";
        }
        return usuario.isOng() ? "redirect:/painel-ong" : "redirect:/painel-doador";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String fazerLogin(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        try {
            var auth = usuariosClient.login(new LoginRequest(email.trim(), password.trim()));
            SessaoUsuario usuario = new SessaoUsuario(
                    auth.usuarioId(),
                    auth.nome(),
                    auth.email(),
                    auth.tipo(),
                    auth.accessToken()
            );
            session.setAttribute(SESSAO_USUARIO, usuario);
            return usuario.isOng() ? "redirect:/painel-ong" : "redirect:/painel-doador";
        } catch (ApiException exception) {
            redirectAttributes.addFlashAttribute("erro", exception.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/cadastro")
    public String cadastro(
            @RequestParam(defaultValue = "ong") String tipo,
            Model model,
            HttpSession session
    ) {
        session.removeAttribute(SESSAO_USUARIO);
        model.addAttribute("tipo", tipo);
        return "cadastro";
    }

    @PostMapping("/cadastro/doador")
    public String cadastrarDoador(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String phone,
            @RequestParam String city,
            @RequestParam String state,
            RedirectAttributes redirectAttributes
    ) {
        return cadastrarUsuario(
                new CadastroUsuarioRequest(fullName.trim(), email.trim(), password.trim(), phone.trim(), city.trim() + "/" + state.trim(), "DOADOR"),
                redirectAttributes
        );
    }

    @PostMapping("/cadastro/ong")
    public String cadastrarOng(
            @RequestParam String ongName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String phone,
            @RequestParam String cnpj,
            @RequestParam String city,
            @RequestParam String state,
            RedirectAttributes redirectAttributes
    ) {
        return cadastrarUsuario(
                new CadastroUsuarioRequest(ongName.trim(), email.trim(), password.trim(), phone.trim(), city.trim() + "/" + state.trim(), "ONG"),
                redirectAttributes
        );
    }

    @GetMapping("/painel-doador")
    public String painelDoador(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        SessaoUsuario usuario = usuarioLogado(session);
        if (usuario == null) {
            return "redirect:/login";
        }
        if (!usuario.isDoador()) {
            return "redirect:/painel-ong";
        }

        try {
            var doacoesResponse = doacoesClient.listarDoacoesDoUsuario(usuario.accessToken(), usuario.id());
            List<PedidoResponse> pedidosResponse = doacoesClient.listarPedidosDisponiveis(usuario.accessToken());
            Set<Long> ongIds = pedidosResponse.stream()
                    .map(PedidoResponse::ongId)
                    .collect(Collectors.toSet());
            ongIds.addAll(doacoesResponse.stream()
                    .map(com.example.doari.dto.DoacaoResponse::ongId)
                    .filter(id -> id != null)
                    .collect(Collectors.toSet()));
            Map<Long, UsuarioResponse> perfisOng = carregarPerfisOng(usuario.accessToken(), ongIds);
            List<DoacaoView> doacoes = doacoesResponse.stream()
                    .map(doacao -> DoacaoView.from(doacao, nomeOng(perfisOng, doacao.ongId())))
                    .toList();
            List<PedidoView> pedidos = pedidosResponse.stream()
                    .map(pedido -> PedidoView.from(pedido, nomeOng(perfisOng, pedido.ongId()), cidadeOng(perfisOng, pedido.ongId())))
                    .toList();
            popularModeloBase(model, usuario, "DOADOR");
            model.addAttribute("nomeDoador", usuario.nome());
            model.addAttribute("doacoes", doacoes);
            model.addAttribute("pedidos", pedidos);
            model.addAttribute("notificacoes", carregarNotificacoes(usuario));
            return "painel-doador";
        } catch (ApiException exception) {
            redirectAttributes.addFlashAttribute("erro", exception.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/painel-ong")
    public String painelOng(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        SessaoUsuario usuario = usuarioLogado(session);
        if (usuario == null) {
            return "redirect:/login";
        }
        if (!usuario.isOng()) {
            return "redirect:/painel-doador";
        }

        try {
            List<DoacaoView> doacoesDisponiveis = doacoesClient.listarDoacoesDisponiveis(usuario.accessToken())
                    .stream()
                    .map(DoacaoView::from)
                    .toList();
            List<DoacaoView> doacoesRecebidas = doacoesClient.listarDoacoesDaOng(usuario.accessToken(), usuario.id())
                    .stream()
                    .map(DoacaoView::fromOng)
                    .toList();
            List<PedidoView> pedidos = doacoesClient.listarPedidosDaOng(usuario.accessToken(), usuario.id())
                    .stream()
                    .map(PedidoView::from)
                    .toList();
            popularModeloBase(model, usuario, "ONG");
            model.addAttribute("nomeOng", usuario.nome());
            model.addAttribute("doacoesDisponiveis", doacoesDisponiveis);
            model.addAttribute("doacoesRecebidas", doacoesRecebidas);
            model.addAttribute("pedidos", pedidos);
            model.addAttribute("notificacoes", carregarNotificacoes(usuario));
            return "painel-ong";
        } catch (ApiException exception) {
            redirectAttributes.addFlashAttribute("erro", exception.getMessage());
            return "redirect:/login";
        }
    }

    @PostMapping("/doacoes")
    public String cadastrarDoacao(
            @RequestParam String tipoDoacao,
            @RequestParam(required = false) String item,
            @RequestParam(required = false) String quantidade,
            @RequestParam(required = false) String unidade,
            @RequestParam(required = false) String valor,
            @RequestParam(required = false) String mensagemFisico,
            @RequestParam(required = false) String mensagemDinheiro,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        SessaoUsuario usuario = exigirUsuario(session);
        if (!usuario.isDoador()) {
            return "redirect:/painel-ong";
        }

        try {
            String itemDoado = "dinheiro".equals(tipoDoacao) ? "Dinheiro" : item;
            String quantidadeDoada = "dinheiro".equals(tipoDoacao) ? valor : juntarQuantidade(quantidade, unidade);
            String descricao = "dinheiro".equals(tipoDoacao) ? mensagemDinheiro : mensagemFisico;
            doacoesClient.cadastrarDoacao(usuario.accessToken(), new DoacaoRequest(itemDoado, quantidadeDoada, descricao));
            redirectAttributes.addFlashAttribute("mensagem", "Doação cadastrada com sucesso.");
        } catch (ApiException exception) {
            redirectAttributes.addFlashAttribute("erro", exception.getMessage());
        }

        return "redirect:/painel-doador";
    }

    @PostMapping("/doacoes/excluir")
    public String excluirDoacao(
            @RequestParam Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        SessaoUsuario usuario = exigirUsuario(session);
        if (!usuario.isDoador()) {
            return "redirect:/painel-ong";
        }

        try {
            doacoesClient.removerDoacao(usuario.accessToken(), id);
            redirectAttributes.addFlashAttribute("mensagem", "Doação excluída com sucesso.");
        } catch (ApiException exception) {
            redirectAttributes.addFlashAttribute("erro", exception.getMessage());
        }

        return "redirect:/painel-doador";
    }

    @PostMapping("/doacoes/confirmar")
    public String confirmarDoacao(
            @RequestParam Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        return alterarStatusDoacaoOng(id, session, redirectAttributes, "confirmar");
    }

    @PostMapping("/doacoes/recusar")
    public String recusarDoacao(
            @RequestParam Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        return alterarStatusDoacaoOng(id, session, redirectAttributes, "recusar");
    }

    @PostMapping("/doacoes/entregar")
    public String entregarDoacao(
            @RequestParam Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        return alterarStatusDoacaoOng(id, session, redirectAttributes, "entregar");
    }

    @PostMapping("/pedidos")
    public String cadastrarPedido(
            @RequestParam String item,
            @RequestParam(required = false) String quantidade,
            @RequestParam(required = false) String descricao,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        SessaoUsuario usuario = exigirUsuario(session);
        if (!usuario.isOng()) {
            return "redirect:/painel-doador";
        }

        try {
            doacoesClient.cadastrarPedido(usuario.accessToken(), new PedidoRequest(item, quantidade, descricao));
            redirectAttributes.addFlashAttribute("mensagem", "Pedido cadastrado com sucesso.");
        } catch (ApiException exception) {
            redirectAttributes.addFlashAttribute("erro", exception.getMessage());
        }

        return "redirect:/painel-ong";
    }

    @PostMapping("/pedidos/excluir")
    public String excluirPedido(
            @RequestParam Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        SessaoUsuario usuario = exigirUsuario(session);
        if (!usuario.isOng()) {
            return "redirect:/painel-doador";
        }

        try {
            doacoesClient.removerPedido(usuario.accessToken(), id);
            redirectAttributes.addFlashAttribute("mensagem", "Pedido excluido com sucesso.");
        } catch (ApiException exception) {
            redirectAttributes.addFlashAttribute("erro", exception.getMessage());
        }

        return "redirect:/painel-ong";
    }

    @PostMapping("/pedidos/atender")
    public String atenderPedido(
            @RequestParam Long pedidoId,
            @RequestParam String quantidade,
            @RequestParam(required = false) String unidade,
            @RequestParam(required = false) String mensagem,
            @RequestParam String nomeContato,
            @RequestParam String emailContato,
            @RequestParam String telefoneContato,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        SessaoUsuario usuario = exigirUsuario(session);
        if (!usuario.isDoador()) {
            return "redirect:/painel-ong";
        }

        try {
            doacoesClient.atenderPedido(
                    usuario.accessToken(),
                    pedidoId,
                    new AtenderPedidoRequest(quantidade, unidade, mensagem, nomeContato, emailContato, telefoneContato)
            );
            redirectAttributes.addFlashAttribute("mensagem", "Pedido atendido. A ONG foi notificada para confirmar sua doação.");
        } catch (ApiException exception) {
            redirectAttributes.addFlashAttribute("erro", exception.getMessage());
        }

        return "redirect:/painel-doador";
    }

    private String cadastrarUsuario(CadastroUsuarioRequest request, RedirectAttributes redirectAttributes) {
        try {
            usuariosClient.cadastrar(request);
            redirectAttributes.addFlashAttribute("mensagem", "Cadastro realizado com sucesso. Faça login para continuar.");
            return "redirect:/login";
        } catch (ApiException exception) {
            redirectAttributes.addFlashAttribute("erro", exception.getMessage());
            return "redirect:/cadastro?tipo=" + request.tipo().toLowerCase();
        }
    }

    private String alterarStatusDoacaoOng(
            Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            String acao
    ) {
        SessaoUsuario usuario = exigirUsuario(session);
        if (!usuario.isOng()) {
            return "redirect:/painel-doador";
        }

        try {
            if ("confirmar".equals(acao)) {
                doacoesClient.confirmarDoacao(usuario.accessToken(), id);
                redirectAttributes.addFlashAttribute("mensagem", "Doação confirmada.");
            }
            if ("recusar".equals(acao)) {
                doacoesClient.recusarDoacao(usuario.accessToken(), id);
                redirectAttributes.addFlashAttribute("mensagem", "Doação recusada.");
            }
            if ("entregar".equals(acao)) {
                doacoesClient.entregarDoacao(usuario.accessToken(), id);
                redirectAttributes.addFlashAttribute("mensagem", "Doação marcada como entregue.");
            }
        } catch (ApiException exception) {
            redirectAttributes.addFlashAttribute("erro", exception.getMessage());
        }

        return "redirect:/painel-ong";
    }

    private List<NotificacaoView> carregarNotificacoes(SessaoUsuario usuario) {
        return notificacoesClient.listar(usuario.accessToken(), usuario.tipo(), usuario.id())
                .stream()
                .map(NotificacaoView::from)
                .toList();
    }

    private Map<Long, UsuarioResponse> carregarPerfisOng(String token, Set<Long> ongIds) {
        return ongIds.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        id -> {
                            try {
                                return usuariosClient.buscarPerfil(token, id);
                            } catch (ApiException exception) {
                                return new UsuarioResponse(id, "ONG #" + id, "", "", "", "ONG", "");
                            }
                        }
                ));
    }

    private String nomeOng(Map<Long, UsuarioResponse> perfisOng, Long ongId) {
        if (ongId == null || !perfisOng.containsKey(ongId)) {
            return null;
        }
        return perfisOng.get(ongId).nome();
    }

    private String cidadeOng(Map<Long, UsuarioResponse> perfisOng, Long ongId) {
        if (ongId == null || !perfisOng.containsKey(ongId)) {
            return "Não informada";
        }

        String documento = perfisOng.get(ongId).documento();
        if (documento == null || !documento.contains("/")) {
            return "Não informada";
        }

        return documento.replace("/", " - ");
    }

    private void popularModeloBase(Model model, SessaoUsuario usuario, String destinatarioTipo) {
        model.addAttribute("usuarioId", usuario.id());
        model.addAttribute("destinatarioTipo", destinatarioTipo);
        model.addAttribute("accessToken", usuario.accessToken());
        model.addAttribute("notificacoesUrl", notificacoesUrl);
    }

    private SessaoUsuario exigirUsuario(HttpSession session) {
        SessaoUsuario usuario = usuarioLogado(session);
        if (usuario == null) {
            throw new ApiException("Sessão expirada. Faça login novamente.");
        }
        return usuario;
    }

    private SessaoUsuario usuarioLogado(HttpSession session) {
        Object value = session.getAttribute(SESSAO_USUARIO);
        if (value instanceof SessaoUsuario usuario) {
            return usuario;
        }
        return null;
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
}
