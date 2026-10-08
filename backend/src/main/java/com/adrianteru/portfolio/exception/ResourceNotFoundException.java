package com.adrianteru.portfolio.exception;

/**
 * Excepción de dominio para "recurso no encontrado" (equivalente HTTP: 404).
 *
 * <p><b>¿Por qué una excepción propia en vez de RuntimeException?</b>
 * Porque permite al {@link GlobalExceptionHandler} traducir CADA tipo de
 * error a su código HTTP adecuado: esta → 404, validación → 400,
 * genérica → 500. Es el patrón estándar de las APIs Spring.</p>
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String mensaje) {
        super(mensaje);   // super() pasa el mensaje a la clase padre (RuntimeException)
    }
}
