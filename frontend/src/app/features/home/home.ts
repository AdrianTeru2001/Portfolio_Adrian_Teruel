import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { PerfilService } from '../../core/services/perfil.service';
import { PortfolioService } from '../../core/services/portfolio.service';

/**
 * PÁGINA: Home (portada).
 *
 * Carga en paralelo dos fuentes de datos:
 *   - el perfil (hero: nombre, título, bio)
 *   - los proyectos destacados (cards resumen)
 *
 * Ambos Observables se convierten a signals con toSignal → el template
 * reacciona automáticamente (patrón "signal-based" de Angular 17+).
 */
@Component({
  selector: 'app-home',
  imports: [RouterLink],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class Home {
  private perfilService = inject(PerfilService);
  private portfolioService = inject(PortfolioService);

  // initialValue: {} / [] → evita undefined mientras carga (evita parpadeos)
  perfil = toSignal(this.perfilService.getPerfil(), { initialValue: null });
  destacados = toSignal(this.portfolioService.getDestacados(), { initialValue: [] as import('../../core/models/portfolio.models').Proyecto[] });
}
