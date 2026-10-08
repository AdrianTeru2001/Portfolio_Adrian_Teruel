package com.adrianteru.portfolio.controller;

import com.adrianteru.portfolio.dto.PerfilUpdateRequest;
import com.adrianteru.portfolio.entity.Perfil;
import com.adrianteru.portfolio.service.PerfilService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * CONTROLLER: perfil personal.
 *
 * <p>Anotaciones clave:</p>
 * <ul>
 *   <li>{@code @RestController} = {@code @Controller} (recibe peticiones HTTP)
 *       + {@code @ResponseBody} (el objeto devuelto se serializa a JSON con
 *       Jackson automáticamente; sin esto, Spring buscaría una vista HTML).</li>
 *   <li>{@code @RequestMapping("/api/perfil")} → prefijo de ruta del controller.</li>
 * </ul>
 *
 * <p><b>Mapa de endpoints de este controller:</b></p>
 * <pre>
 *   GET    /api/perfil      → leer datos (público)
 *   PUT    /api/perfil      → actualizar (en Fase 2: requiere JWT)
 * </pre>
 */
@RestController
@RequestMapping("/api/perfil")
public class PerfilController {

    private final PerfilService perfilService;

    // Inyección por constructor (misma filosofía que en los services)
    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    /**
     * GET /api/perfil → devuelve el perfil.
     *
     * <p>Spring serializa la entidad Perfil a JSON con Jackson. En este caso
     * devolvemos la entidad (no un DTO) porque la entidad no tiene relaciones
     * lazy ni campos sensibles; si las tuviera, SÍ usaríamos DTO.</p>
     */
    @GetMapping
    public ResponseEntity<Perfil> obtenerPerfil() {
        return ResponseEntity.ok(perfilService.obtenerPerfil());
    }

    /**
     * PUT /api/perfil → actualiza los datos del perfil.
     *
     * <p>{@code @Valid} activa la Bean Validation del DTO: si "nombre" llega
     * vacío o el email es inválido, Spring lanza MethodArgumentNotValidException
     * y NUESTRO GlobalExceptionHandler devuelve un 400 con detalle — el
     * controller ni se llega a ejecutar.</p>
     *
     * <p>PUT vs PATCH: PUT reemplaza el recurso completo; PATCH modifica
     * parcialmente. Usamos PUT porque el formulario admin envía todo el perfil.</p>
     */
    @PutMapping
    public ResponseEntity<Perfil> actualizarPerfil(@Valid @RequestBody PerfilUpdateRequest dto) {
        return ResponseEntity.ok(perfilService.actualizarPerfil(dto));
    }
}
