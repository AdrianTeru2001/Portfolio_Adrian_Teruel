package com.adrianteru.portfolio.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * SERVICIO JWT: genera y valida JSON Web Tokens.
 *
 * ============================================================================
 *  ¿QUÉ ES UN JWT?
 * ============================================================================
 *  JSON Web Token = un "recibo" firmado digitalmente que viaja en cada petición
 *  autenticada. Estructura (3 partes separadas por puntos):
 *
 *    eyJhbGciOiJIUzI1NiJ9 . eyJzdWIiOiJhZG1pbiIsImV4cCI6MT... . SflKxwRJ...
 *    ───── cabecera ─────   ───────── payload (datos) ────────   ─ firma ──
 *
 *  - Cabecera: algoritmo de firma (HS256)
 *  - Payload:  claims = datos útiles (usuario, expiración, emitido...)
 *  - Firma:    hecha con NUESTRO SECRETO → cualquiera puede LEER el payload,
 *              pero NADIE puede FALSIFICARLO sin el secreto.
 *
 *  ¿Por qué JWT y no sesiones con cookies?
 *  - Stateless: el servidor no guarda estado; solo valida la firma.
 *  - Funciona bien con SPA (Angular) que manda el token en un header.
 *  - Ideal cuando solo hay 1 backend y 1 frontend (nuestro caso).
 *  Contras que asumimos: el token no se puede revocar antes de expirar
 *  (por eso usamos expiraciones cortas, ver application.yml).
 *
 *  FLUJO COMPLETO:
 *  1. POST /api/auth/login con user+pass → se verifica con BCrypt
 *  2. Si OK → aquí se FIRMA un token con expiración → se devuelve al cliente
 *  3. El cliente lo guarda (localStorage del navegador)
 *  4. En cada request, Angular añade:  Authorization: Bearer <token>
 *  5. El JwtAuthenticationFilter (abajo) lee el header, VALIDA la firma y
 *     si es correcta "loguea" al usuario para esa petición
 * ============================================================================
 */
@Service
public class JwtService {

    /**
     * Secreto para firmar (HS256 necesita >= 256 bits = 32 bytes).
     * Viene de application.yml ← variable de entorno JWT_SECRET.
     * NUNCA hardcodearlo: si se filtra el repo, cualquiera firma tokens.
     */
    private final String secret;

    /** Duración del token en milisegundos (ej: 8h → 28800000). */
    private final long expirationMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-ms}") long expirationMs) {
        if (secret == null || secret.length() < 32) {
            // Fallar AL ARRANCAR es mejor que fallar en runtime con tokens inválidos.
            throw new IllegalArgumentException(
                    "app.jwt.secret debe tener al menos 32 caracteres (256 bits para HS256)");
        }
        this.secret = secret;
        this.expirationMs = expirationMs;
    }

    /**
     * GENERA un token firmado para un usuario.
     *
     * <p>Claims personalizados que incluimos:</p>
     * <ul>
     *   <li>{@code sub} (subject) → quién es el usuario ("admin") — es un claim ESTÁNDAR</li>
     *   <li>{@code iat} (issued at) → cuándo se emitió — estándar</li>
     *   <li>{@code exp} (expiration) → cuándo caduca — estándar; sin él el token
     *       valdría para siempre (muy peligroso si se filtra)</li>
     * </ul>
     */
    public String generarToken(String username) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expirationMs);

        return Jwts.builder()
                .subject(username)          // "sub": quién eres
                .issuedAt(ahora)            // "iat": cuándo se emitió
                .expiration(expiracion)     // "exp": cuándo caduca
                .signWith(claveSecreta())   // firma HMAC-SHA256 con nuestro secreto
                .compact();                 // serializa a "header.payload.firma"
    }

    /**
     * EXTRAE el username del token.
     *
     * <p>{@code parseSignedClaims()} verifica la FIRMA automáticamente:
     * si alguien altera el payload (p. ej. cambia "sub" a "admin" con un token
     * suyo), la firma deja de cuadrar y lanza {@code JwtException} → 401.</p>
     *
     * <p>También valida internamente que no esté caducado según "exp".</p>
     */
    public String extraerUsername(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(claveSecreta())   // confirma la firma con el secreto
                .build()
                .parseSignedClaims(token)     // lanza excepción si firma o exp fallan
                .getPayload();                // devuelve los claims ya verificados

        return claims.getSubject();
    }

    /**
     * Validez completa del token: firma correcta + no caducado.
     * Devuelve false en vez de lanzar excepción → cómodo para el filtro.
     */
    public boolean esValido(String token) {
        try {
            extraerUsername(token);   // si no lanza, es válido
            return true;
        } catch (Exception e) {
            // Firma inválida, token malformado, caducado...
            return false;
        }
    }

    /**
     * Construye la clave HMAC a partir del secreto.
     * HS256 = HMAC con SHA-256 → la clave es simétrica (misma para firmar
     * y verificar), por eso el secreto debe ser EXCLUSIVO y largo.
     */
    private SecretKey claveSecreta() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
