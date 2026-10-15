import { Component, input, output } from '@angular/core';

/**
 * COMPONENTE COMPARTIDO: estado de carga / error de una sección.
 *
 * <p>Se usa en todas las páginas públicas para no repetir el mismo
 * `@if` de skeleton/aviso por cada sitio que consume la API.</p>
 *
 * <pre>
 *   &lt;app-estado-carga
 *      [estado]="proyectos().estado"
 *      [tipo]="'tarjetas'"
 *      (reintentar)="recargar()"&gt;
 *   &lt;/app-estado-carga&gt;
 * </pre>
 *
 * <p>Inputs declarativos con `input()` (Angular 17.1+): son SIGNALS de
 * entrada — más ligeros que los @Input() clásicos y leen igual en el
 * template. El output `reintentar` permite re-lanzar la petición.</p>
 */
@Component({
  selector: 'app-estado-carga',
  templateUrl: './estado-carga.html',
  styleUrl: './estado-carga.scss',
})
export class EstadoCarga {

  /** Estado actual de la petición ('cargando' | 'ok' | 'error'). */
  estado = input.required<'cargando' | 'ok' | 'error'>();

  /**
   * Forma del skeleton a pintar mientras carga:
   * 'tarjetas' (grid de cards), 'texto' (líneas) o 'timeline' (lista).
   */
  tipo = input<'tarjetas' | 'texto' | 'timeline'>('tarjetas');

  /** Número de elementos fantasma a mostrar en el skeleton. */
  cantidad = input(3);

  /** Evento que emite el botón "Reintentar". */
  reintentar = output<void>();

  /** Array "vacío" para iterar con @for en el skeleton. */
  filas = () => Array.from({ length: this.cantidad() });

  /** Invocado por el botón de reintento. */
  reintentarCarga(): void {
    this.reintentar.emit();
  }
}
