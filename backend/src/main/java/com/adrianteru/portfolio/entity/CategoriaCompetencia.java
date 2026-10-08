package com.adrianteru.portfolio.entity;

/**
 * Enumeración de categorías de competencias.
 *
 * <p>Un ENUM en Java es un tipo de datos con un conjunto FIJO de valores:
 * si intentas asignar algo fuera de esta lista, no compila. Eso evita errores
 * como "JAVVA" o "FrameWork".</p>
 *
 * <p>En la BBDD se guarda como texto: 'LENGUAJE', 'FRAMEWORK'...</p>
 */
public enum CategoriaCompetencia {
    LENGUAJE,       // Java, TypeScript, SQL, HTML/CSS...
    FRAMEWORK,      // Spring Boot, Angular, Hibernate...
    HERRAMIENTA,    // Git, Docker, IntelliJ, Jenkins...
    BASE_DE_DATOS,  // PostgreSQL, MySQL, Redis...
    BLANDA          // Comunicación, trabajo en equipo... (los recruiters también miran esto)
}
