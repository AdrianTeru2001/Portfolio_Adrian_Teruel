package com.adrianteru.portfolio.controller;

import com.adrianteru.portfolio.dto.AuthResponse;
import com.adrianteru.portfolio.dto.LoginRequest;
import com.adrianteru.portfolio.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * CONTROLLER: autenticación (la única puerta de entrada para obtener un JWT).
 *
 * <pre>
 *   POST /api/auth/login   → {username, password} ⇒ {token, type, expiresIn}
 * </pre>
 *
 * <p><b>¿Por qué este endpoint es "permitAll" si es el de login?</b>
 * Porque sin él no podrías AUTENTICARTE. La protección real no está aquí,
 * sino en que sin token válido no pasas a los endpoints POST/PUT/DELETE.</p>
 *
 * <p><b>Nota de seguridad:</b> si las credenciales son incorrectas,
 * AuthenticationManager lanza BadCredentialsException → nuestro
 * GlobalExceptionHandler la traduce a 401 (no se revela SI el usuario existe,
 * solo "no autorizado" — evita enumeración de usuarios).</p>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /** Duración del token leída de application.yml (misma que usa JwtService). */
    private final long expirationMs;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          @org.springframework.beans.factory.annotation.Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.expirationMs = expirationMs;
    }

    /**
     * LOGIN.
     *
     * <p>Flujo paso a paso:</p>
     * <ol>
     *   <li>{@code @Valid} valida que username/password no vengan vacíos</li>
     *   <li>Se crea un "token de autenticación" (aún SIN validar) con los
     *       datos crudos</li>
     *   <li>{@code authenticationManager.authenticate()}:
     *       <ul>
     *         <li>llama a UsuarioAdminDetailsService → busca el usuario</li>
     *         <li>compara password con el hash BCrypt</li>
     *         <li>si falla → BadCredentialsException (→ 401)</li>
     *       </ul>
     *   </li>
     *   <li>Si todo OK → generamos el JWT con el username y lo devolvemos</li>
     * </ol>
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        // Paso 1: autenticar (verifica credenciales contra BCrypt en BBDD)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(), request.password()));

        // Paso 2: si llegamos aquí, las credenciales son correctas → firmar JWT
        String token = jwtService.generarToken(authentication.getName());
        //                                    ^ getName() = username autenticado

        return ResponseEntity.ok(AuthResponse.bearer(token, expirationMs));
    }
}
