package com.adrianteru.portfolio.repository;

import com.adrianteru.portfolio.entity.UsuarioAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioAdminRepository extends JpaRepository<UsuarioAdmin, Long> {

    /**
     * Query derivada: SELECT * FROM usuarios_admin WHERE username = ?
     * Devuelve Optional porque puede no existir (login incorrecto).
     */
    Optional<UsuarioAdmin> findByUsername(String username);
}
