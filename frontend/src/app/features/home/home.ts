import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PerfilService } from '../../core/services/perfil.service';
import { PortfolioService } from '../../core/services/portfolio.service';
import { EstadoCarga } from '../../shared/estado-carga/estado-carga';
import { recurso } from '../../core/http/recurso';
import { Perfil, Proyecto } from '../../core/models/portfolio.models';

/**
 * PÁGINA: Home (portada).
 *
 * <p>Carga en paralelo dos fuentes de datos con el patrón `recurso()` (Fase 5):
 * en vez de `toSignal`, cada petición expone un ESTADO
 * (cargando / ok / error) + `datos` + `recargar()` → permite pintar skeletons
 * y avisos de reintento en vez de quedarse en un "Cargando…" eterno si la API
 * falla.</p>
 *
 * <p>Cada `recurso()` es una propiedad que se crea UNA vez en la clase.
 * `recargar()` (el botón de reintento) hace `.set()` interno, así que
 * Angular re-renderiza sin necesidad de reasignar nada aquí.</p>
 */
@Component({
  selector: 'app-home',
  imports: [RouterLink, EstadoCarga],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class Home {
  private perfilService = inject(PerfilService);
  private portfolioService = inject(PortfolioService);

  // Recursos con estado (cargando/ok/error) en vez de signals "ciegos"
  perfil = recurso<Perfil>(() => this.perfilService.getPerfil());
  destacados = recurso<Proyecto[]>(() => this.portfolioService.getDestacados(), []);
}
