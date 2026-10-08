package com.adrianteru.portfolio.repository;

import com.adrianteru.portfolio.entity.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {

    /** Destacados para la home (limitados a N en el service). */
    List<Proyecto> findByDestacadoTrueOrderByFechaDesc();

    /** Todos ordenados: destacados primero y más recientes arriba. */
    List<Proyecto> findAllByOrderByDestacadoDescFechaDesc();

    /**
     * Búsqueda por tecnología con LIKE + ignore case.
     * NOTA: concatena '%' || :texto || '%' → JPQL clásico.
     * En PostgreSQL equivalente a ILIKE (sin diferenciar mayúsculas).
     */
    @Query("SELECT p FROM Proyecto p WHERE LOWER(p.tecnologias) LIKE LOWER(CONCAT('%', :texto, '%')) ORDER BY p.fecha DESC")
    List<Proyecto> buscarPorTecnologia(String texto);
}
