package com.adrianteru.portfolio.dto;

import java.time.Instant;

/**
 * DTO de RESPUESTA estándar para errores de la API.
 *
 * <p>Formato de error consistente en TODA la API:</p>
 * <pre>
 * {
 *   "timestamp": "2026-10-08T10:30:00Z",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "Proyecto no encontrado con id 42",
 *   "path": "/api/proyectos/42"
 * }
 * </pre>
 *
 * <p>Un formato de error predecible es señal de una API profesional:
 * el frontend puede manejar errores sin parsear HTML de error de Tomcat.</p>
 *
 * <p>Se construye desde {@code GlobalExceptionHandler}.</p>
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
