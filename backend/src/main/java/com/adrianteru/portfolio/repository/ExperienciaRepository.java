package com.adrianteru.portfolio.repository;

import com.adrianteru.portfolio.entity.Experiencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExperienciaRepository extends JpaRepository<Experiencia, Long> {

    /** Experiencias ordenadas: primero las que siguen en curso, después por fecha. */
    List<Experiencia> findAllByOrderByFechaInicioDesc();
}
