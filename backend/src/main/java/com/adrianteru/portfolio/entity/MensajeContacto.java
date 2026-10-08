package com.adrianteru.portfolio.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

/**
 * ENTIDAD: MensajeContacto
 *
 * <p>Lo que rellena el formulario de "Contacto" del frontend. Solo el admin
 * autenticado podrá listarlos/eliminarlos (Fase 2).</p>
 *
 * <b>Observaciones de seguridad:</b>
 * <ul>
 *   <li>La tabla es de SOLO ESCRITURA para el público: no existe endpoint público
 *       que devuelva mensajes (evita que alguien liste los correos de otros).</li>
 *   <li>La validación (email válido, longitud máxima) se hace en el DTO de entrada
 *       con Bean Validation → si alguien lo intenta con curl recibirá un 400.</li>
 * </ul>
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "mensajes_contacto")
public class MensajeContacto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    /** Email del remitente (validado en el DTO, no en la entidad). */
    @Column(nullable = false, length = 150)
    private String email;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido;

    /**
     * Instant = punto exacto en el tiempo (UTC). Se usa para timestamps porque
     * NO depende de la zona horaria del servidor; se guarda como TIMESTAMPTZ.
     * El valor lo pone el backend (nunca confíes en el "fecha" que llegue del cliente).
     */
    @Column(name = "fecha", nullable = false, updatable = false)
    private Instant fecha;

    /**
     * Para marcar mensajes ya leídos desde el panel admin (esto es una mejora
     * futura posible; la columna se crea ya para no migrar después).
     */
    @Column(name = "leido", nullable = false)
    private boolean leido;

    /** Hook de JPA: se ejecuta ANTES del primer insert. */
    @PrePersist
    private void antesDeInsertar() {
        // Si el cliente no envió fecha, la ponemos "ahora" (en UTC).
        if (fecha == null) {
            fecha = Instant.now();
        }
    }
}
