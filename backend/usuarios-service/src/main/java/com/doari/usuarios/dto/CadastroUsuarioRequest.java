package com.doari.usuarios.dto;

import com.doari.usuarios.model.TipoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CadastroUsuarioRequest(
        @NotBlank String nome,
        @Email @NotBlank String email,
        @NotBlank @Size(min = 6) String senha,
        String telefone,
        String documento,
        @NotNull TipoUsuario tipo
) {
}
