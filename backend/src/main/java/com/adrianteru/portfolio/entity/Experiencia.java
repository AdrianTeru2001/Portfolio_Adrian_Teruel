package com.adrianteru.portfolio.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

/**
 * ENTIDAD: Experiencia laboral / prácticas
 *
 * <p>Estructura casi idéntica a {@link Estudio} a propósito: comparten el patrón
 * "fecha inicio / fin / en curso" y la sección de Trayectoria del frontend
 * mezcla ambas en una única timeline.</p>
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "experiencias")
public class Experiencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Puesto: "Desarrollador Backend", "Becario FP DAW"... */
    @Column(nullable = false, length = 200)
    private String puesto;

    @Column(nullable = false, length = 200)
    private String empresa;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "en_curso", nullable = false)
    private boolean enCurso;

    /** Responsabilidades y logros. Se recomienda empezar con verbo de acción. */
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "orden")
    private Integer orden;
}
