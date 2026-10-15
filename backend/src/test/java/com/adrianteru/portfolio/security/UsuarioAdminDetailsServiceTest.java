package com.adrianteru.portfolio.security;

import com.adrianteru.portfolio.entity.UsuarioAdmin;
import com.adrianteru.portfolio.repository.UsuarioAdminRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * TESTS UNITARIOS de UsuarioAdminDetailsService (Fase 6).
 *
 * <p>Es el PUENTE entre Spring Security y nuestra tabla de administradores.
 * Su lógica tiene 3 ramas, y cada una es una puerta de seguridad:</p>
 * <ol>
 *   <li>Usuario encontrado y activo → devolver authorities (ROLE_ADMIN).</li>
 *   <li>Usuario NO existe → UsernameNotFoundException (login imposible).</li>
 *   <li>Usuario existe pero DESACTIVADO → también UsernameNotFoundException
 *       (deshabilitar una cuenta debe cerrar la puerta al instante).</li>
 * </ol>
 */
@ExtendWith(MockitoExtension.class)
class UsuarioAdminDetailsServiceTest {

    @Mock
    private UsuarioAdminRepository usuarioAdminRepository;

    @InjectMocks
    private UsuarioAdminDetailsService service;

    @Test
    @DisplayName("Devuelve un UserDetails con el rol ROLE_ADMIN")
    void loadUser_devuelveUserDetailsConRolAdmin() {
        when(usuarioAdminRepository.findByUsername("admin"))
                .thenReturn(Optional.of(usuario("admin", "$2a$10$hash", true)));

        UserDetails detalles = service.loadUserByUsername("admin");

        // El nombre debe coincidir: el JWT se firma con él después
        assertThat(detalles.getUsername()).isEqualTo("admin");
        // El HASH (nunca la contraseña en claro) es lo que viaja
        assertThat(detalles.getPassword()).isEqualTo("$2a$10$hash");
        // Sin ROLE_ no habría permisos: el guard del frontend y las reglas
        // del backend dependen de esta authority concreta
        assertThat(detalles.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
        assertThat(detalles.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("Si el usuario no existe lanza UsernameNotFoundException")
    void loadUser_lanzaExcepcion_siNoExiste() {
        when(usuarioAdminRepository.findByUsername("nadie"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("nadie"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    @DisplayName("Un usuario DESACTIVADO no puede autenticarse")
    void loadUser_lanzaExcepcion_siInactivo() {
        when(usuarioAdminRepository.findByUsername("admin"))
                .thenReturn(Optional.of(usuario("admin", "$2a$10$hash", false)));

        // Si este test fallara, un admin dado de baja seguiría pudiendo
        // loguearse mientras su fila siga en la tabla: fallo de seguridad.
        assertThatThrownBy(() -> service.loadUserByUsername("admin"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("deshabilitado");
    }

    /** Fábrica de la entidad admin para los tests. */
    private UsuarioAdmin usuario(String username, String hash, boolean activo) {
        UsuarioAdmin u = new UsuarioAdmin();
        u.setUsername(username);
        u.setPassword(hash);
        u.setActivo(activo);
        return u;
    }
}
