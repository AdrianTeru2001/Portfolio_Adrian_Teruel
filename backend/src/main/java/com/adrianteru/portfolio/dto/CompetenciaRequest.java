package com.adrianteru.portfolio.dto;

import com.adrianteru.portfolio.entity.CategoriaCompetencia;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de ENTRADA para crear/editar una competencia (admin).
 *
 * <p>Fíjate en la validación del nivel: @Min(1) @Max(5) garantizan el rango
 * ANTES de tocar la BBDD. Sin esto, un "nivel: 99" llegaría a PostgreSQL y
 * podría provocar un error 500 si la columna tuviera un CHECK constraint
 * (o guardar basura si no lo tuviera).</p>
 */
public record CompetenciaRequest(

        @NotBlank(message = "El nombre de la competencia es obligatorio")
        String nombre,

        @NotNull(message = "La categoría es obligatoria")
        CategoriaCompetencia categoria,

        @Min(value = 1, message = "El nivel mínimo es 1")
        @Max(value = 5, message = "El nivel máximo es 5")
        Integer nivel,

        String subgrupo
) {
}
