package com.doari.doacoes.controller;

import com.doari.doacoes.dto.AtenderPedidoRequest;
import com.doari.doacoes.dto.PedidoRequest;
import com.doari.doacoes.dto.PedidoResponse;
import com.doari.doacoes.security.AuthenticatedUser;
import com.doari.doacoes.security.ForbiddenException;
import com.doari.doacoes.security.JwtService;
import com.doari.doacoes.security.TipoUsuario;
import com.doari.doacoes.service.DoacaoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final DoacaoService doacaoService;
    private final JwtService jwtService;

    public PedidoController(DoacaoService doacaoService, JwtService jwtService) {
        this.doacaoService = doacaoService;
        this.jwtService = jwtService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponse cadastrar(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody PedidoRequest request
    ) {
        AuthenticatedUser usuario = jwtService.autenticar(authorization);
        usuario.requireTipo(TipoUsuario.ONG);
        return doacaoService.cadastrarPedido(request, usuario.id());
    }

    @GetMapping
    public List<PedidoResponse> listarDisponiveis(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        jwtService.autenticar(authorization);
        return doacaoService.listarPedidosDisponiveis();
    }

    @GetMapping("/ong/{ongId}")
    public List<PedidoResponse> listarDaOng(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long ongId
    ) {
        AuthenticatedUser usuario = jwtService.autenticar(authorization);
        usuario.requireTipo(TipoUsuario.ONG);
        if (!usuario.id().equals(ongId)) {
            throw new ForbiddenException("Não é permitido listar pedidos de outra ONG.");
        }
        return doacaoService.listarPedidosDaOng(ongId);
    }

    @PostMapping("/{pedidoId}/atender")
    @ResponseStatus(HttpStatus.CREATED)
    public void atender(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long pedidoId,
            @Valid @RequestBody AtenderPedidoRequest request
    ) {
        AuthenticatedUser usuario = jwtService.autenticar(authorization);
        usuario.requireTipo(TipoUsuario.DOADOR);
        doacaoService.atenderPedido(pedidoId, request, usuario.id());
    }

    @DeleteMapping("/{pedidoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long pedidoId
    ) {
        AuthenticatedUser usuario = jwtService.autenticar(authorization);
        usuario.requireTipo(TipoUsuario.ONG);
        doacaoService.removerPedido(pedidoId, usuario.id());
    }
}
