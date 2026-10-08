package com.adrianteru.portfolio.service;

import com.adrianteru.portfolio.entity.Competencia;
import com.adrianteru.portfolio.repository.CompetenciaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * SERVICIO: competencias (skills).
 *
 * <p>En esta fase devolvemos la entidad directamente en lecturas por simplicidad
 * — al ser una entidad "plana" sin relaciones lazy, no hay riesgo de
 * LazyInitializationException y el JSON resultante es idéntico al de un DTO.
 * En la Fase 4 (admin) añadiremos el DTO de entrada con validación.</p>
 */
@Service
public class CompetenciaService {

    private final CompetenciaRepository competenciaRepository;

    public CompetenciaService(CompetenciaRepository competenciaRepository) {
        this.competenciaRepository = competenciaRepository;
    }

    @Transactional(readOnly = true)
    public List<Competencia> listarTodas() {
        return competenciaRepository.findAllByOrderByCategoriaAscNivelDesc();
    }
}
