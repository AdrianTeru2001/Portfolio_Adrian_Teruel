package com.adrianteru.portfolio.repository;

import com.adrianteru.portfolio.entity.Estudio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EstudioRepository extends JpaRepository<Estudio, Long> {

    /**
     * QUERY DERIVADA POR NOMBRE: Spring Data "lee" el nombre del método y
     * deduce la consulta. Reglas del nombre:
     *   Find + [condición campo] + [comparador] + [_Order por]
     *
     *   "OrderByFechaInicioDesc" → ORDER BY fecha_inicio DESC (más reciente arriba)
     *
     * Si el nombre no se puede interpretar, FALLA AL ARRANCAR la app
     * (mejor que fallar en runtime la primera vez que se llama).
     */
    List<Estudio> findAllByOrderByFechaInicioDesc();

    /**
     * QUERY JPQL con @Query: es como SQL pero usando nombres de ENTIDAD
     * (Estudio, no estudios) y de propiedad (fechaInicio, no fecha_inicio).
     * Ventaja: es portable (funcionaría en MySQL sin tocar nada).
     *
     * El prefijo "JPQL" del comentario es solo documentación.
     */
    @Query("SELECT e FROM Estudio e WHERE e.enCurso = true ORDER BY e.fechaInicio DESC")
    List<Estudio> findEnCurso();
}
