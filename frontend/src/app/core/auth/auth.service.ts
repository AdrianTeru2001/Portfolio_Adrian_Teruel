import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { API_URL } from '../api-url.token';
import { AuthResponse, LoginRequest } from '../models/portfolio.models';

/**
 * ============================================================================
 *  SERVICIO DE AUTENTICACIÓN (Fase 4)
 * ============================================================================
 * Responsabilidades:
 *   1. login()    → POST /api/auth/login y guardar el JWT
 *   2. logout()   → borrar el JWT y volver al login
 *   3. Exponer el estado de sesión como SIGNAL reactivamente
 *   4. Decodificar el JWT para saber cuándo caduca
 *
 * <p><b>DÓNDE SE GUARDA EL TOKEN</b> usamos `localStorage`:</p>
 * <table>
 *   <tr><th>Almacén</th><th>Pros</th><th>Contras</th></tr>
 *   <tr><td>localStorage</td><td>Persiste al recargar; simple</td>
 *       <td>Accesible por JS (riesgo XSS)</td></tr>
 *   <tr><td>sessionStorage</td><td>Se borra al cerrar pestaña</td>
 *       <td>Molesto si recargas a menudo</td></tr>
 *   <tr><td>cookie httpOnly</td><td>El más seguro (JS no lo lee)</td>
 *       <td>Requiere configurar SameSite/CORS con credentials</td></tr>
 * </table>
 *
 * <p>Para un portfolio personal el riesgo XSS es bajo y `localStorage` es
 * aceptable y el más didáctico. Si fuera una banca, irías con cookies httpOnly.
 * Ese trade-off es una pregunta clásica de entrevista: queda anotado.</p>
 */
@Injectable({ providedIn: 'root' })
export class AuthService {

  private http = inject(HttpClient);
  private api = inject(API_URL);
  private router = inject(Router);

  /** Clave donde persistimos el token en localStorage (prefijo = namespace). */
  private readonly CLAVE = 'portfolio_token';

  /**
   * SIGNAL con el token actual (null = no autenticado).
   * Es la "fuente única de verdad": todo lo demás se deriva de él con
   * `computed`. Si alguien cambia este signal, las vistas se actualizan solas.
   */
  private tokenState = signal<string | null>(this.cargarTokenInicial());

  /**
   * ¿Hay sesión activa? (derivado del token)
   * `computed` recalcula SOLO cuando cambia `tokenState`.
   */
  autenticado = computed(() => this.tokenState() !== null);

  /** Token actual (lo consume el interceptor para la cabecera Authorization). */
  token = this.tokenState.asReadonly();

  // ==========================================================================
  //  LOGIN / LOGOUT
  // ==========================================================================

  /**
   * POST /api/auth/login.
   *
   * <p>Flujo: el backend valida credenciales (BCrypt) → si son correctas,
   * devuelve un JWT. Nosotros lo guardamos en `tap` (side-effect sin romper
   * el Observable) y redirigimos al panel.</p>
   *
   * <p>Si las credenciales son malas, el backend responde 401 y el error viaja
   * por el canal de ERROR del Observable → el componente de login muestra el
   * aviso sin necesidad de try/catch aquí.</p>
   */
  login(credenciales: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.api}/auth/login`, credenciales)
      .pipe(
        tap((respuesta) => {
          this.guardarToken(respuesta.token);
          this.router.navigate(['/admin']);   // entra al panel
        }),
      );
  }

  /**
   * Cierra la sesión: borra el token y redirige al login.
   *
   * <p>Se invoca desde el botón "Salir" Y automáticamente desde el interceptor
   * cuando el backend responde 401 (token caducado).</p>
   */
  logout(): void {
    this.tokenState.set(null);
    localStorage.removeItem(this.CLAVE);
    this.router.navigate(['/admin/login']);
  }

  // ==========================================================================
  //  PERSISTENCIA Y DECODIFICACIÓN
  // ==========================================================================

  /** Lee el token de localStorage al arrancar la app (para sobrevivir F5). */
  private cargarTokenInicial(): string | null {
    // typeof window = comprobación SSR-safe: si Angular corriera en Node
    // (server-side rendering) no existiría localStorage.
    if (typeof window === 'undefined') {
      return null;
    }
    const token = localStorage.getItem(this.CLAVE);
    // Si el token ya caducó, lo descartamos (ahorramos un 401 inútil)
    return token && !this.estaCaducado(token) ? token : null;
  }

  private guardarToken(token: string): void {
    localStorage.setItem(this.CLAVE, token);
    this.tokenState.set(token);
  }

  /**
   * ¿Ha caducado el token?
   *
   * <p>Un JWT tiene 3 partes separadas por puntos: `header.payload.firma`.
   * El payload es JSON en base64url y contiene claims como `exp`
   * (fecha de expiración en segundos Unix). Lo decodificamos SOLO para leer
   * `exp`: la FIRMA la valida el backend, jamás confiamos en el cliente.</p>
   *
   * <p>base64url = base64 con dos cambios: `-` por `+`, `_` por `/` (para ser
   * seguro en URLs). `atob` puro fallaría sin ese reemplazo.</p>
   */
  private estaCaducado(token: string): boolean {
    try {
      const payload = token.split('.')[1];                 // la parte central
      const json = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
      const datos = JSON.parse(json) as { exp?: number };
      if (!datos.exp) {
        return false;   // sin exp → lo dejamos pasar; el backend dirá 401
      }
      // exp en SEGUNDOS; Date.now() en MILISEGUNDOS → ×1000
      return Date.now() >= datos.exp * 1000;
    } catch {
      // Token mal formado → lo tratamos como caducado (defensa)
      return true;
    }
  }
}
