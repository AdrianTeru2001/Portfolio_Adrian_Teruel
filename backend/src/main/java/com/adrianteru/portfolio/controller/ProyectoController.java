package com.adrianteru.portfolio.controller;

import com.adrianteru.portfolio.dto.ProyectoRequest;
import com.adrianteru.portfolio.dto.ProyectoResponse;
import com.adrianteru.portfolio.service.ProyectoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CONTROLLER: proyectos (el CRUD completo del portfolio).
 *
 * <pre>
 *   GET    /api/proyectos           → listar todos (público)
 *   GET    /api/proyectos/destacados → los destacados para la home (público)
 *   GET    /api/proyectos/{id}      → detalle (público)
 *   GET    /api/proyectos?tecnologia=spring → búsqueda (público)
 *   POST   /api/proyectos           → crear   (Fase 2: JWT)
 *   PUT    /api/proyectos/{id}      → editar  (Fase 2: JWT)
 *   DELETE /api/proyectos/{id}      → borrar  (Fase 2: JWT)
 * </pre>
 *
 * <p><b>Convenciones REST aplicadas:</b></p>
 * <ul>
 *   <li>Recurso en plural y sin verbo: {@code /proyectos}, no {@code /getProyectos}</li>
 *   <li>El verbo lo pone el método HTTP (GET/POST/PUT/DELETE)</li>
 *   <li>El id va en la URL, nunca en el body</li>
 *   <li>POST crea → 201 Created; DELETE borra → 204 sin contenido</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/proyectos")
public class ProyectoController {

    private final ProyectoService proyectoService;

    public ProyectoController(ProyectoService proyectoService) {
        this.proyectoService = proyectoService;
    }

    /**
     * GET /api/proyectos?tecnologia=angular
     *
     * <p>{@code @RequestParam} lee la query string. {@code required=false} →
     * si no viene, vale null (listamos todos en vez de fallar con 400).</p>
     */
    @GetMapping
    public ResponseEntity<List<ProyectoResponse>> listar(
            @RequestParam(required = false) String tecnologia) {

        List<ProyectoResponse> proyectos = (tecnologia == null || tecnologia.isBlank())
                ? proyectoService.listarTodos()
                : proyectoService.buscarPorTecnologia(tecnologia);

        return ResponseEntity.ok(proyectos);
    }

    /** GET /api/proyectos/destacados — ruta fija, hay que declararla ANTES de /{id}. */
    @GetMapping("/destacados")
    public ResponseEntity<List<ProyectoResponse>> destacados() {
        return ResponseEntity.ok(proyectoService.listarDestacados());
    }

    /** GET /api/proyectos/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<ProyectoResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(proyectoService.obtenerPorId(id));
    }

    /**
     * POST /api/proyectos → crea un proyecto.
     *
     * <p>{@code @RequestBody} = lee el JSON del body y lo convierte en
     * ProyectoRequest (Jackson hace la conversión automática). {@code @Valid}
     * lanza validación antes.</p>
     *
     * <p>201 Created + header "Location" apuntando al recurso nuevo:
     * es lo que dice la especificación HTTP para un POST exitoso.</p>
     */
    @PostMapping
    public ResponseEntity<ProyectoResponse> crear(@Valid @RequestBody ProyectoRequest dto) {
        ProyectoResponse creado = proyectoService.crear(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header("Location", "/api/proyectos/" + creado.id())
                .body(creado);
    }

    /** PUT /api/proyectos/{id} → actualiza completo. */
    @PutMapping("/{id}")
    public ResponseEntity<ProyectoResponse> actualizar(@PathVariable Long id,
                                                       @Valid @RequestBody ProyectoRequest dto) {
        return ResponseEntity.ok(proyectoService.actualizar(id, dto));
    }

    /**
     * DELETE /api/proyectos/{id}.
     * {@code @ResponseStatus(HttpStatus.NO_CONTENT)} → 204 sin body
     * (la operación tuvo éxito y no hay nada que devolver).
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        proyectoService.eliminar(id);
    }
}
