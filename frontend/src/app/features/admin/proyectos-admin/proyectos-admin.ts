import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PortfolioService } from '../../../core/services/portfolio.service';
import { AdminService } from '../../../core/services/admin.service';
import {
  Proyecto,
  ProyectoRequest,
} from '../../../core/models/portfolio.models';

/**
 * PÁGINA: Gestión de proyectos (CRUD completo).
 *
 * <p>Es la página más "completa" del panel: combina LISTA + FORMULARIO en
 * una sola vista (patrón "master-detail" sencillo).</p>
 *
 * <p><b>Estados que gestionamos (signals):</b></p>
 * <pre>
 *   proyectos      → la lista viva (se actualiza tras cada operación)
 *   modoForm       → 'crear' | 'editar' | 'oculto'  (¿se ve el form?)
 *   proyectoEditando→ id del proyecto que se edita (null al crear)
 *   borrando       → id en proceso de borrado (para deshabilitar botón)
 *   mensaje        → feedback éxito/error al usuario
 * </pre>
 *
 * <p>En vez de recargar la lista tras cada operación, MUTAMOS el signal
 * `proyectos` localmente (añadimos/modificamos/quitamos el item). Es más
 * rápido (sin viaje a la API) y demuestra el poder de los signals.</p>
 */
@Component({
  selector: 'app-proyectos-admin',
  imports: [FormsModule],
  templateUrl: './proyectos-admin.html',
  styleUrl: './proyectos-admin.scss',
})
export class ProyectosAdmin {

  private portfolioService = inject(PortfolioService);
  private adminService = inject(AdminService);

  // Lista VIVA de proyectos. Empezamos vacía y la llenamos al suscribirnos
  // a la API; después la mutamos localmente con .set()/.update() tras cada
  // operación (crear/editar/borrar) SIN necesidad de recargar la página.
  proyectos = signal<Proyecto[]>([]);

  constructor() {
    this.portfolioService.getProyectos().subscribe({
      next: (lista) => this.proyectos.set(lista),
    });
  }

  // ¿Se muestra el formulario y en qué modo?
  modoForm = signal<'oculto' | 'crear' | 'editar'>('oculto');

  // Modelo del formulario (siempre un ProyectoRequest "en blanco")
  form = signal<ProyectoRequest>(ProyectosAdmin.formVacio());

  // Id del proyecto que se está editando (null = modo crear)
  idEditando = signal<number | null>(null);

  // Feedback para el usuario (éxito / error)
  mensaje = signal<{ tipo: 'ok' | 'mal'; texto: string } | null>(null);

  // ==========================================================================
  //  FORMULARIO
  // ==========================================================================

  /** Abre el formulario en modo CREAR (con los campos por defecto). */
  abrirCrear(): void {
    this.form.set(ProyectosAdmin.formVacio());
    this.idEditando.set(null);
    this.modoForm.set('crear');
  }

  /** Abre el formulario en modo EDITAR con los datos del proyecto dado. */
  abrirEditar(proyecto: Proyecto): void {
    // Convertimos el Proyecto (respuesta) a ProyectoRequest (entrada):
    // mismo shape, pero sin id (el id va en la URL).
    this.form.set({
      nombre: proyecto.nombre,
      descripcion: proyecto.descripcion,
      tecnologias: proyecto.tecnologias,
      urlRepo: proyecto.urlRepo,
      urlDemo: proyecto.urlDemo,
      imagenUrl: proyecto.imagenUrl,
      destacado: proyecto.destacado,
      fecha: proyecto.fecha,
    });
    this.idEditando.set(proyecto.id);
    this.modoForm.set('editar');
  }

  /** Cierra el formulario (cancelar). */
  cerrar(): void {
    this.modoForm.set('oculto');
  }

  /** ¿Está el formulario visible? (para el @if del template) */
  formVisible(): boolean {
    return this.modoForm() !== 'oculto';
  }

