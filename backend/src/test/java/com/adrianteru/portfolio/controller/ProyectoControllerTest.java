package com.adrianteru.portfolio.controller;

import com.adrianteru.portfolio.dto.ProyectoResponse;
import com.adrianteru.portfolio.exception.ResourceNotFoundException;
import com.adrianteru.portfolio.security.JwtAuthenticationFilter;
import com.adrianteru.portfolio.security.JwtService;
import com.adrianteru.portfolio.security.SecurityConfig;
import com.adrianteru.portfolio.security.UsuarioAdminDetailsService;
import com.adrianteru.portfolio.service.ProyectoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * TESTS WEB del ProyectoController + MATRIZ DE SEGURIDAD (Fase 6).
 *
 * ============================================================================
 *  ¿QUÉ ES @WebMvcTest?
 * ============================================================================
 * Un "slice" (rebanada) del contexto de Spring: levanta SOLO la capa web
 * (controllers, @ControllerAdvice, conversores, filtros de seguridad) con un
 * {@code MockMvc} en memoria. NO arranca JPA, NI Flyway, NI PostgreSQL:
 * por eso estos tests corren en segundos y sin Docker.
 *
 * <p>Los services se sustituyen por mocks con {@code @MockitoBean} (Spring
 * Boot 4 ya no usa el antiguo {@code @MockBean}).</p>
 *
 * <p><b>Lo que se valida aquí es lo que NO cubre el test unitario del
 * service:</b> la capa HTTP (códigos de estado, headers, JSON) Y la matriz de
 * autorización de SecurityConfig — que es donde un fallo dejaría el CRUD
 * abierto al mundo o inaccesible.</p>
 */
