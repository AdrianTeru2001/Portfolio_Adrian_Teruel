package com.adrianteru.portfolio.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO de ENTRADA para el login (POST /api/auth/login).
 *
 * <p>Recibe solo username y password. Spring Security se encarga de la
 * comparación con BCrypt (nosotros NUNCA tocamos el password aquí).</p>
 */
public record LoginRequest(
        @NotBlank(message = "El usuario es obligatorio")
        String username,

        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {
}
