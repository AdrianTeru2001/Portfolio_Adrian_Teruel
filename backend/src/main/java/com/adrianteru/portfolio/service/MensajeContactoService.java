package com.adrianteru.portfolio.service;

import com.adrianteru.portfolio.dto.MensajeContactoRequest;
import com.adrianteru.portfolio.entity.MensajeContacto;
import com.adrianteru.portfolio.repository.MensajeContactoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * SERVICIO: mensajes del formulario de contacto.
 *
 * <p>Endpoint público de escritura — el más delicado del sistema porque es el
 * que un desconocido puede llamar. En la Fase 2 añadiremos protección extra
 * (rate limiting / honeypot), de momento confiamos en la validación del DTO.</p>
 */
@Service
public class MensajeContactoService {

    private final MensajeContactoRepository mensajeRepository;

    public MensajeContactoService(MensajeContactoRepository mensajeRepository) {
        this.mensajeRepository = mensajeRepository;
    }

    /**
     * Guarda un mensaje entrante.
     *
     * <p>Nota sobre seguridad: la fecha la pone el backend ({@code @PrePersist}
     * en la entidad), nunca se acepta del cliente. Así nadie puede falsificar
     * cuándo se envió un mensaje.</p>
     */
    @Transactional
    public MensajeContacto guardar(MensajeContactoRequest dto) {
        MensajeContacto mensaje = new MensajeContacto();
        mensaje.setNombre(dto.nombre());
        mensaje.setEmail(dto.email());
        mensaje.setContenido(dto.contenido());
        // fecha y leido los inicializa la entidad (@PrePersist / default false)
        return mensajeRepository.save(mensaje);
    }
}
