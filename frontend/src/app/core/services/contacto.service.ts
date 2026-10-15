import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api-url.token';
import { MensajeContactoRequest } from '../models/portfolio.models';

/**
 * SERVICIO: envío del formulario de contacto.
 *
 * <p>POST con body JSON: HttpClient serializa el objeto a JSON y pone la
 * cabecera Content-Type automáticamente (si envías un objeto, no un string).</p>
 */
@Injectable({ providedIn: 'root' })
export class ContactoService {

  private http = inject(HttpClient);
  private api = inject(API_URL);

  /**
   * POST /api/contacto → devuelve Observable<Object> con el mensaje guardado
   * (el backend responde 201 + el objeto con id asignado).
   *
   * Si el backend rechaza la validación (400), el error viaja por el canal
   * de ERROR del observable (lo captura el .subscribe({error}) del componente).
   */
  enviar(mensaje: MensajeContactoRequest): Observable<{ id: number }> {
    return this.http.post<{ id: number }>(`${this.api}/contacto`, mensaje);
  }
}
