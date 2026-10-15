import { Component, computed, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { toSignal } from '@angular/core/rxjs-interop';
import { PortfolioService } from '../../core/services/portfolio.service';
import { Categoria, Competencia } from '../../core/models/portfolio.models';

/**
 * PÁGINA: Trayectoria (estudios + experiencia) y Competencias.
 *
 * Mezcla las dos timelines (estudios y experiencias) en una sola lista
 * ordenada por fecha descendente — es lo que espera ver un recruiter:
 * "por dónde ha pasado esta persona" de un vistazo.
 */
@Component({
  selector: 'app-trayectoria',
  imports: [DatePipe],
  templateUrl: './trayectoria.html',
  styleUrl: './trayectoria.scss',
})
export class Trayectoria {
  private portfolioService = inject(PortfolioService);

  estudios = toSignal(this.portfolioService.getEstudios(), { initialValue: [] as import('../../core/models/portfolio.models').Estudio[] });
  experiencias = toSignal(this.portfolioService.getExperiencias(), { initialValue: [] as import('../../core/models/portfolio.models').Experiencia[] });
  competencias = toSignal(this.portfolioService.getCompetencias(), { initialValue: [] as Competencia[] });

  /**
   * Mezcla estudios y experiencias en UNA sola lista tipada para la timeline.
   * Cada item lleva `tipo` para poder pintar un icono/color distinto.
   *
   * computed = derivado de otros signals: se recalcula SOLO cuando cambian
   * estudios() o experiencias() (y cachea el resultado).
   */
  lineaDeVida = computed(() => {
    const items = [
      ...this.estudios().map((e) => ({
        id: `e-${e.id}`,
        tipo: 'estudio' as const,
        titulo: e.titulo,
        subtitulo: e.centro,
        descripcion: e.descripcion,
        inicio: e.fechaInicio,
        fin: e.enCurso ? null : e.fechaFin,
        enCurso: e.enCurso,
      })),
      ...this.experiencias().map((x) => ({
        id: `x-${x.id}`,
        tipo: 'experiencia' as const,
        titulo: x.puesto,
        subtitulo: x.empresa,
        descripcion: x.descripcion,
        inicio: x.fechaInicio,
        fin: x.enCurso ? null : x.fechaFin,
        enCurso: x.enCurso,
      })),
    ];

    // Más reciente arriba: compara fechas ISO ("YYYY-MM-MM" ordena lexicográficamente)
    return items.sort((a, b) => b.inicio.localeCompare(a.inicio));
  });

  /** Agrupa competencias por categoría para pintar secciones. */
  porCategoria = computed(() => {
    const mapa = new Map<Categoria, Competencia[]>();
    for (const c of this.competencias()) {
      const lista = mapa.get(c.categoria) ?? [];
      lista.push(c);
      mapa.set(c.categoria, lista);
    }
    // Orden fijo de categorías (más interesantes primero)
    const orden: Categoria[] = [
      'LENGUAJE', 'FRAMEWORK', 'BASE_DE_DATOS', 'HERRAMIENTA', 'BLANDA',
    ];
    return orden
      .filter((cat) => mapa.has(cat))
      .map((cat) => ({ categoria: cat, items: mapa.get(cat)! }));
  });

  /** Etiqueta legible de categoría para el template. */
  etiquetaCategoria(cat: Categoria): string {
    const etiquetas: Record<Categoria, string> = {
      LENGUAJE: 'Lenguajes',
      FRAMEWORK: 'Frameworks y librerías',
      BASE_DE_DATOS: 'Bases de datos',
      HERRAMIENTA: 'Herramientas',
      BLANDA: 'Competencias blandas',
    };
    return etiquetas[cat];
  }
}
