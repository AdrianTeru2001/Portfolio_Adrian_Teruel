package com.adrianteru.portfolio.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * ENTIDAD: UsuarioAdmin (credenciales del panel de administración).
 *
 * <p>NO confundir con "perfil": el perfil son datos públicos; esta entidad
 * solo existe para autenticar. Por eso:</p>
 * <ul>
 *   <li>Nunca se expone por la API pública (no hay GET /api/usuarios)</li>
 *   <li>Solo la lee {@code UsuarioAdminDetailsService} durante el login</li>
 * </ul>
 *
 * <p><b>Contraseña:</b> el campo guarda SIEMPRE el hash BCrypt
 * (ej: {@code $2a$10$iRIs...}). Spring lo verifica con
 * {@code BCryptPasswordEncoder.matches("texto plano", hash)} — el texto plano
 * nunca viaja ni se guarda en ningún sitio.</p>
 */
@Getter
@Setter
@Entity
@Table(name = "usuarios_admin")
public class UsuarioAdmin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identificador usado en el formulario de login. */
    @Column(nullable = false, unique = true, length = 60)
    private String username;

    /** Hash BCrypt. Nunca el texto plano (ni en BD ni en logs ni en API). */
    @Column(nullable = false, length = 100)
    private String password;

    /** Permite deshabilitar un admin sin borrar la fila. */
    @Column(nullable = false)
    private boolean activo;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private java.time.Instant creadoEn;

    @Column(name = "ultimo_acceso")
    private java.time.Instant ultimoAcceso;

    @PrePersist
    private void antesDeInsertar() {
        if (creadoEn == null) {
            creadoEn = java.time.Instant.now();
        }
    }
}
