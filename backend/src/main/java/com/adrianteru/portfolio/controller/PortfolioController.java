package com.adrianteru.portfolio.controller;

import com.adrianteru.portfolio.dto.MensajeContactoRequest;
import com.adrianteru.portfolio.entity.Competencia;
import com.adrianteru.portfolio.entity.Estudio;
import com.adrianteru.portfolio.entity.Experiencia;
import com.adrianteru.portfolio.entity.MensajeContacto;
import com.adrianteru.portfolio.service.CompetenciaService;
import com.adrianteru.portfolio.service.MensajeContactoService;
import com.adrianteru.portfolio.service.TrayectoriaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CONTROLLER: datos públicos del portfolio + contacto.
 *
 * <p>Agrupamos aquí los endpoints "de lectura principal" que consume la home
 * del frontend. Cada recurso podría tener su propio controller (como
 * ProyectoController); unimos los pequeños para no tener 6 ficheros con
 * 3 líneas cada uno. En un proyecto mayor sí se separarían.</p>
 *
 * <pre>
 *   GET  /api/estudios        → timeline académica
 *   GET  /api/experiencias    → timeline laboral
 *   GET  /api/competencias    → skills agrupados
 *   POST /api/contacto        → enviar mensaje del formulario (público)
 * </pre>
 */
@RestController
@RequestMapping("/api")
public class PortfolioController {

    private final TrayectoriaService trayectoriaService;
    private final CompetenciaService competenciaService;
    private final MensajeContactoService mensajeContactoService;

    public PortfolioController(TrayectoriaService trayectoriaService,
                               CompetenciaService competenciaService,
                               MensajeContactoService mensajeContactoService) {
        this.trayectoriaService = trayectoriaService;
        this.competenciaService = competenciaService;
        this.mensajeContactoService = mensajeContactoService;
    }

    // ---------------------------------------------------------------------
    // Lecturas públicas
    // ---------------------------------------------------------------------

    @GetMapping("/estudios")
    public ResponseEntity<List<Estudio>> listarEstudios() {
        return ResponseEntity.ok(trayectoriaService.listarEstudios());
    }

    @GetMapping("/experiencias")
    public ResponseEntity<List<Experiencia>> listarExperiencias() {
        return ResponseEntity.ok(trayectoriaService.listarExperiencias());
    }

    @GetMapping("/competencias")
    public ResponseEntity<List<Competencia>> listarCompetencias() {
        return ResponseEntity.ok(competenciaService.listarTodas());
    }

    // ---------------------------------------------------------------------
    // Escritura pública: formulario de contacto
    // ---------------------------------------------------------------------

    /**
     * POST /api/contacto → recibe un mensaje del formulario.
     *
     * <p>201 Created: se creó un recurso (el mensaje) en la BBDD.
     * Devolvemos el body con el id asignado para que el frontend pueda
     * referenciarlo si hiciera falta.</p>
     */
    @PostMapping("/contacto")
    public ResponseEntity<MensajeContacto> enviarMensaje(
            @Valid @RequestBody MensajeContactoRequest dto) {
        MensajeContacto guardado = mensajeContactoService.guardar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }
}
