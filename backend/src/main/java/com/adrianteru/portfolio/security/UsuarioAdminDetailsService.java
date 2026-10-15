package com.adrianteru.portfolio.security;

import com.adrianteru.portfolio.entity.UsuarioAdmin;
import com.adrianteru.portfolio.repository.UsuarioAdminRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * PUENTE entre Spring Security y nuestra BBDD.
 *
 * <p>Spring Security no sabe leer nuestras tablas; delega en esta interfaz:
 * cuando necesita "cargar al usuario" (login o peticiones), llama a
 * {@link #loadUserByUsername(String)} y nosotros le devolvemos un
 * {@link UserDetails} con el hash BCrypt dentro.</p>
 *
 * <p>Flujo del login:</p>
 * <pre>
 *  POST /api/auth/login  {username, password}
 *        ↓
 *  AuthenticationManager (de Spring)
 *        ↓  llama a este servicio
 *  loadUserByUsername("admin") → UserDetails con hash $2a$10$...
 *        ↓
 *  Spring compara password con el hash (BCryptPasswordEncoder)
 *        ↓ OK
 *  Generamos y devolvemos el JWT
 * </pre>
 *
 * <p>La clase {@code User} de Spring Security es una implementación estándar
 * de UserDetails: lleva username, password (hash) y roles/authorities.</p>
 */
@Service
public class UsuarioAdminDetailsService implements UserDetailsService {

    private final UsuarioAdminRepository usuarioAdminRepository;

    public UsuarioAdminDetailsService(UsuarioAdminRepository usuarioAdminRepository) {
        this.usuarioAdminRepository = usuarioAdminRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UsuarioAdmin usuario = usuarioAdminRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado: " + username));

        if (!usuario.isActivo()) {
            // Usuario deshabilitado → Spring devolverá 403 (inhabilitado).
            throw new UsernameNotFoundException("Usuario deshabilitado: " + username);
        }

        // "ROLE_ADMIN" → la convención de Spring: los roles llevan prefijo ROLE_
        // y con hasRole("ADMIN") se comprueban. Usamos una authority simple.
        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword())   // ← el HASH BCrypt, no texto plano
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .build();
    }
}
