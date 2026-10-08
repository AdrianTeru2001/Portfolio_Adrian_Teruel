package com.adrianteru.portfolio.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CONFIGURACIÓN DE CORS.
 *
 * <p><b>¿Qué es CORS?</b> Cross-Origin Resource Sharing. El navegador aplica
 * la "política del mismo origen": una página en http://localhost:4200 (Angular)
 * NO puede leer respuestas de http://localhost:8080 (esta API) salvo que el
 * servidor permita explícitamente ese origen mediante cabeceras:</p>
 *
 * <pre>Access-Control-Allow-Origin: http://localhost:4200</pre>
 *
 * <p>Sin esta configuración, verías en la consola del navegador el error:</p>
 * <pre>Blocked by CORS policy: No 'Access-Control-Allow-Origin' header</pre>
 *
 * <p><b>Nota:</b> CORS es una protección del NAVEGADOR, no del servidor.
 * Un atacante con curl/Postman puede llamar a la API sin ninguna cabecera —
 * por eso la autenticación real (JWT, Fase 2) es imprescindible de todas formas.</p>
 *
 * <p>El origen permitido viene de {@code app.cors.allowed-origins} en
 * application.yml → en local es localhost:4200, en producción se cambia con la
 * variable de entorno CORS_ALLOWED_ORIGINS (p. ej. https://adrianteruel.dev).</p>
 */
@Configuration
public class CorsConfig {

    // @Value lee una propiedad del archivo de configuración (application.yml)
    // y la inyecta en el campo. La sintaxis ${...} es la misma que usamos allí.
    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;

    /**
     * WebMvcConfigurer permite personalizar el MVC de Spring.
     * Registramos una regla global de CORS aplicable a TODOS los controllers
     * (también a los de errores, cosa que hacerlo por controller no cubre).
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")          // qué rutas afecta
                        .allowedOrigins(allowedOrigins) // quién puede llamar
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")            // cualquier cabecera (Authorization, Content-Type...)
                        .allowCredentials(true)         // permite cookies/auth si hiciera falta
                        .maxAge(3600);                  // cachea la respuesta preflight 1h
            }
        };
    }
}
