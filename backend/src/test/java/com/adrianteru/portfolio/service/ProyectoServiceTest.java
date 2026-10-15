package com.adrianteru.portfolio.service;

import com.adrianteru.portfolio.dto.ProyectoRequest;
import com.adrianteru.portfolio.dto.ProyectoResponse;
import com.adrianteru.portfolio.entity.Proyecto;
import com.adrianteru.portfolio.exception.ResourceNotFoundException;
import com.adrianteru.portfolio.repository.ProyectoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TESTS UNITARIOS de ProyectoService con MOCKITO (Fase 6).
 *
 * ============================================================================
 *  ¿QUÉ ES UN "MOCK"?
 * ============================================================================
 * Un mock es un DOBLE de actor: finge ser el {@code ProyectoRepository} para
 * que el service "crea" que habla con la base de datos real, cuando en
 * realidad no hay base de datos, ni red, ni disco. Solo verificamos la
 * LÓGICA del service (mapeos, decisiones, excepciones).
 *
 * <pre>
 *   @Mock      → crea el doble (el repositorio falso)
 *   @InjectMocks → construye ProyectoService y le INYECTA los mocks
 *   when(...).thenReturn(...) → "si preguntan esto, contesta esto otro"
 *   verify(...)                → "comprueba que llamaron a esto"
 * </pre>
 *
 * <p><b>¿Por qué testear el service y no el controller?</b> Ahí vive la
 * lógica real (convertir DTOs, decidir 404, copiar campos). El controller es
 * una cáscara HTTP que cubrimos con {@code @WebMvcTest} (ver
 * {@code ProyectoControllerTest}).</p>
 *
 * <p>Estos tests NO necesitan Spring ni PostgreSQL → corren en milisegundos.</p>
 */
@ExtendWith(MockitoExtension.class)
class ProyectoServiceTest {

    /** El doble del repositorio: nunca toca la BD. */
    @Mock
    private ProyectoRepository proyectoRepository;

    /** La clase bajo prueba, con el mock inyectado en su constructor. */
    @InjectMocks
    private ProyectoService proyectoService;

    // ---------------------------------------------------------------------
    // LECTURAS
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("listarTodos convierte cada entidad en su DTO de salida")
    void listarTodos_convierteEntidadesADtos() {
        // La BD devuelve entidades... el service debe devolver DTOs:
        // es el "patrón anti-fugas" (los controllers jamás ven entidades).
        when(proyectoRepository.findAllByOrderByDestacadoDescFechaDesc())
                .thenReturn(List.of(entidad("Alpha", 1L), entidad("Beta", 2L)));

        List<ProyectoResponse> resultado = proyectoService.listarTodos();

        assertThat(resultado)
                .hasSize(2)
                .extracting(ProyectoResponse::nombre)
                .containsExactly("Alpha", "Beta");   // respeta el orden del repo
        assertThat(resultado.get(0).id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("obtenerPorId lanza ResourceNotFoundException (→ 404) si no existe")
    void obtenerPorId_lanzaExcepcionCuandoNoExiste() {
        when(proyectoRepository.findById(99L)).thenReturn(Optional.empty());

        // El service NO devuelve null ni Optional al controller: LANZA, y el
        // GlobalExceptionHandler lo traduce a 404. Si esto fallara, la API
        // devolvería un 200 con body vacío (bug clásico).
        assertThatThrownBy(() -> proyectoService.obtenerPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("obtenerPorId devuelve el DTO cuando el proyecto existe")
    void obtenerPorId_devuelveDtoCuandoExiste() {
        when(proyectoRepository.findById(7L)).thenReturn(Optional.of(entidad("Portfolio", 7L)));

        ProyectoResponse respuesta = proyectoService.obtenerPorId(7L);

        assertThat(respuesta.id()).isEqualTo(7L);
        assertThat(respuesta.nombre()).isEqualTo("Portfolio");
    }

    // ---------------------------------------------------------------------
    // ESCRITURAS
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("crear copia TODOS los campos del DTO a la entidad")
    void crear_copiaTodosLosCamposDelDto() {
        // El argumentCaptor "atrapa" la entidad que llegó al repositorio para
        // poder inspeccionarla. Sin él, solo sabríamos que save() se llamó.
        ArgumentCaptor<Proyecto> captor = ArgumentCaptor.forClass(Proyecto.class);
        when(proyectoRepository.save(any(Proyecto.class)))
                .thenAnswer(invocation -> {
                    Proyecto p = invocation.getArgument(0);
                    p.setId(7L);   // simula el id que asignaría la BD
                    return p;
                });

        LocalDate fecha = LocalDate.of(2026, 3, 15);
        ProyectoRequest dto = new ProyectoRequest(
                "Portfolio", "Sitio personal", "Java, Angular",
                "https://github.com/x", "https://demo.x", "img.png",
                true, fecha);

        ProyectoResponse respuesta = proyectoService.crear(dto);

        verify(proyectoRepository).save(captor.capture());
        Proyecto guardado = captor.getValue();

        // Regresión de mapeo: si alguien añade un campo a la entidad y se
        // olvida de copiarlo aquí, ESTE test se pone en rojo.
        assertThat(guardado.getNombre()).isEqualTo("Portfolio");
        assertThat(guardado.getDescripcion()).isEqualTo("Sitio personal");
        assertThat(guardado.getTecnologias()).isEqualTo("Java, Angular");
        assertThat(guardado.getUrlRepo()).isEqualTo("https://github.com/x");
        assertThat(guardado.getUrlDemo()).isEqualTo("https://demo.x");
        assertThat(guardado.getImagenUrl()).isEqualTo("img.png");
        assertThat(guardado.isDestacado()).isTrue();
        assertThat(guardado.getFecha()).isEqualTo(fecha);

        assertThat(respuesta.id()).isEqualTo(7L);
        assertThat(respuesta.destacado()).isTrue();
    }

    @Test
    @DisplayName("eliminar lanza 404 y NO borra nada si el proyecto no existe")
    void eliminar_noBorra_siNoExiste() {
        when(proyectoRepository.existsById(5L)).thenReturn(false);

        assertThatThrownBy(() -> proyectoService.eliminar(5L))
                .isInstanceOf(ResourceNotFoundException.class);

        // La aserción más valiosa del fichero: nunca borrar a ciegas.
        // Un DELETE silencioso sobre un id inexistente devolvería 204 y el
        // cliente creería que había borrado algo real.
        verify(proyectoRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("eliminar sí borra cuando el proyecto existe")
    void eliminar_borraCuandoExiste() {
        when(proyectoRepository.existsById(5L)).thenReturn(true);

        proyectoService.eliminar(5L);

        verify(proyectoRepository).deleteById(5L);
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    /** Fábrica de entidades para los tests de lectura. */
    private Proyecto entidad(String nombre, Long id) {
        Proyecto p = new Proyecto();
        p.setId(id);
        p.setNombre(nombre);
        return p;
    }
}
