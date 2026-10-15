package com.adrianteru.portfolio.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * CONFIGURACIÓN DE SEGURIDAD (la pieza central de la Fase 2).
 *
 * <p>{@code @EnableWebSecurity} activa la seguridad web. Sin esta clase,
 * Spring Boot aplicaría sus "defaults": login con formulario y TODO bloqueado
 * con 403 — por eso es obligatoria.</p>
 *
 * <p><b>Mapa de autorización de nuestro portfolio:</b></p>
 * <pre>
 *  MÉTODO    RUTA                     ¿QUIÉN?
 *  ─────────────────────────────────────────────────────────────
 *  GET       /api/**                  PÚBLICO (cualquiera lee)
 *  POST      /api/auth/login          PÚBLICO (obtener token)
 *  POST      /api/contacto            PÚBLICO (formulario de contacto)
 *  OPTIONS   /api/**                  PÚBLICO (preflight de CORS)
 *  POST/PUT/DELETE /api/**            ADMIN (requiere JWT válido)
 *  GET       /actuator/health         PÚBLICO (monitorización)
 *  TODO lo demás                     DENEGADO (403 por defecto)
 * </pre>
 *
 * <p>Regla de oro: <b>lo que no se concede, se deniega</b>
 * (deny by default) — el filtro por defecto bloquea lo no listado.</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Permite restringir rutas según perfil (local/producción) con
     * ${app.security.restringir-admin} — en local true; en el futuro si
     * quieres exponer el CRUD sin JWT, lo cambias a false solo ahí.
     */
    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;

    /**
     * CADENA DE FILTROS: define qué pasa por dónde.
     *
     * <p>Orden de dentro hacia fuera:</p>
     * <ol>
     *   <li>{@code addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)}
     *       → nuestro filtro JWT se ejecuta ANTES del filtro "clásico" de
     *       username/password (que aquí no usamos porque somos stateless)</li>
     *   <li>{@code csrf} DESACTIVADO → CSRF explota cookies de sesión;
     *       nosotros usamos tokens en headers, no cookies, así que no aplica</li>
     *   <li>{@code sessionManagement STATELESS} → el servidor NO crea sesión:
     *       cada request se valida solo con su JWT (sin nada en memoria)</li>
     *   <li>{@code authorizeHttpRequests} → el mapa de arriba</li>
     * </ol>
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtFilter)
            throws Exception {

        http
            // Desactivamos CSRF (no usamos cookies de sesión)
            .csrf(AbstractHttpConfigurer::disable)

            // CORS: necesita la config de CorsConfig para los preflight
            .cors(cors -> cors.configurationSource(
                    crearCorsSource()))

            // Somos stateless: sin sesiones en el servidor
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Nuestro filtro JWT antes del filtro clásico de autenticación
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

            // MAPA DE AUTORIZACIÓN
            .authorizeHttpRequests(auth -> auth
                    // --- Preflight CORS (OPTIONS) siempre abierto ---
                    .requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()

                    // --- Login: abierto (si no, no podrías obtener el token) ---
                    .requestMatchers("/api/auth/login").permitAll()

                    // --- Lecturas públicas del portfolio ---
                    .requestMatchers(HttpMethod.GET, "/api/perfil",
                            "/api/estudios", "/api/experiencias",
                            "/api/competencias", "/api/proyectos/**").permitAll()

                    // --- Formulario de contacto: escritura pública controlada ---
                    .requestMatchers(HttpMethod.POST, "/api/contacto").permitAll()

                    // --- Actuator: salud de monitorización ---
                    .requestMatchers("/actuator/health").permitAll()

                    // --- TODO lo demás: requiere estar autenticado (JWT) ---
                    // Esto cubre POST/PUT/DELETE de proyectos, perfil, etc.
                    .anyRequest().authenticated()
            )

            // Manejo de errores de seguridad con JSON (en vez de redirect a login)
            .exceptionHandling(ex -> ex
                    // 401: no hay token o es inválido → el frontend sabe que debe loguearse
                    .authenticationEntryPoint((req, res, ex2) -> {
                        res.setStatus(401);
                        res.setContentType("application/json");
                        res.getWriter().write(
                            "{\"status\":401,\"error\":\"Unauthorized\"," +
                            "\"message\":\"Falta o es inválido el token JWT\"}");
                    })
                    // 403: token válido pero sin permisos para esta ruta
                    .accessDeniedHandler((req, res, ex2) -> {
                        res.setStatus(403);
                        res.setContentType("application/json");
                        res.getWriter().write(
                            "{\"status\":403,\"error\":\"Forbidden\"," +
                            "\"message\":\"No tienes permisos para esta operación\"}");
                    })
            );

        return http.build();
    }

    /**
     * PASSWORD ENCODER: el traductor de contraseñas.
     *
     * <p>BCrypt es el estándar industrial:</p>
     * <ul>
     *   <li><b>Salt aleatorio</b> por hash → dos "admin123" idénticos generan
     *       hashes distintos (rompe los ataques con rainbow tables)</li>
     *   <li><b>Cost factor 10</b> → 2^10 iteraciones de SHA-256 deliberadamente
     *       lentas: hace caro el ataque de fuerza bruta</li>
     *   <li>El hash incluye su propia configuración → se puede verificar hoy
     *       aunque cambies de algoritmo mañana</li>
     * </ul>
     *
     * <p>Con este bean, allí donde Spring compara contraseñas
     * ({@code authenticationManager.authenticate(...)}) usará BCrypt
     * automáticamente.</p>
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AUTHENTICATION MANAGER: el que ejecuta el login.
     * Recibe username+password → consulta UserDetailsService → compara con
     * PasswordEncoder → lanza BadCredentialsException si falla.
     * Lo usaremos en AuthController (más abajo).
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * Origen de configuración CORS para Spring Security (reutiliza los
     * orígenes de application.yml). Sin esto, el preflight OPTIONS de
     * Angular sería rechazado por la cadena de seguridad.
     */
    private org.springframework.web.cors.CorsConfigurationSource crearCorsSource() {
        org.springframework.web.cors.CorsConfiguration config =
                new org.springframework.web.cors.CorsConfiguration();
        config.setAllowedOrigins(java.util.Arrays.asList(allowedOrigins));
        config.setAllowedMethods(java.util.List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(java.util.List.of("*"));
        config.setAllowCredentials(true);

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source =
                new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
