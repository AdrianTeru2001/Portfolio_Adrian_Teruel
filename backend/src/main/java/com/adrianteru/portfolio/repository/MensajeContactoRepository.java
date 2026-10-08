package com.adrianteru.portfolio.repository;

import com.adrianteru.portfolio.entity.MensajeContacto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensajeContactoRepository extends JpaRepository<MensajeContacto, Long> {

    /** Los más recientes primero (para el panel del admin). */
    List<MensajeContacto> findAllByOrderByFechaDesc();
}
