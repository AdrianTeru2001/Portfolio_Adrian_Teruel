package com.adrianteru.portfolio.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * ENTIDAD: Perfil
 * ==============
 * Representa una fila de la tabla "perfil".
 *
 * <p>En este proyecto el perfil es SINGLETON lógico: solo hay una fila (id=1)
 * con los datos personales de Adrián. La editas desde el panel admin.</p>
 *
 * <b>Conceptos clave:</b>
 * <ul>
 *   <li>{@code @Entity} → Spring Data JPA mapea esta clase a una tabla.</li>
 *   <li>{@code @Table(name="perfil")} → nombre explícito de la tabla (opcional
 *       pero recomendable: el nombre por defecto sería "perfil" igualmente,
 *       pero en otras BBDD conviene fijarlo).</li>
 *   <li>{@code @Id + @GeneratedValue} → clave primaria autogenerada.
 *       Con PostgreSQL usamos IDENTITY (que equivale a BIGSERIAL). El valor lo
 *       asigna la BBDD al insertar y Hibernate lo refleja en el objeto.</li>
 *   <li>{@code Lombok} → @Getter/@Setter generan los getters y setters en
 *       tiempo de compilación; sin ellos tendríamos ~200 líneas de boilerplate.</li>
 * </ul>
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "perfil")
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre completo. length = tamaño de columna VARCHAR en la migración. */
    @Column(nullable = false, length = 100)
    private String nombre;

    /** Título profesional corto, ej: "Desarrollador Java Full Stack". */
    @Column(name = "titulo_profesional", length = 150)
    private String tituloProfesional;

    /** Bio "sobre mí". TEXT en la BBDD (ilimitado). */
    @Column(columnDefinition = "TEXT")
    private String bio;

    /** URL de la foto (se subirá a un servicio externo o /assets). */
    @Column(name = "foto_url", length = 500)
    private String fotoUrl;

    @Column(length = 150)
    private String email;

    @Column(name = "telefono", length = 30)
    private String telefono;

    @Column(name = "linkedin_url", length = 300)
    private String linkedinUrl;

    @Column(name = "github_url", length = 300)
    private String githubUrl;

    /** Ubicación, ej: "España". */
    @Column(name = "ubicacion", length = 100)
    private String ubicacion;
}
