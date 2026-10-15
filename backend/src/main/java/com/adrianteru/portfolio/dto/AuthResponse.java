package com.adrianteru.portfolio.dto;

/**
 * DTO de SALIDA del login: lo que recibe Angular tras autenticarse.
 *
 * <pre>
 * {
 *   "token": "eyJhbGciOiJIUzI1NiJ9...",
 *   "type": "Bearer",
 *   "expiresIn": 28800        ← segundos hasta caducar (para refrescar a tiempo)
 * }
 * </pre>
 *
 * <p>El frontend guarda el token y lo añade en cada request:</p>
 * <pre>Authorization: Bearer eyJhbG...</pre>
 */
public record AuthResponse(
        String token,
        String type,
        long expiresIn
) {
    /** Fábrica conveniente para no repetir "Bearer" y el cálculo. */
    public static AuthResponse bearer(String token, long expirationMs) {
        return new AuthResponse(token, "Bearer", expirationMs / 1000);
    }
}
