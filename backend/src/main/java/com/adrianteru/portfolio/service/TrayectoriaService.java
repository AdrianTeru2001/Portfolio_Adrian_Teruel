package com.adrianteru.portfolio.service;

import com.adrianteru.portfolio.entity.Estudio;
import com.adrianteru.portfolio.entity.Experiencia;
import com.adrianteru.portfolio.repository.EstudioRepository;
import com.adrianteru.portfolio.repository.ExperienciaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * SERVICIO: trayectoria (estudios + experiencias agrupados).
 *
 * <p>Unimos ambos repositorios en un solo servicio porque el frontend consume
 * la sección "Trayectoria" como una única timeline mezclando las dos listas.
 * Esto es una decisión de API: en vez de que el frontend haga 2 peticiones y
 * mezcle, el backend lo devuelve ya combinado (menos round-trips).</p>
 */
@Service
public class TrayectoriaService {

    private final EstudioRepository estudioRepository;
    private final ExperienciaRepository experienciaRepository;

    public TrayectoriaService(EstudioRepository estudioRepository,
                              ExperienciaRepository experienciaRepository) {
        this.estudioRepository = estudioRepository;
        this.experienciaRepository = experienciaRepository;
    }

    @Transactional(readOnly = true)
    public List<Estudio> listarEstudios() {
        return estudioRepository.findAllByOrderByFechaInicioDesc();
    }

    @Transactional(readOnly = true)
    public List<Experiencia> listarExperiencias() {
        return experienciaRepository.findAllByOrderByFechaInicioDesc();
    }
}
