package com.doari.usuarios.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenValidationRequest(
        @NotBlank String token
) {
}
