package com.doari.doacoes.controller;

import com.doari.doacoes.dto.CompatibilidadeResponse;
import com.doari.doacoes.security.JwtService;
import com.doari.doacoes.service.DoacaoService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/compatibilidades")
public class CompatibilidadeController {

    private final DoacaoService doacaoService;
    private final JwtService jwtService;

    public CompatibilidadeController(DoacaoService doacaoService, JwtService jwtService) {
        this.doacaoService = doacaoService;
        this.jwtService = jwtService;
    }

    @GetMapping
    public List<CompatibilidadeResponse> listar(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        jwtService.autenticar(authorization);
        return doacaoService.listarCompatibilidades();
    }
}