@WebMvcTest(controllers = ProyectoController.class)
@Import({
        SecurityConfig.class,            // nuestra cadena de seguridad (no la de Spring)
        JwtAuthenticationFilter.class,   // el filtro que interpreta Authorization
        JwtService.class,                // real: firma los tokens de prueba
        UsuarioAdminDetailsService.class // requerido por el filtro
})
class ProyectoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /** JWT real: genera tokens válidos para las pruebas autenticadas. */
    @Autowired
    private JwtService jwtService;

    /** El service es un doble: verificamos HTTP, no la lógica de negocio. */
    @MockitoBean
    private ProyectoService proyectoService;

    /** El servicio de usuarios también se mockea (no hay BD en este slice). */
    @MockitoBean
    private UsuarioAdminDetailsService usuarioAdminDetailsService;

    // ---------------------------------------------------------------------
    // LECTURAS PÚBLICAS (deben funcionar SIN token)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/proyectos es público: responde 200 sin ningún token")
    void getProyectos_esPublico_sinToken() throws Exception {
        when(proyectoService.listarTodos())
                .thenReturn(List.of(new ProyectoResponse(
                        1L, "Portfolio", "Sitio personal", "Angular",
                        null, null, null, true, LocalDate.of(2026, 1, 1))));

        mockMvc.perform(get("/api/proyectos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Portfolio"));
    }

    @Test
    @DisplayName("GET /api/proyectos/{id} inexistente responde 404 con JSON de error")
    void getProyectoInexistente_responde404() throws Exception {
        when(proyectoService.obtenerPorId(99L))
                .thenThrow(new ResourceNotFoundException("Proyecto no encontrado con id 99"));

        // Sin el GlobalExceptionHandler esto sería un 500 con stacktrace:
        // el test protege la "fachada de errores" de la API.
        mockMvc.perform(get("/api/proyectos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value("/api/proyectos/99"));
    }

    // ---------------------------------------------------------------------
    // ESCRITURAS PROTEGIDAS (la razón de existir del JWT)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/proyectos SIN token responde 401 (no está abierto)")
    void postSinToken_responde401() throws Exception {
        // El CRUD nunca debe poder crearse desde fuera sin autenticarse:
        // si este test falla, el panel está expuesto públicamente.
        mockMvc.perform(post("/api/proyectos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Nuevo\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Falta o es inválido el token JWT"));

        // Ni siquiera debe llegar al service
        verify(proyectoService, org.mockito.Mockito.never()).crear(any());
    }

    @Test
    @DisplayName("POST /api/proyectos CON token válido responde 201 + header Location")
    void postConToken_responde201() throws Exception {
        when(proyectoService.crear(any())).thenReturn(new ProyectoResponse(
                7L, "Nuevo", null, null, null, null, null, false, null));

        mockMvc.perform(post("/api/proyectos")
                        .header("Authorization", "Bearer " + tokenAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Nuevo\",\"destacado\":true}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/proyectos/7"))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.nombre").value("Nuevo"));
    }

    @Test
    @DisplayName("POST con nombre vacío responde 400 e indica el campo que falla")
    void postInvalido_responde400ConDetalleDeCampo() throws Exception {
        // Bean Validation (@NotBlank) dispara ANTES de llegar al service.
        // El "fields" permite al frontend pintar el error bajo el input.
        mockMvc.perform(post("/api/proyectos")
                        .header("Authorization", "Bearer " + tokenAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\",\"destacado\":false}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fields.nombre").value("El nombre del proyecto es obligatorio"));

        // La validación NUNCA debe llegar al service
        verify(proyectoService, org.mockito.Mockito.never()).crear(any());
    }

    @Test
    @DisplayName("Un JSON ilegible responde 400 (no 500) y no llega al service")
    void jsonMalformado_responde400() throws Exception {
        // Regresión del bug encontrado en la Fase 6: un body que Jackson no
        // puede PARSEAR debe ser un error del CLIENTE (400), nunca un 500
        // que parece fallo del servidor. Lo cubre el handler de
        // HttpMessageNotReadableException del GlobalExceptionHandler.
        mockMvc.perform(post("/api/proyectos")
                        .header("Authorization", "Bearer " + tokenAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":"))   // JSON truncado a mitad
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("JSON inválido o incompleto"));

        verify(proyectoService, org.mockito.Mockito.never()).crear(any());
    }

    @Test
    @DisplayName("Un Content-Type que no es JSON responde 415")
    void contentTypeNoJson_responde415() throws Exception {
        mockMvc.perform(post("/api/proyectos")
                        .header("Authorization", "Bearer " + tokenAdmin())
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("nombre=X"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.message")
                        .value("Tipo de contenido no soportado: usa application/json"));
    }

    @Test
    @DisplayName("DELETE /api/proyectos/{id} con token responde 204 sin cuerpo")
    void deleteConToken_responde204() throws Exception {
        mockMvc.perform(delete("/api/proyectos/5")
                        .header("Authorization", "Bearer " + tokenAdmin()))
                .andExpect(status().isNoContent());

        verify(proyectoService).eliminar(5L);
    }

    // ---------------------------------------------------------------------
    // DENY BY DEFAULT ("lo que no se concede, se deniega")
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Cualquier ruta no listada responde 401 sin token")
    void rutaNoListada_responde401() throws Exception {
        // Esta es la regla de oro de la seguridad: NO basta con proteger los
        // endpoints que conoces; hay que asegurar que TODO lo demás queda
        // fuera. Si alguien añade mañana un endpoint y se olvida de listarlo,
        // este test garantiza que seguirá bloqueado.
        mockMvc.perform(get("/api/esto-no-existe"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Una escritura sobre un endpoint público de lectura también exige token")
    void escrituraNoPermitida_responde401() throws Exception {
        // GET /api/proyectos está permitAll, pero eso NO concede POST:
        // cada método HTTP se evalúa por separado.
        mockMvc.perform(post("/api/perfil")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    /**
     * Firma un token real para "admin" y prepara el doble del servicio de
     * usuarios para que el filtro JWT encuentre a ese usuario.
     *
     * <p>Se llama DENTRO de cada test (no en @BeforeEach) a propósito: con la
     * estrategia de mocks estricta de Mockito, un stub sin usar en otros tests
     * haría fallar la ejecución.</p>
     */
    private String tokenAdmin() {
        when(usuarioAdminDetailsService.loadUserByUsername("admin"))
                .thenReturn(User.withUsername("admin")
                        .password("hash-de-prueba")
                        .roles("ADMIN")
                        .build());
        return jwtService.generarToken("admin");
    }
}
