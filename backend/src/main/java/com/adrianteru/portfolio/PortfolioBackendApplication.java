package com.adrianteru.portfolio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CLASE PRINCIPAL del backend.
 *
 * <p>{@code @SpringBootApplication} es un "meta-alias" que agrupa tres anotaciones:</p>
 *
 * <ul>
 *   <li><b>@Configuration</b> → esta clase puede definir beans (objetos gestionados por Spring)</li>
 *   <li><b>@EnableAutoConfiguration</b> → Spring "adivina" qué configurar según las
 *       dependencias del classpath (si hay PostgreSQL + JPA, monta el pool de
 *       conexiones y Hibernate automáticamente)</li>
 *   <li><b>@ComponentScan</b> → escanea TODO el paquete {@code com.adrianteru.portfolio}
 *       (y subpaquetes) buscando clases con {@code @Service}, {@code @RestController},
 *       {@code @Repository}... y las registra como beans.
 *       <b>Por eso el paquete base importa:</b> si esta clase estuviera en otro paquete,
 *       no encontraría el resto del código.</li>
 * </ul>
 *
 * <p>Al ejecutar {@code main()}, Spring levanta un servidor web embebido (Tomcat)
 * y queda escuchando peticiones en el puerto 8080.</p>
 */
@SpringBootApplication
public class PortfolioBackendApplication {

    public static void main(String[] args) {
        // SpringApplication.run() = arranca el "contexto de Spring":
        // crea los beans, ejecuta los CommandLineRunners y deja Tomcat vivo.
        SpringApplication.run(PortfolioBackendApplication.class, args);
    }
}
