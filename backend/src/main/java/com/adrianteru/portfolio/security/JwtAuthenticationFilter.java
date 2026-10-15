package com.adrianteru.portfolio.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * FILTRO JWT: se ejecuta en CADA petición HTTP ANTES de llegar al controller.
 *
 * <p><b>¿Qué es un filtro en Spring Security?</b> Una cadena de filtros
 * (SecurityFilterChain) intercepta el request. La URL pasa por ellos como
 * un "embudo": este filtro mira el header Authorization y, si hay un token
 * válido, "loguea" al usuario para esa petición.</p>
 *
 * <pre>
 *  Petición: GET /api/proyectos
 *  Header:   Authorization: Bearer eyJhbG...
 *       ↓
 *  [Otros filtros: CORS, sesión...]
 *       ↓
 *  [JwtAuthenticationFilter] ← ESTE
 *      1. ¿Existe header Authorization con "Bearer "?  → si no, sigue sin loguear
 *      2. extraerUsername(token) valida FIRMA + expiración
 *      3. loadUserByUsername() para tener el UserDetails completo
 *      4. SecurityContextHolder.getContext().setAuthentication(...)  ← "estás logueado"
 *       ↓
 *  [Filtro de autorización] decide: ¿la ruta /api/proyectos POST requiere rol?
 *       ↓
 *  Controller
 * </pre>
 *
 * <p>{@code OncePerRequestFilter} garantiza que se ejecute UNA sola vez por
 * request (aunque otros filtros lo encadenen varias veces).</p>
 *
 * <p><b>Importante:</b> si el token es inválido/caducado, NO lanzamos 401 aquí:
 * simplemente NO logueamos. La decisión 401/403 la toma la configuración de
 * autorización después (así los endpoints públicos siguen funcionando sin token).</p>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioAdminDetailsService usuarioAdminDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   UsuarioAdminDetailsService usuarioAdminDetailsService) {
        this.jwtService = jwtService;
        this.usuarioAdminDetailsService = usuarioAdminDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // ---------------------------------------------------------------
        // PASO 1: ¿viene un header Authorization con prefijo "Bearer "?
        // ---------------------------------------------------------------
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Sin token → pasamos la petición tal cual (podría ser un GET público)
            filterChain.doFilter(request, response);
            return;
        }

        // Quitamos "Bearer " (7+1 caracteres) y nos quedamos con el token
        final String token = authHeader.substring(7);

        try {
            // ---------------------------------------------------------------
            // PASO 2: validar firma + expiración y extraer el username
            // ---------------------------------------------------------------
            final String username = jwtService.extraerUsername(token);

            // ---------------------------------------------------------------
            // PASO 3: ¿ya está logueado en ESTA petición? (defensa en profundidad:
            //         si un filtro previo ya autenticó, no lo pisamos)
            // ---------------------------------------------------------------
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Cargamos el UserDetails completo (habilitado, authorities...)
                UserDetails userDetails =
                        usuarioAdminDetailsService.loadUserByUsername(username);

                // ---------------------------------------------------------------
                // PASO 4: "loguear" al usuario en el contexto de SEGURIDAD
                // ---------------------------------------------------------------
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,                          // credenciales: NO se guardan
                                userDetails.getAuthorities()); // ROLE_ADMIN

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                // Contexto THREAD-LOCAL: durante el resto del request,
                // SecurityContextHolder.getContext().getAuthentication()
                // devolverá este usuario. Los controllers pueden inyectar
                // @AuthenticationPrincipal para saber quién es.
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            // Token inválido/caducado: no autenticamos, pero NO cortamos la
            // petición. Si la ruta exige auth, el filtro de autorización
            // devolverá 401/403 con un cuerpo JSON limpio.
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
