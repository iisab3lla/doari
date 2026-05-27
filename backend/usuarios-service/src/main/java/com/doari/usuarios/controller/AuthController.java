package com.doari.usuarios.controller;

import com.doari.usuarios.dto.AuthResponse;
import com.doari.usuarios.dto.LoginRequest;
import com.doari.usuarios.dto.TokenValidationRequest;
import com.doari.usuarios.dto.TokenValidationResponse;
import com.doari.usuarios.service.JwtService;
import com.doari.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UsuarioService usuarioService;
    private final JwtService jwtService;

    public AuthController(UsuarioService usuarioService, JwtService jwtService) {
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return usuarioService.login(request.email(), request.senha());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout() {
        // API stateless: o frontend descarta a sessao local.
    }

    @PostMapping("/validar-token")
    public TokenValidationResponse validarToken(@Valid @RequestBody TokenValidationRequest request) {
        return jwtService.validar(request.token());
    }
}