  /**
   * Envía el formulario: CREAR o EDITAR según el modo.
   *
   * <p>Tras el éxito, mutamos la lista local en vez de recargarla:
   * añadimos el proyecto nuevo, o reemplazamos el editado por id.</p>
   */
  guardar(): void {
    const datos = this.form();
    const modo = this.modoForm();
    const id = this.idEditando();

    if (modo === 'crear') {
      this.adminService.crearProyecto(datos).subscribe({
        next: (creado) => {
          // Añadimos el nuevo proyecto al FINAL de la lista local
          this.proyectos.update((lista) => [...lista, creado]);
          this.mostrarMensaje('ok', `Proyecto "${creado.nombre}" creado.`);
          this.cerrar();
        },
        error: (err) => this.manejarError(err, 'crear'),
      });
    } else if (modo === 'editar' && id !== null) {
      this.adminService.actualizarProyecto(id, datos).subscribe({
        next: (actualizado) => {
          // Reemplazamos el item editado por su versión nueva (map por id)
          this.proyectos.update((lista) =>
            lista.map((p) => (p.id === id ? actualizado : p)));
          this.mostrarMensaje('ok', `Proyecto "${actualizado.nombre}" actualizado.`);
          this.cerrar();
        },
        error: (err) => this.manejarError(err, 'actualizar'),
      });
    }
  }

  // ==========================================================================
  //  BORRADO
  // ==========================================================================

  /**
   * Borra un proyecto tras confirmar.
   *
   * <p>`confirm()` es el diálogo nativo del navegador. Es simple y funciona,
   * aunque no es personalizable; en un proyecto mayor se usaría un modal
   * propio. Aquí cumple su función: evitar borrados accidentales.</p>
   */
  eliminar(proyecto: Proyecto): void {
    const seguro = confirm(
      `¿Seguro que quieres borrar "${proyecto.nombre}"?\n\nEsta acción no se puede deshacer.`,
    );
    if (!seguro) {
      return;   // el usuario canceló
    }

    this.adminService.eliminarProyecto(proyecto.id).subscribe({
      next: () => {
        // Quitamos el item de la lista local (filter deja fuera el id)
        this.proyectos.update((lista) =>
          lista.filter((p) => p.id !== proyecto.id));
        this.mostrarMensaje('ok', `Proyecto "${proyecto.nombre}" eliminado.`);
      },
      error: (err) => this.manejarError(err, 'eliminar'),
    });
  }

  // ==========================================================================
  //  UTILIDADES
  // ==========================================================================

  /** Actualiza un campo del formulario (llamado desde el template). */
  cambiarCampo(campo: keyof ProyectoRequest, valor: unknown): void {
    this.form.update((f) => ({ ...f, [campo]: valor }));
  }

  /** Modelo por defecto del formulario (proyecto "en blanco"). */
  private static formVacio(): ProyectoRequest {
    return {
      nombre: '',
      descripcion: null,
      tecnologias: null,
      urlRepo: null,
      urlDemo: null,
      imagenUrl: null,
      destacado: false,
      fecha: null,
    };
  }

  private mostrarMensaje(tipo: 'ok' | 'mal', texto: string): void {
    this.mensaje.set({ tipo, texto });
    // Ocultamos el mensaje a los 4 s (setTimeout + signal)
    setTimeout(() => this.mensaje.set(null), 4000);
  }

  /** Traduce errores del backend a un mensaje entendible. */
  private manejarError(err: unknown, accion: string): void {
    console.error(`Error al ${accion}`, err);
    let texto = `No se pudo ${accion}. Inténtalo de nuevo.`;
    // Si el backend devolvió validación (400), mostramos el detalle
    if (
      typeof err === 'object' && err !== null && 'error' in err &&
      typeof (err as { error: unknown }).error === 'object'
    ) {
      const detalle = (err as { error: { message?: string } }).error?.message;
      if (detalle) {
        texto = `Revisa el formulario: ${detalle}`;
      }
    }
    this.mostrarMensaje('mal', texto);
  }
}
