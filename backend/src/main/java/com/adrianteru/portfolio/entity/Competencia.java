package com.adrianteru.portfolio.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * ENTIDAD: Competencia (skills)
 *
 * <p>Ejemplos: Java, Spring Boot, Angular, SQL, Git, Docker...</p>
 *
 * <b>Decisión de diseño:</b> la categoría se almacena como STRING con un
 * conjunto cerrado de valores (ver {@link CategoriaCompetencia}) en vez de una
 * tabla separada. Razones:
 * <ul>
 *   <li>El dominio es pequeño y estable (5 categorías, no cambia cada semana)</li>
 *   <li>Evita un JOIN innecesario en cada consulta</li>
 *   <li>En JPA: {@code @Enumerated(EnumType.STRING)} guarda el nombre del enum
 *       ("LENGUAJE"), no su ordinal numérico → si mañana reordenas el enum,
 *       los datos siguen siendo válidos. ¡Nunca uses ORDINAL en producción!</li>
 * </ul>
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "competencias")
public class Competencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    /**
     * Categoría técnica. Se persiste como VARCHAR con el nombre del enum.
     * Si llegara un valor desconocido de la BBDD, JPA lanzaría una excepción
     * (mejor que guardar basura silenciosamente).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoriaCompetencia categoria;

    /**
     * Nivel de 1 a 5 (o 0-100 si prefieres porcentaje).
     * La validación de rango NO está aquí sino en el DTO de entrada:
     * las entidades reflejan el estado guardado, los DTOs validan lo que llega.
     */
    @Column(nullable = false)
    private Integer nivel;

    /** Para agrupar visualmente dentro de una categoría, ej: "Backend", "Frontend". */
    @Column(name = "subgrupo", length = 100)
    private String subgrupo;
}
