import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PerfilService } from '../../core/services/perfil.service';
import { recurso } from '../../core/http/recurso';
import { Perfil } from '../../core/models/portfolio.models';

/**
 * COMPONENTE: pie de página.
 *
 * <p>Demuestra la forma moderna de consumir un Observable en un componente
 * con el patrón `recurso()` (Fase 5):</p>
 * <pre>
 *   perfil = recurso&lt;Perfil&gt;(() =&gt; this.perfilService.getPerfil())
 * </pre>
 * <p>{@code recurso} convierte el Observable en un objeto con SEÑALES:
 * `perfil.estado()` (cargando/ok/error) y `perfil.datos()` (el perfil o
 * null). El template se actualiza solo cuando llegan los datos, sin
 * .subscribe() manual ni estrategia *ngIf + async.</p>
 *
 * <p>Usamos `recurso` en vez de `toSignal` porque el footer se ve en TODAS
 * las rutas: si la API fallara, `toSignal` propagaría la excepción y
 * rompería la página entera; `recurso` la captura y deja el footer en
 * modo "degradado" (sin enlaces) sin tumbar nada.</p>
 */
@Component({
  selector: 'app-footer',
  imports: [RouterLink],
  templateUrl: './footer.html',
  styleUrl: './footer.scss',
})
export class Footer {
  private perfilService = inject(PerfilService);

  // Recurso con estado: null mientras carga, el perfil cuando responda
  perfil = recurso<Perfil>(() => this.perfilService.getPerfil());

  // new Date().getFullYear() en el template (año de copyright)
  anio = signal(new Date().getFullYear());
}
