package com.adrianteru.portfolio.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TESTS UNITARIOS del JwtService (Fase 6).
 *
 * <p><b>¿Por qué este test es el más importante de la seguridad?</b> Porque
 * protege los 3 fallos que convierten un JWT en papel mojado:</p>
 * <ol>
 *   <li><b>Round-trip</b>: lo que firmas debe poder leerse con la misma clave.</li>
 *   <li><b>Token firmado con OTRA clave</b>: si un atacante crea su propio
 *       token con su secreto, NUESTRO servicio debe rechazarlo.</li>
 *   <li><b>Token caducado</b>: sin esta comprobación, un token filtrado
 *       serviría para siempre.</li>
 * </ol>
 *
 * <p>Es un test <b>unitario puro</b>: no arranca Spring, no toca la base de
 * datos, no abre puertos. Por eso corre en milisegundos.</p>
 */
class JwtServiceTest {

    /** Secreto de prueba con la longitud mínima válida (>= 32 caracteres). */
    private static final String SECRETO = "secreto-de-prueba-suficientemente-largo-1234567890";
    private static final String OTRO_SECRETO = "otro-secreto-completamente-distinto-0987654321";
    private static final long UNA_HORA_MS = 3_600_000L;

    private JwtService servicio() {
        return new JwtService(SECRETO, UNA_HORA_MS);
    }

    @Test
    @DisplayName("Generar un token y extraer el username devuelve el mismo usuario")
    void generarYExtraer_hacenRoundTrip() {
        String token = servicio().generarToken("admin");

        // El token debe tener la estructura JWT: 3 partes separadas por puntos
        assertThat(token.split("\\.")).hasSize(3);
        assertThat(servicio().extraerUsername(token)).isEqualTo("admin");
    }

    @Test
    @DisplayName("Un token recién generado es válido")
    void esValido_devuelveTrue_paraTokenRecienGenerado() {
        String token = servicio().generarToken("admin");

        assertThat(servicio().esValido(token)).isTrue();
    }

    @Test
    @DisplayName("Un texto que no es un JWT se rechaza (no lanza excepción)")
    void esValido_devuelveFalse_paraTokenBasura() {
        // esValido debe devolver false en vez de lanzar: el filtro HTTP
        // prefiere "no autenticar" a romper la petición con una excepción.
        assertThat(servicio().esValido("esto-no-es-un-jwt")).isFalse();
        assertThat(servicio().esValido("")).isFalse();
        assertThat(servicio().esValido(null)).isFalse();
    }

    @Test
    @DisplayName("Un token firmado con OTRO secreto se rechaza (firma inválida)")
    void esValido_devuelveFalse_paraTokenDeOtroSecreto() {
        // Simula a un atacante: genera un token válidos SÍ, pero con SU clave.
        JwtService atacante = new JwtService(OTRO_SECRETO, UNA_HORA_MS);
        String tokenFalso = atacante.generarToken("admin");

        // Nuestro servicio usa otro secreto → la firma no cuadra → false
        assertThat(servicio().esValido(tokenFalso)).isFalse();
    }

    @Test
    @DisplayName("Un token caducado se rechaza aunque su firma sea correcta")
    void esValido_devuelveFalse_paraTokenCaducado() {
        // Un JwtService con expiración NEGativa → el token "nace" caducado,
        // pero firmado con el MISMO secreto (la firma sí cuadra: solo debe
        // caer la comprobación de "exp", que es el punto del test).
        JwtService caducado = new JwtService(SECRETO, -60_000L);
        String tokenVencido = caducado.generarToken("admin");

        assertThat(servicio().esValido(tokenVencido)).isFalse();
    }

    @Test
    @DisplayName("Un secreto de menos de 32 caracteres revienta al arrancar")
    void constructor_rechazaSecretoDemasiadoCorto() {
        // Fallar AL ARRANCAR es mejor que arrancar y firmar tokens débiles:
        // HS256 exige >= 256 bits (32 bytes) para ser seguro.
        assertThatThrownBy(() -> new JwtService("corto", UNA_HORA_MS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("32");
    }
}
