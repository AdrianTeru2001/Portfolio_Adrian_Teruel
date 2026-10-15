import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api-url.token';
import {
  Perfil,
  PerfilUpdateRequest,
  Proyecto,
  ProyectoRequest,
} from '../models/portfolio.models';

/**
 * ============================================================================
 *  SERVICIO ADMIN (Fase 4) — escrituras protegidas
 * ============================================================================
 * Agrupa las operaciones de ESCRITURA (crear/editar/borrar) que el backend
 * protege con JWT. La cabecera `Authorization` NO se pone aquí: la añade el
 * `authInterceptor` a TODAS las peticiones. Así este servicio se concentra
 * en los endpoints, igual que los services públicos.
 *
 * <p>Endpoints que consume (verificados contra los controllers Java):</p>
 * <pre>
 *   POST   /api/proyectos      → crear           (201 + Location)
 *   PUT    /api/proyectos/{id} → editar          (200)
 *   DELETE /api/proyectos/{id} → borrar          (204 sin contenido)
 *   PUT    /api/perfil         → editar perfil   (200)
 * </pre>
 */
@Injectable({ providedIn: 'root' })
export class AdminService {

  private http = inject(HttpClient);
  private api = inject(API_URL);

  // ==========================================================================
  //  PROYECTOS
  // ==========================================================================

  /**
   * POST /api/proyectos → crea.
   * Devolvemos `ProyectoResponse` (con el id asignado) para que la lista
   * del panel pueda añadirlo sin recargar todo.
   */
  crearProyecto(datos: ProyectoRequest): Observable<Proyecto> {
    return this.http.post<Proyecto>(`${this.api}/proyectos`, datos);
  }

  /**
   * PUT /api/proyectos/{id} → reemplaza el recurso completo.
   * Nota: el id va en la URL, nunca en el body (convención REST).
   */
  actualizarProyecto(id: number, datos: ProyectoRequest): Observable<Proyecto> {
    return this.http.put<Proyecto>(`${this.api}/proyectos/${id}`, datos);
  }

  /**
   * DELETE /api/proyectos/{id} → borra.
   * El backend responde 204 (éxito sin body) → usamos `void` en el tipo.
   */
  eliminarProyecto(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/proyectos/${id}`);
  }

  // ==========================================================================
  //  PERFIL
  // ==========================================================================

  /** PUT /api/perfil → actualiza todos los campos del perfil. */
  actualizarPerfil(datos: PerfilUpdateRequest): Observable<Perfil> {
    return this.http.put<Perfil>(`${this.api}/perfil`, datos);
  }
}
