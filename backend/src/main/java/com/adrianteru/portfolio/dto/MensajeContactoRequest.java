package com.adrianteru.portfolio.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de ENTRADA para el formulario de contacto (POST /api/contacto).
 *
 * <p>Es el ÚNICO endpoint que el público puede escribir sin autenticación,
 * por lo que la validación aquí es la primera línea de defensa:</p>
 * <ul>
 *   <li>{@code @NotBlank} → no vacío ni solo espacios</li>
 *   <li>{@code @Email} → formato "algo@algo.algo"</li>
 *   <li>{@code @Size(max=...)} → límite de longitud = tamaño de la columna;
 *       sin esto, un texto de 10.000 caracteres causaría un error 500 de la BBDD</li>
 * </ul>
 *
 * <p>Si la validación falla, Spring devuelve automáticamente un <b>400</b>
 * con el detalle de cada campo ( gracias a {@code @Valid} en el controller +
 * {@code spring-boot-starter-validation}).</p>
 */
public record MensajeContactoRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100)
        String nombre,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Email con formato inválido")
        @Size(max = 150)
        String email,

        @NotBlank(message = "El mensaje no puede estar vacío")
        @Size(max = 5000, message = "El mensaje no puede superar 5000 caracteres")
        String contenido
) {
}
