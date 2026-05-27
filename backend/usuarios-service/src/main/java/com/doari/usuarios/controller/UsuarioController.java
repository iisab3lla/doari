package com.doari.usuarios.controller;

import com.doari.usuarios.dto.CadastroUsuarioRequest;
import com.doari.usuarios.dto.UsuarioResponse;
import com.doari.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse cadastrar(@Valid @RequestBody CadastroUsuarioRequest request) {
        return usuarioService.cadastrar(request);
    }

    @GetMapping("/{id}/perfil")
    public UsuarioResponse perfil(@PathVariable Long id) {
        return usuarioService.buscarPerfil(id);
    }
}
