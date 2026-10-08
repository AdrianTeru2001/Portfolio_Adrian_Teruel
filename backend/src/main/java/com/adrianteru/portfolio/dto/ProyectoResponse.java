package com.adrianteru.portfolio.dto;

import com.adrianteru.portfolio.entity.Proyecto;

import java.time.LocalDate;

/**
 * DTO de SALIDA para un proyecto.
 *
 * <p>Se construye desde la entidad con {@link #from(Proyecto)} (patrón "mapper"
 * manual). Ventajas frente a devolver la entidad:</p>
 * <ul>
 *   <li>La entidad puede tener campos internos que NO deben salir por HTTP</li>
 *   <li>Si mañana cambias la entidad, la API pública no se rompe</li>
 *   <li>Evita problemas de JSON con proxies de Hibernate
 *       (LazyInitializationException)</li>
 * </ul>
 *
 * <p>Record inmutable: si Spring lo serializa a JSON genera exactamente los
 * campos del record, ni uno más.</p>
 */
public record ProyectoResponse(
        Long id,
        String nombre,
        String descripcion,
        String tecnologias,
        String urlRepo,
        String urlDemo,
        String imagenUrl,
        boolean destacado,
        LocalDate fecha
) {
    /**
     * Fábrica estática: entidad → DTO.
     * Es convención común en APIs Java ({@code ProyectoResponse.from(entidad)}).
     */
    public static ProyectoResponse from(Proyecto p) {
        return new ProyectoResponse(
                p.getId(),
                p.getNombre(),
                p.getDescripcion(),
                p.getTecnologias(),
                p.getUrlRepo(),
                p.getUrlDemo(),
                p.getImagenUrl(),
                p.isDestacado(),
                p.getFecha()
        );
    }
}
