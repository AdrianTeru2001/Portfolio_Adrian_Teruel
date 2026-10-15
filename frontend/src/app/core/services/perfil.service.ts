import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api-url.token';
import { Perfil } from '../models/portfolio.models';

/**
 * SERVICIO: datos del perfil.
 *
 * <p>Patrón de Angular: un "service" por dominio que devuelve OBSERVABLES.
 * El componente decide cuándo suscribirse (idealmente con {@code toSignal}).</p>
 *
 * <p>{@code providedIn: 'root'} = singleton global: Angular crea UNA sola
 * instancia y la inyecta donde haga falta. Sin esto, habría que declararlo
 * en providers del módulo/config → más verboso y con riesgo de duplicados.</p>
 */
@Injectable({ providedIn: 'root' })
export class PerfilService {

  // inject() en campo = forma moderna (Angular 16+); equivalente al constructor.
  private http = inject(HttpClient);
  private api = inject(API_URL);

  /** GET /api/perfil → Observable<Perfil> (lazy: no llama hasta suscribirse) */
  getPerfil(): Observable<Perfil> {
    return this.http.get<Perfil>(`${this.api}/perfil`);
  }
}
