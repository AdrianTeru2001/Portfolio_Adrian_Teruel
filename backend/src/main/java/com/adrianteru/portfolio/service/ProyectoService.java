package com.adrianteru.portfolio.service;

import com.adrianteru.portfolio.dto.ProyectoRequest;
import com.adrianteru.portfolio.dto.ProyectoResponse;
import com.adrianteru.portfolio.entity.Proyecto;
import com.adrianteru.portfolio.exception.ResourceNotFoundException;
import com.adrianteru.portfolio.repository.ProyectoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * SERVICIO: proyectos.
 *
 * <p>Esta clase ejemplifica el patrón completo de la capa service:
 * <b>recibe DTOs de entrada, trabaja con entidades internamente y devuelve
 * DTOs de salida.</b> Los controllers jamás ven entidades.</p>
 */
@Service
public class ProyectoService {

    private final ProyectoRepository proyectoRepository;

    public ProyectoService(ProyectoRepository proyectoRepository) {
        this.proyectoRepository = proyectoRepository;
    }

    // ---------------------------------------------------------------------
    // LECTURAS (públicas)
    // ---------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<ProyectoResponse> listarTodos() {
        // stream() → API de Java 8+ para transformar colecciones:
        // recorre cada Proyecto (p) y lo convierte a ProyectoResponse.
        // .toList() cierra el stream en una inmutable List.
        return proyectoRepository.findAllByOrderByDestacadoDescFechaDesc()
                .stream()
                .map(ProyectoResponse::from)   // method reference = lambda (p -> ProyectoResponse.from(p))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProyectoResponse> listarDestacados() {
        return proyectoRepository.findByDestacadoTrueOrderByFechaDesc()
                .stream()
                .map(ProyectoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProyectoResponse obtenerPorId(Long id) {
        // findById devuelve Optional<T> → contenedor que representa
        // "puede haber un valor o no". Obliga a manejar el caso vacío
        // (evita el temido NullPointerException).
        return proyectoRepository.findById(id)
                .map(ProyectoResponse::from)                    // si existe → lo convierte
                .orElseThrow(() -> new ResourceNotFoundException(  // si no → 404 vía el handler
                        "Proyecto no encontrado con id " + id));
    }

    @Transactional(readOnly = true)
    public List<ProyectoResponse> buscarPorTecnologia(String texto) {
        return proyectoRepository.buscarPorTecnologia(texto)
                .stream()
                .map(ProyectoResponse::from)
                .toList();
    }

    // ---------------------------------------------------------------------
    // ESCRITURAS (las protegerá Spring Security con JWT en la Fase 2)
    // ---------------------------------------------------------------------

    @Transactional
    public ProyectoResponse crear(ProyectoRequest dto) {
        Proyecto proyecto = new Proyecto();
        copiarDtoAEntidad(dto, proyecto);
        Proyecto guardado = proyectoRepository.save(proyecto);
        return ProyectoResponse.from(guardado);
    }

    @Transactional
    public ProyectoResponse actualizar(Long id, ProyectoRequest dto) {
        Proyecto proyecto = proyectoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proyecto no encontrado con id " + id));
        copiarDtoAEntidad(dto, proyecto);
        return ProyectoResponse.from(proyectoRepository.save(proyecto));
    }

    @Transactional
    public void eliminar(Long id) {
        // Comprobamos que existe antes de borrar para devolver 404 en vez de
        // un 200 silencioso (eliminar algo inexistente no es "éxito").
        if (!proyectoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Proyecto no encontrado con id " + id);
        }
        proyectoRepository.deleteById(id);
    }

    /** Copia del DTO a la entidad. Extraída para reutilizarla en crear/actualizar. */
    private void copiarDtoAEntidad(ProyectoRequest dto, Proyecto proyecto) {
        proyecto.setNombre(dto.nombre());
        proyecto.setDescripcion(dto.descripcion());
        proyecto.setTecnologias(dto.tecnologias());
        proyecto.setUrlRepo(dto.urlRepo());
        proyecto.setUrlDemo(dto.urlDemo());
        proyecto.setImagenUrl(dto.imagenUrl());
        proyecto.setDestacado(dto.destacado());
        proyecto.setFecha(dto.fecha());
    }
}
