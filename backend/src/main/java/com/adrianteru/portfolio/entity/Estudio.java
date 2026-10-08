package com.adrianteru.portfolio.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

/**
 * ENTIDAD: Estudio (formación académica)
 *
 * <p>Se muestra en la sección "Trayectoria" del portfolio, ordenada por fecha.</p>
 *
 * <b>Nota sobre fechas:</b> usamos {@link LocalDate} (fecha sin hora). JPA lo
 * mapea al tipo DATE de PostgreSQL. Alternativas:
 * <ul>
 *   <li>LocalDateTime → si necesitara hora (p. ej. mensajes de contacto)</li>
 *   <li>Instant → punto exacto del reloj UTC, ideal para timestamps de auditoría</li>
 * </ul>
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "estudios")
public class Estudio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    /** Centro educativo: "IES ..." , "UOC", "Platzi"... */
    @Column(nullable = false, length = 200)
    private String centro;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    /** Puede ser null si el estudio está en curso → columna nullable. */
    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    /**
     * Flag de "en curso". Si es true, la API ignora fechaFin.
     * Es redundante con (fechaFin == null) pero hace más legible las consultas
     * y el frontend: {@code WHERE en_curso = true} es más claro que
     * {@code WHERE fecha_fin IS NULL}.
     */
    @Column(name = "en_curso", nullable = false)
    private boolean enCurso;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Orden de visualización manual (1 = primero). Sin él, el orden lo
     * decidiría la fecha; con él, el usuario puede priorizar un título.
     */
    @Column(name = "orden")
    private Integer orden;
}
