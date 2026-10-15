import { Component, inject, signal, computed } from '@angular/core';
import { DatePipe } from '@angular/common';
import { PortfolioService } from '../../core/services/portfolio.service';
import { EstadoCarga } from '../../shared/estado-carga/estado-carga';
import { recurso } from '../../core/http/recurso';
import { Proyecto } from '../../core/models/portfolio.models';

/**
 * PÁGINA: Proyectos (listado completo + filtro).
 *
 * Patrón "lista + búsqueda": el filtro es un signal y el listado visible es
 * un {@code computed} que se recalcula automáticamente cuando cambia el
 * filtro. NO se hace una petición por tecla (filtrado 100% en cliente):
 * el portfolio tiene pocos proyectos, así va instantáneo y sin saturar la API.
 * Si hubiera miles, usaríamos debounce + llamada a ?tecnologia= del backend.
 *
 * <p>Fase 5: la lista usa `recurso()` → skeleton mientras carga y aviso +
 * "Reintentar" si la API falla (en vez de quedarse vacío sin explicación).</p>
 */
@Component({
  selector: 'app-proyectos',
  imports: [DatePipe, EstadoCarga],
  templateUrl: './proyectos.html',
  styleUrl: './proyectos.scss',
})
export class Proyectos {
  private portfolioService = inject(PortfolioService);
  // Datos completos (una sola petición) con estado cargando/ok/error
  todos = recurso<Proyecto[]>(() => this.portfolioService.getProyectos(), []);

  /** Texto del input de búsqueda (signal enlazado con [(ngModel)] o (input)) */
  filtro = signal('');

  /** Proyectos tras aplicar el filtro (derivado → computed) */
  visibles = computed(() => {
    const todos = this.todos.datos();
    const texto = this.filtro().trim().toLowerCase();
    if (!texto) {
      return todos;
    }
    // Busca el texto en nombre, descripción Y tecnologías
    return todos.filter((p) =>
      [p.nombre, p.descripcion ?? '', p.tecnologias ?? '']
        .some((campo) => campo.toLowerCase().includes(texto)),
    );
  });

  /** Extrae la lista única de tecnologías (para chips de filtro rápido). */
  tecnologias = computed(() => {
    const set = new Set<string>();
    for (const p of this.todos.datos()) {
      if (p.tecnologias) {
        for (const t of p.tecnologias.split(',')) {
          const limpia = t.trim();
          if (limpia) {
            set.add(limpia);
          }
        }
      }
    }
    return [...set].sort((a, b) => a.localeCompare(b));
  });

  /** Activa un chip de tecnología como filtro. */
  filtrarPorTech(tech: string): void {
    // Si ya está activo, lo desactiva (toggle)
    this.filtro.set(this.filtro() === tech ? '' : tech);
  }
}
