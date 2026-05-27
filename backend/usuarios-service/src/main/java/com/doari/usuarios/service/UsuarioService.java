package com.doari.usuarios.service;

import com.doari.usuarios.dto.AuthResponse;
import com.doari.usuarios.dto.CadastroUsuarioRequest;
import com.doari.usuarios.dto.UsuarioResponse;
import com.doari.usuarios.model.Usuario;
import com.doari.usuarios.repository.UsuarioRepository;
import java.util.NoSuchElementException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UsuarioService(UsuarioRepository usuarioRepository, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public UsuarioResponse cadastrar(CadastroUsuarioRequest request) {
        String email = request.email().trim().toLowerCase();
        String senha = request.senha().trim();

        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(request.nome().trim());
        usuario.setEmail(email);
        usuario.setSenhaHash(passwordEncoder.encode(senha));
        usuario.setTelefone(request.telefone());
        usuario.setDocumento(request.documento());
        usuario.setTipo(request.tipo());

        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(String email, String senha) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new IllegalArgumentException("E-mail ou senha inválidos."));

        if (!passwordEncoder.matches(senha.trim(), usuario.getSenhaHash())) {
            throw new IllegalArgumentException("E-mail ou senha inválidos.");
        }

        String token = jwtService.gerarToken(usuario);
        return new AuthResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getTipo(),
                "Bearer",
                token,
                jwtService.getExpirationSeconds()
        );
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPerfil(Long id) {
        return usuarioRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new NoSuchElementException("Usuário não encontrado."));
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getTelefone(),
                usuario.getDocumento(),
                usuario.getTipo(),
                usuario.getCriadoEm()
        );
    }
}
