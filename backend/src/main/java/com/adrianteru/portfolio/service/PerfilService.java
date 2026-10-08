package com.adrianteru.portfolio.service;

import com.adrianteru.portfolio.dto.PerfilUpdateRequest;
import com.adrianteru.portfolio.entity.Perfil;
import com.adrianteru.portfolio.repository.PerfilRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * SERVICIO: lógica de negocio del perfil.
 *
 * <p>¿Por qué una capa extra entre Controller y Repository?</p>
 * <ul>
 *   <li>El Controller solo entiende de HTTP (leer body, devolver status codes)</li>
 *   <li>El Repository solo entiende de SQL</li>
 *   <li>El Service es donde vive lo que "decide": reglas, combinaciones de
 *       operaciones, mapeos entidad↔DTO...</li>
 * </ul>
 *
 * <p><b>@Transactional:</b> una transacción = varias operaciones que deben
 * aplicarse TODAS o NINGUNA (si algo falla a mitad, se deshace todo).
 * Spring la gestiona con el patrón "Template Method": abre al entrar en el
 * método, commit al salir bien, rollback si salta una excepción.</p>
 * <ul>
 *   <li>{@code readOnly=true} en lecturas → Hibernate optimiza (p.ej. no hace
 *       dirty checking, puede usar un modo más ligero)</li>
 *   <li>Sin @Transactional, cada repository hace su propia transacción
 *       (y dos operaciones del service NO estarían atómicas entre sí)</li>
 * </ul>
 */
@Service
public class PerfilService {

    private final PerfilRepository perfilRepository;

    /**
     * INYECCIÓN DE DEPENDENCIAS por constructor (la recomendada por Spring):
     * Spring crea el Repository (bean) y lo "mete" aquí automáticamente.
     * Ventajas: sin @Autowired (que puede ser field-injection y oculta
     * dependencias), y el campo puede ser final.
     */
    public PerfilService(PerfilRepository perfilRepository) {
        this.perfilRepository = perfilRepository;
    }

    /** Devuelve el perfil con id=1, o lo crea con valores mínimos si no existe. */
    @Transactional(readOnly = true)
    public Perfil obtenerPerfil() {
        List<Perfil> todos = perfilRepository.findAll();
        if (todos.isEmpty()) {
            // Caso defensivo: la semilla (V2__seed) crea el perfil, pero si
            // alguien borrara la fila, la API no debe romperse con 500.
            return new Perfil();
        }
        return todos.get(0);
    }

    /**
     * Actualiza el perfil existente con los datos del DTO.
     *
     * <p>Patrón "copy por campo": el DTO no tiene id porque el registro que se
     * edita viene de la URL/BBDD, no del body (evita mass assignment).</p>
     */
    @Transactional
    public Perfil actualizarPerfil(PerfilUpdateRequest dto) {
        Perfil perfil = obtenerPerfil();

        // Si el perfil no tenía id (no existía), lo guardamos como nuevo.
        if (perfil.getId() == null) {
            perfil = new Perfil();
        }

        // Copia campo a campo: explícito y controlado.
        // (Alternativa: BeanUtils.copyProperties, pero usa reflexión implícita
        //  y es frágil ante renombrar un campo sin darse cuenta).
        perfil.setNombre(dto.nombre());
        perfil.setTituloProfesional(dto.tituloProfesional());
        perfil.setBio(dto.bio());
        perfil.setFotoUrl(dto.fotoUrl());
        perfil.setEmail(dto.email());
        perfil.setTelefono(dto.telefono());
        perfil.setLinkedinUrl(dto.linkedinUrl());
        perfil.setGithubUrl(dto.githubUrl());
        perfil.setUbicacion(dto.ubicacion());

        return perfilRepository.save(perfil);
    }
}
