import { Component, computed, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { PortfolioService } from '../../core/services/portfolio.service';
import { EstadoCarga } from '../../shared/estado-carga/estado-carga';
import { recurso } from '../../core/http/recurso';
import {
  Categoria,
  Competencia,
  Estudio,
  Experiencia,
} from '../../core/models/portfolio.models';

/**
 * PÁGINA: Trayectoria (estudios + experiencia) y Competencias.
 *
 * Mezcla las dos timelines (estudios y experiencias) en una sola lista
 * ordenada por fecha descendente — es lo que espera ver un recruiter:
 * "por dónde ha pasado esta persona" de un vistazo.
 *
 * <p>Fase 5: ahora con `recurso()` → cada sección sabe si está cargando,
 * ok o falló, y ofrece reintento.</p>
 */
@Component({
  selector: 'app-trayectoria',
  imports: [DatePipe, EstadoCarga],
  templateUrl: './trayectoria.html',
  styleUrl: './trayectoria.scss',
})
export class Trayectoria {
  private portfolioService = inject(PortfolioService);

  estudios = recurso<Estudio[]>(() => this.portfolioService.getEstudios(), []);
  experiencias = recurso<Experiencia[]>(() => this.portfolioService.getExperiencias(), []);
  competencias = recurso<Competencia[]>(() => this.portfolioService.getCompetencias(), []);

  /** ¿Está cargando ALGO de las dos fuentes de la timeline? */
  cargandoAlgo(): boolean {
    return (
      this.estudios.estado() === 'cargando' ||
      this.experiencias.estado() === 'cargando'
    );
  }

  /** ¿Falló alguna de las tres fuentes? */
  falloAlgo(): boolean {
    return (
      this.estudios.estado() === 'error' ||
      this.experiencias.estado() === 'error' ||
      this.competencias.estado() === 'error'
    );
  }

  /**
   * Mezcla estudios y experiencias en UNA sola lista tipada para la timeline.
   * Cada item lleva `tipo` para poder pintar un icono/color distinto.
   *
   * computed = derivado de otros signals: se recalcula SOLO cuando cambian
   * estudios() o experiencias() (y cachea el resultado).
   */
  lineaDeVida = computed(() => {
    const items = [
      ...this.estudios.datos().map((e) => ({
        id: `e-${e.id}`,
        tipo: 'estudio' as const,
        titulo: e.titulo,
        subtitulo: e.centro,
        descripcion: e.descripcion,
        inicio: e.fechaInicio,
        fin: e.enCurso ? null : e.fechaFin,
        enCurso: e.enCurso,
      })),
      ...this.experiencias.datos().map((x) => ({
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

    // Más reciente arriba: compara fechas ISO ("YYYY-MM-DD" ordena lexicográficamente)
    return items.sort((a, b) => b.inicio.localeCompare(a.inicio));
  });

  /** Agrupa competencias por categoría para pintar secciones. */
  porCategoria = computed(() => {
    const mapa = new Map<Categoria, Competencia[]>();
    for (const c of this.competencias.datos()) {
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
