package com.adrianteru.portfolio.exception;

import com.adrianteru.portfolio.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * MANEJADOR GLOBAL DE EXCEPCIONES.
 *
 * <p>{@code @RestControllerAdvice} = combina {@code @ControllerAdvice}
 * (intercepta excepciones de CUALQUIER controller) + {@code @RestController}
 * (devuelve JSON en vez de vista HTML).</p>
 *
 * <p>Sin esta clase, si lanza una excepción Java, el usuario recibiría un feo
 * 500 con stacktrace (o una página blanca de error de Tomcat). Con ella,
 * siempre recibirá un JSON estructurado con el código HTTP correcto:</p>
 *
 * <ul>
 *   <li>{@link ResourceNotFoundException} → <b>404</b></li>
 *   <li>{@link MethodArgumentNotValidException} → <b>400</b> con detalle de campos</li>
 *   <li>Cualquier otra → <b>500</b> (sin filtrar detalles internos al cliente)</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 404: recurso inexistente.
     * El parámetro {@code HttpServletRequest} da acceso al contexto del
     * request (para saber la URL que falló, útil para logs).
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(ResourceNotFoundException ex,
                                                      HttpServletRequest request) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    /**
     * 401: credenciales incorrectas en el login.
     *
     * <p>{@code BadCredentialsException} la lanza AuthenticationManager cuando
     * BCrypt no coincide (o el usuario no existe — el mensaje es el mismo a
     * propósito, para no revelar qué usuarios hay en el sistema).</p>
     */
    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ErrorResponse> noAutenticado(
            org.springframework.security.core.AuthenticationException ex,
            HttpServletRequest request) {
        // No devolvemos ex.getMessage() tal cual: puede filtrar internos.
        return construir(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas", request);
    }

    /**
     * 400: falló la Bean Validation del DTO ({@code @Valid} en el controller).
     *
     * <p>Recorremos los errores de campo y creamos un mapa
     * {"nombre": "El nombre es obligatorio", ...} que se añade al body —
     * así el frontend puede pintar el error debajo de cada input.</p>
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacionFallida(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> campos = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            // Primer mensaje de validación de cada campo (si hay varios, el primero)
            campos.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", "Datos de entrada inválidos");
        body.put("path", request.getRequestURI());
        body.put("fields", campos);   // ← el detalle por campo

        return ResponseEntity.badRequest().body(body);
    }

    /**
     * 400: el body JSON no se pudo PARSEAR (JSON roto, vacío, o un `null`
     * donde se esperaba un primitivo...).
     *
     * <p>Esto ocurre ANTES de la validación de campos: si no lo capturamos
     * aquí, caería en el handler genérico de Exception y el cliente
     * recibiría un 500 (como si hubiéramos roto el servidor), cuando en
     * realidad el problema es su petición. Descubierto por un test (Fase 6).</p>
     */
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> jsonInvalido(
            org.springframework.http.converter.HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        // Mensaje genérico: el detalle del parse (a veces con tipos internos
        // del JSON) no aporta nada al cliente y puede filtrar información.
        return construir(HttpStatus.BAD_REQUEST,
                "JSON inválido o incompleto", request);
    }

    /**
     * 415: el cliente envió un Content-Type que no sabemos deserializar
     * (p. ej. text/plain donde esperamos application/json).
     */
    @ExceptionHandler(org.springframework.web.HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> tipoNoSoportado(
            org.springframework.web.HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request) {
        return construir(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Tipo de contenido no soportado: usa application/json", request);
    }

    /**
     * 500: cualquier error no previsto.
     *
     * <b>Importante:</b> NO devolvemos ex.getMessage() al cliente si podría
     * filtrar información interna (nombres de tablas, rutas...). Solo en
     * desarrollo interesa el detalle; en producción, un mensaje genérico.
     * Aquí simplificamos mostrando el mensaje, pero en un proyecto real
     * añadirías un flag de perfil.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> errorInterno(Exception ex,
                                                      HttpServletRequest request) {
        // En un proyecto real: log.error(...) aquí para dejar rastro en los logs.
        return construir(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor", request);
    }

    /** Fábrica común del JSON de error. */
    private ResponseEntity<ErrorResponse> construir(HttpStatus status,
                                                    String mensaje,
                                                    HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),   // "Not Found", "Internal Server Error"...
                mensaje,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }
}
