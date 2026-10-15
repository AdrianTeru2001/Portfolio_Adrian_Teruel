package com.adrianteru.portfolio.controller;

import com.adrianteru.portfolio.security.JwtAuthenticationFilter;
import com.adrianteru.portfolio.security.JwtService;
import com.adrianteru.portfolio.security.SecurityConfig;
import com.adrianteru.portfolio.security.UsuarioAdminDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * TESTS WEB del AuthController: el login (Fase 6).
 *
 * <p>El {@code AuthenticationManager} se mockea: así probamos el CAMINO HTTP
 * (validación, códigos, JSON de respuesta) sin tocar la base de datos ni
 * BCrypt. La parte de "comparar contraseñas" es responsabilidad de Spring
 * Security y del {@code UsuarioAdminDetailsService}, testado aparte.</p>
 */
@WebMvcTest(controllers = AuthController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtService.class,
        UsuarioAdminDetailsService.class
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private UsuarioAdminDetailsService usuarioAdminDetailsService;

    @Test
    @DisplayName("Login con credenciales válidas devuelve 200 + token Bearer")
    void loginCorrecto_devuelveToken() throws Exception {
        // El manager "da por bueno" el login y devuelve el usuario autenticado
        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken("admin", "pass"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"AdminPortfolio2026!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("Bearer"))
                // El token debe ser un JWT de verdad (3 partes), no un texto
                .andExpect(jsonPath("$.token").value(org.hamcrest.Matchers.matchesPattern("^[^.]+\\.[^.]+\\.[^.]+$")))
                // expiresIn en SEGUNDOS (28800000 ms / 1000 = 28800 = 8 horas)
                .andExpect(jsonPath("$.expiresIn").value(28800));
    }

    @Test
    @DisplayName("Login con credenciales incorrectas devuelve 401 sin filtrar detalles")
    void loginIncorrecto_devuelve401() throws Exception {
        // Así falla Spring Security cuando BCrypt no coincide
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("mala"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"equivocada\"}"))
                .andExpect(status().isUnauthorized())
                // Mensaje GENÉRICO a propósito: no revela si el usuario existe
                // (evita la enumeración de usuarios, un ataque clásico)
                .andExpect(jsonPath("$.message").value("Credenciales incorrectas"));
    }

    @Test
    @DisplayName("Login con campos vacíos devuelve 400 antes de tocar el manager")
    void loginVacio_devuelve400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.username").value("El usuario es obligatorio"))
                .andExpect(jsonPath("$.fields.password").value("La contraseña es obligatoria"));
    }
}
