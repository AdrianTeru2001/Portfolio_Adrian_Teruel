package com.adrianteru.portfolio.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de ENTRADA para actualizar el perfil (PUT /api/perfil).
 *
 * <p><b>¿Por qué no recibir la entidad Perfil directamente?</b></p>
 * <ol>
 *   <li><b>Seguridad:</b> si aceptas la entidad completa, un cliente malicioso
 *       podría enviar un JSON con "id": 999 y sobrescribir otro registro
 *       (mass assignment). El DTO solo expone los campos que DEBEN llegar.</li>
 *   <li><b>Validación:</b> aquí vive Bean Validation (@NotBlank, @Size...),
 *       que se ejecuta automáticamente antes de entrar en el controller.</li>
 *   <li><b>Desacople:</b> la API puede evolucionar sin tocar el modelo interno.</li>
 * </ol>
 *
 * <p>Usamos un <b>record</b> de Java: tipo inmutable con constructor, getters
 * ({@code dto.nombre()}), equals y hashCode de serie. Ideal para DTOs.</p>
 *
 * <p><b>Spring 6+/Jakarta:</b> las anotaciones de validación son
 * {@code jakarta.validation.*} (antes {@code javax.validation.*}).</p>
 */
public record PerfilUpdateRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String nombre,

        @Size(max = 150)
        String tituloProfesional,

        @Size(max = 2000)
        String bio,

        @Size(max = 500)
        String fotoUrl,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        String email,

        @Size(max = 30)
        String telefono,

        @Size(max = 300)
        String linkedinUrl,

        @Size(max = 300)
        String githubUrl,

        @Size(max = 100)
        String ubicacion
) {
}
