package com.adrianteru.portfolio.repository;

import com.adrianteru.portfolio.entity.CategoriaCompetencia;
import com.adrianteru.portfolio.entity.Competencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompetenciaRepository extends JpaRepository<Competencia, Long> {

    /**
     * Query derivada con PARÁMETRO: el tipo del parámetro del método
     * (CategoriaCompetencia) se corresponde con el campo "categoria".
     * El SQL generado será: ... WHERE categoria = ? ORDER BY nombre
     */
    List<Competencia> findByCategoriaOrderByNombre(CategoriaCompetencia categoria);

    /** Todas ordenadas por categoría y luego por nivel descendente. */
    List<Competencia> findAllByOrderByCategoriaAscNivelDesc();
}
