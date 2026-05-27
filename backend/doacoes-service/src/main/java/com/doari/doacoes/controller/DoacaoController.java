package com.doari.doacoes.controller;

import com.doari.doacoes.dto.DoacaoRequest;
import com.doari.doacoes.dto.DoacaoResponse;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/doacoes")
public class DoacaoController {

    private final DoacaoService doacaoService;
    private final JwtService jwtService;

    public DoacaoController(DoacaoService doacaoService, JwtService jwtService) {
        this.doacaoService = doacaoService;
        this.jwtService = jwtService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DoacaoResponse cadastrar(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody DoacaoRequest request
    ) {
        AuthenticatedUser usuario = jwtService.autenticar(authorization);
        usuario.requireTipo(TipoUsuario.DOADOR);
        return doacaoService.cadastrarDoacao(request, usuario.id());
    }

    @GetMapping
    public List<DoacaoResponse> listarDisponiveis(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        jwtService.autenticar(authorization);
        return doacaoService.listarDoacoesDisponiveis();
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<DoacaoResponse> listarDoUsuario(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long usuarioId
    ) {
        AuthenticatedUser usuario = jwtService.autenticar(authorization);
        usuario.requireTipo(TipoUsuario.DOADOR);
        if (!usuario.id().equals(usuarioId)) {
            throw new ForbiddenException("Não é permitido listar doações de outro usuário.");
        }
        return doacaoService.listarDoacoesDoUsuario(usuarioId);
    }

    @GetMapping("/ong/{ongId}")
    public List<DoacaoResponse> listarDaOng(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long ongId
    ) {
        AuthenticatedUser usuario = jwtService.autenticar(authorization);
        usuario.requireTipo(TipoUsuario.ONG);
        if (!usuario.id().equals(ongId)) {
            throw new ForbiddenException("Não é permitido listar doações de outra ONG.");
        }
        return doacaoService.listarDoacoesDaOng(ongId);
    }

    @PutMapping("/{id}")
    public DoacaoResponse editar(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id,
            @Valid @RequestBody DoacaoRequest request
    ) {
        AuthenticatedUser usuario = jwtService.autenticar(authorization);
        usuario.requireTipo(TipoUsuario.DOADOR);
        return doacaoService.editarDoacao(id, request, usuario.id());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id
    ) {
        AuthenticatedUser usuario = jwtService.autenticar(authorization);
        usuario.requireTipo(TipoUsuario.DOADOR);
        doacaoService.removerDoacao(id, usuario.id());
    }

    @PostMapping("/{id}/confirmar")
    public DoacaoResponse confirmar(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id
    ) {
        AuthenticatedUser usuario = jwtService.autenticar(authorization);
        usuario.requireTipo(TipoUsuario.ONG);
        return doacaoService.confirmarDoacao(id, usuario.id());
    }

    @PostMapping("/{id}/recusar")
    public DoacaoResponse recusar(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id
    ) {
        AuthenticatedUser usuario = jwtService.autenticar(authorization);
        usuario.requireTipo(TipoUsuario.ONG);
        return doacaoService.recusarDoacao(id, usuario.id());
    }

    @PostMapping("/{id}/entregar")
    public DoacaoResponse entregar(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id
    ) {
        AuthenticatedUser usuario = jwtService.autenticar(authorization);
        usuario.requireTipo(TipoUsuario.ONG);
        return doacaoService.entregarDoacao(id, usuario.id());
    }
}
