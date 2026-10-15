import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { provideZonelessChangeDetection } from '@angular/core';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { AuthService } from './auth.service';
import { authInterceptor } from './auth.interceptor';

/**
 * TESTS del interceptor de autenticación (Fase 6).
 *
 * <p>Este interceptor hace DOS cosas críticas, y ambas son fáciles de romper
 * sin darse cuenta:</p>
 * <ol>
 *   <li>Mete el JWT en la cabecera `Authorization` — si falla, TODO el CRUD
 *       empezaría a devolver 401 CON sesión activa.</li>
 *   <li>Ante un 401 del backend cierra sesión — si falla, el usuario se queda
 *       en un panel muerto con un token caducado.</li>
 * </ol>
 *
 * <p>Se registra igual que en `app.config.ts`, pero con
 * `provideHttpClientTesting()` DETRÁS: así las respuestas las decide el test
 * (`req.flush(...)`) y nada sale hacia la red.</p>
 *
 * <p><b>OJO al orden (detalle que costó depurar):</b> `AuthService` lee
 * localStorage UNA SOLA VEZ, al construirse. Por eso cada test precarga el
 * token ANTES de inyectar el servicio; inyectarlo antes (p. ej. en
 * `beforeEach`) dejaría el signal en `null` aunque después guardáramos el
 * token.</p>
 */
describe('authInterceptor', () => {
  const API = 'http://localhost:8080/api';
  const CLAVE = 'portfolio_token';

  /** JWT de prueba (solo el frontend lee `exp`: la firma es cosa del backend). */
  function jwt(): string {
    return `${btoa(JSON.stringify({ alg: 'HS256' }))}.${btoa(
      JSON.stringify({ sub: 'admin', exp: Math.floor(Date.now() / 1000) + 3600 }),
    )}.firma`;
  }

  let httpMock: HttpTestingController;
  let http: HttpClient;
  let logout: ReturnType<typeof vi.spyOn>;

  beforeEach(() => {
    localStorage.clear();

    TestBed.configureTestingModule({
      providers: [
        provideZonelessChangeDetection(),
        // El interceptor se registra AQUÍ igual que en app.config.ts
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    });

    httpMock = TestBed.inject(HttpTestingController);
    http = TestBed.inject(HttpClient);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  /**
   * Activa la sesión: guarda el token y YA DESPUÉS construye el servicio
   * (que es cuando lo lee). Espía `logout` para poder afirmar si se llamó
   * sin disparar la redirección real.
   */
  function conSesion(): void {
    localStorage.setItem(CLAVE, jwt());
    const auth = TestBed.inject(AuthService);
    logout = vi.spyOn(auth, 'logout').mockImplementation(() => {});
  }

  /** Versión sin sesión: solo armamos el espía de logout. */
  function sinSesion(): void {
    const auth = TestBed.inject(AuthService);
    logout = vi.spyOn(auth, 'logout').mockImplementation(() => {});
  }

  // ---------------------------------------------------------------------
  // CABECERA Authorization
  // ---------------------------------------------------------------------

  it('añade Authorization: Bearer <token> a las llamadas a NUESTRA API', () => {
    conSesion();

    http.get(`${API}/proyectos`).subscribe();

    const req = httpMock.expectOne(`${API}/proyectos`);
    expect(req.request.headers.get('Authorization')).toBe(
      `Bearer ${localStorage.getItem(CLAVE)}`,
    );
    req.flush([]);
  });

  it('NO añade la cabecera si no hay sesión', () => {
    sinSesion();

    http.get(`${API}/proyectos`).subscribe();

    const req = httpMock.expectOne(`${API}/proyectos`);
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush([]);
  });

  it('NO filtra nuestro JWT hacia dominios externos', () => {
    conSesion();

    // Nuestro token no debe salir a terceros: filtrar credenciales a un
    // dominio ajeno es un fallo de privacidad clásico.
    http.get('https://api.github.com/users/x').subscribe();

    const req = httpMock.expectOne('https://api.github.com/users/x');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });

  // ---------------------------------------------------------------------
  // GESTIÓN DEL 401
  // ---------------------------------------------------------------------

  it('ante un 401 de NUESTRA API cierra sesión y re-lanza el error', () => {
    conSesion();

    let errorVisto: unknown = null;
    http.get(`${API}/proyectos`).subscribe({ error: (e) => (errorVisto = e) });

    httpMock
      .expectOne(`${API}/proyectos`)
      .flush('caducado', { status: 401, statusText: 'Unauthorized' });

    expect(logout).toHaveBeenCalledTimes(1);
    // El error NO se traga: el componente puede seguir reaccionando a él.
    expect(errorVisto).not.toBeNull();
  });

  it('ante un 401 en el LOGIN NO cierra sesión', () => {
    // Distinción crucial: un 401 en /auth/login = contraseña equivocada.
    // Si cerráramos sesión aquí, el formulario de acceso expulsaría al
    // usuario de la propia página de acceso (y ni siquiera hay sesión que
    // cerrar).
    conSesion();

    let errorVisto: unknown = null;
    http
      .post(`${API}/auth/login`, { username: 'a', password: 'b' })
      .subscribe({ error: (e) => (errorVisto = e) });

    httpMock
      .expectOne(`${API}/auth/login`)
      .flush('mal', { status: 401, statusText: 'Unauthorized' });

    expect(logout).not.toHaveBeenCalled();
    expect(errorVisto).not.toBeNull();
  });

  it('un 401 de un dominio externo tampoco cierra sesión', () => {
    conSesion();

    http.get('https://otro-servicio.com/datos').subscribe({ error: () => {} });

    httpMock
      .expectOne('https://otro-servicio.com/datos')
      .flush('no', { status: 401, statusText: 'Unauthorized' });

    expect(logout).not.toHaveBeenCalled();
  });
});
