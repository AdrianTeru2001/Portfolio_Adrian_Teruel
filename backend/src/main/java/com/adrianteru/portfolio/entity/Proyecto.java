package com.adrianteru.portfolio.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

/**
 * ENTIDAD: Proyecto
 *
 * <p>La estrella del portfolio: cada tarjeta de la sección "Proyectos" es una fila
 * de esta tabla. A los recruiters les interesa especialmente: descripción breve,
 * tecnologías usadas y enlaces a repo/demo.</p>
 *
 * <b>Nota:</b> {@code tecnologias} se guarda como texto separado por comas
 * ("Spring Boot, Angular, PostgreSQL"). Una alternativa "más correcta" sería una
 * tabla intermedia proyecto↔competencia (relación N:N), pero añade complejidad y
 * para un portfolio personal el texto plano es suficiente y flexible.
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "proyectos")
public class Proyecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    /** Stack usado, separado por comas. */
    @Column(name = "tecnologias", length = 300)
    private String tecnologias;

    /** URL del repositorio (GitHub). Clave para que el recruiter pueda ver código. */
    @Column(name = "url_repo", length = 300)
    private String urlRepo;

    /** URL de la demo desplegada (puede ser null si aún no está desplegado). */
    @Column(name = "url_demo", length = 300)
    private String urlDemo;

    /** Imagen de portada (captura). Se sirve desde /assets o un bucket. */
    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    /** Si es true, aparece destacado en la home. */
    @Column(name = "destacado", nullable = false)
    private boolean destacado;

    /** Año o fecha del proyecto, para ordenar de más reciente a antiguo. */
    @Column(name = "fecha")
    private LocalDate fecha;

    @Column(name = "orden")
    private Integer orden;
}
