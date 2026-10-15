import { Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { PortfolioService } from '../../../core/services/portfolio.service';

/**
 * PÁGINA: Dashboard del panel admin.
 *
 * <p>Muestra un resumen rápido (conteo de proyectos y destacados) y accesos
 * directos a las secciones de edición. Es la "portada" del panel: lo primero
 * que ves tras loguearte.</p>
 *
 * <p>Reutiliza los signals de `toSignal` que ya usamos en las páginas
 * públicas — misma técnica, distinta página.</p>
 */
@Component({
  selector: 'app-dashboard',
  imports: [RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard {

  private portfolioService = inject(PortfolioService);

  /** Todos los proyectos (para contar). */
  proyectos = toSignal(this.portfolioService.getProyectos(), {
    initialValue: [] as import('../../../core/models/portfolio.models').Proyecto[],
  });

  /** Derivado: cuántos están marcados como destacados. */
  destacados = () => this.proyectos().filter((p) => p.destacado).length;
}
