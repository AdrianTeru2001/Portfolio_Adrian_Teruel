import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PerfilService } from '../../core/services/perfil.service';
import { toSignal } from '@angular/core/rxjs-interop';

/**
 * COMPONENTE: pie de página.
 *
 * <p>Demuestra la forma moderna de consumir un Observable en un componente:</p>
 * <pre>
 *   perfil = toSignal(this.perfilService.getPerfil(), { initialValue: null })
 * </pre>
 * <p>{@code toSignal} convierte el Observable en un SIGNAL reactivo: el template
 * se actualiza solo cuando llegan los datos, sin .subscribe() manual ni
 * estrategia *ngIf + async. initialValue evita el "undefined" inicial.</p>
 */
@Component({
  selector: 'app-footer',
  imports: [RouterLink],
  templateUrl: './footer.html',
  styleUrl: './footer.scss',
})
export class Footer {
  private perfilService = inject(PerfilService);

  // Signal<Perfil | null>: null mientras carga, el perfil cuando responda
  perfil = toSignal(this.perfilService.getPerfil(), { initialValue: null });

  // new Date().getFullYear() en el template (año de copyright)
  anio = signal(new Date().getFullYear());
}
