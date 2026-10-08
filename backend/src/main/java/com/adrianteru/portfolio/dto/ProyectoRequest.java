package com.adrianteru.portfolio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de ENTRADA para crear/modificar un proyecto (POST/PUT /api/proyectos).
 *
 * <p>Obsérvalo bien: NO incluye "id" (lo asigna la BBDD) ni "destacado" aquí
 * como opcional con valor por defecto... en realidad sí, es primitivo boolean
 * y por defecto es false — quien llame puede decidirlo.</p>
 *
 * <p>Se usa tanto para POST (crear) como PUT (editar). En POST, "id" no se usa;
 * en PUT, el id va en la URL ({@code /api/proyectos/5}), no en el cuerpo:
 * es la convención REST más extendida.</p>
 */
public record ProyectoRequest(

        @NotBlank(message = "El nombre del proyecto es obligatorio")
        @Size(max = 200)
        String nombre,

        @Size(max = 5000)
        String descripcion,

        @Size(max = 300)
        String tecnologias,

        @Size(max = 300)
        String urlRepo,

        @Size(max = 300)
        String urlDemo,

        @Size(max = 500)
        String imagenUrl,

        boolean destacado,

        // LocalDate no lleva anotación: si llega null, se acepta (fecha opcional)
        java.time.LocalDate fecha
) {
}
