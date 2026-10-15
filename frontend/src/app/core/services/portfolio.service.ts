import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api-url.token';
import { Competencia, Estudio, Experiencia, Proyecto } from '../models/portfolio.models';

/**
 * SERVICIO de LECTURA del portfolio: proyectos, trayectoria y competencias.
 *
 * <p>Un único servicio para las consultas públicas porque todas consumen la
 * misma API y no tienen lógica propia: el service solo "traduce" endpoints a
 * métodos tipados. Si creciera, se dividiría (ProyectoService, etc.).</p>
 *
 * <p><b>Observable vs Promise:</b> Angular usa Observables (RxJS) porque
 * permiten cancelar peticiones, encadenarlas y combinarlas. Un GET simple
 * se consume igual que un Promise, pero el ecosistema es más potente.</p>
 */
@Injectable({ providedIn: 'root' })
export class PortfolioService {

  private http = inject(HttpClient);
  private api = inject(API_URL);

  // ------------------------------------------------------------------
  // PROYECTOS
  // ------------------------------------------------------------------

  /** GET /api/proyectos — el backend ya ordena: destacados primero */
  getProyectos(): Observable<Proyecto[]> {
    return this.http.get<Proyecto[]>(`${this.api}/proyectos`);
  }

  /** GET /api/proyectos/destacados — para la home */
  getDestacados(): Observable<Proyecto[]> {
    return this.http.get<Proyecto[]>(`${this.api}/proyectos/destacados`);
  }

  /**
   * GET /api/proyectos?tecnologia=x
   * {@code encodeURIComponent} codifica caracteres especiales (espacios,
   * acentos...) para que la query string sea válida. Sin él, buscar
   * "c++" rompería la URL.
   */
  buscarPorTecnologia(texto: string): Observable<Proyecto[]> {
    return this.http.get<Proyecto[]>(
      `${this.api}/proyectos?tecnologia=${encodeURIComponent(texto)}`);
  }

  // ------------------------------------------------------------------
  // TRAYECTORIA
  // ------------------------------------------------------------------

  getEstudios(): Observable<Estudio[]> {
    return this.http.get<Estudio[]>(`${this.api}/estudios`);
  }

  getExperiencias(): Observable<Experiencia[]> {
    return this.http.get<Experiencia[]>(`${this.api}/experiencias`);
  }

  // ------------------------------------------------------------------
  // COMPETENCIAS
  // ------------------------------------------------------------------

  getCompetencias(): Observable<Competencia[]> {
    return this.http.get<Competencia[]>(`${this.api}/competencias`);
  }
}
