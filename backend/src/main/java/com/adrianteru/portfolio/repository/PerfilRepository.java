package com.adrianteru.portfolio.repository;

import com.adrianteru.portfolio.entity.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * REPOSITORIO de Perfil.
 *
 * <p>Solo extender {@link JpaRepository} ya te regala CRUD completo:</p>
 * <pre>
 *   save(entity)          → INSERT (o UPDATE si tiene id)
 *   findById(id)          → Optional&lt;Perfil&gt;
 *   findAll()             → List&lt;Perfil&gt;
 *   deleteById(id)        → DELETE
 *   count() ...
 * </pre>
 *
 * <p>Spring Data implementa la interfaz EN TIEMPO DE EJECUCIÓN (proxy dinámico):
 * no hay que escribir ni una sola línea de SQL para el CRUD básico. Tampoco hay
 * que poner {@code @Repository}: Spring lo registra automáticamente, pero la
 * anotación ayuda a leer el código y permite capturar excepciones de la capa.</p>
 *
 * <p>¿Y el SQL? Lo genera Hibernate a partir de las @Entity. Para consultas
 * personalizadas hay 3 niveles (de más simple a más potente):</p>
 * <ol>
 *   <li>Métodos con nombre derivado → {@code findByTituloContainingIgnoreCase(String t)}</li>
 *   <li>JPQL con @Query → SQL portable entre BBDD (usa nombres de ENTIDAD, no de tabla)</li>
 *   <li>Native query → SQL crudo de PostgreSQL (cuando JPQL no da abasto)</li>
 * </ol>
 */
@Repository
public interface PerfilRepository extends JpaRepository<Perfil, Long> {
    // El perfil es singleton lógico: método para traer el único registro (id=1).
    // Devolvemos Optional porque puede no existir aún (si no se corrió el seed).
}
