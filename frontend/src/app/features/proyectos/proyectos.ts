import { Component, inject, signal, computed } from '@angular/core';
import { DatePipe } from '@angular/common';
import { toSignal } from '@angular/core/rxjs-interop';
import { PortfolioService } from '../../core/services/portfolio.service';
import { Proyecto } from '../../core/models/portfolio.models';

/**
 * PÁGINA: Proyectos (listado completo + filtro).
 *
 * Patrón "lista + búsqueda": el filtro es un signal y el listado visible es
 * un {@code computed} que se recalcula automáticamente cuando cambia el
 * filtro. NO se hace una petición por tecla (filtrado 100% en cliente):
 * el portfolio tiene pocos proyectos, así va instantáneo y sin saturar la API.
 * Si hubiera miles, usaríamos debounce + llamada a ?tecnologia= del backend.
 */
@Component({
  selector: 'app-proyectos',
  imports: [DatePipe],
  templateUrl: './proyectos.html',
  styleUrl: './proyectos.scss',
})
export class Proyectos {
  private portfolioService = inject(PortfolioService);
  // Datos completos (una sola petición)
  todos = toSignal(this.portfolioService.getProyectos(), {
    initialValue: [] as Proyecto[],
  });

  /** Texto del input de búsqueda (signal enlazado con [(ngModel)] o (input)) */
  filtro = signal('');

  /** Proyectos tras aplicar el filtro (derivado → computed) */
  visibles = computed(() => {
    const texto = this.filtro().trim().toLowerCase();
    if (!texto) {
      return this.todos();
    }
    // Busca el texto en nombre, descripción Y tecnologías
    return this.todos().filter((p) =>
      [p.nombre, p.descripcion ?? '', p.tecnologias ?? '']
        .some((campo) => campo.toLowerCase().includes(texto)),
    );
  });

  /** Extrae la lista única de tecnologías (para chips de filtro rápido). */
  tecnologias = computed(() => {
    const set = new Set<string>();
    for (const p of this.todos()) {
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
