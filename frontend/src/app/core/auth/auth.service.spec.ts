import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { provideZonelessChangeDetection } from '@angular/core';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { AuthService } from './auth.service';

/**
 * TESTS de AuthService (Fase 6) — login, logout y "supervivencia" del token.
 *
 * <p>Se usa `provideHttpClientTesting()`: intercepta las peticiones para que
 * NADA salga hacia la red. El test decide cuándo y cómo "responde" el
 * backend (`req.flush(...)`), incluso con errores simulados (401). Eso
 * permite probar los dos caminos del login sin un servidor real.</p>
 *
 * <p>El token de prueba se fabrica a mano: no hace falta firmarlo (la firma
 * la valida SOLO el backend; el frontend únicamente lee el claim `exp`).</p>
 */
describe('AuthService', () => {
  const CLAVE = 'portfolio_token';
  const API = 'http://localhost:8080/api';

  /** Fabrica un JWT legible con la expiración indicada (segundos desde ya). */
  function jwt(expEnSegundos: number): string {
    const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
    const payload = btoa(
      JSON.stringify({
        sub: 'admin',
        exp: Math.floor(Date.now() / 1000) + expEnSegundos,
      }),
    );
    return `${header}.${payload}.firma-falsa`;
  }

  let httpMock: HttpTestingController;
  let router: Router;
  let navegar: ReturnType<typeof vi.spyOn>;

  beforeEach(() => {
    localStorage.clear();   // arrancamos siempre de una sesión limpia

    TestBed.configureTestingModule({
      providers: [
        provideZonelessChangeDetection(),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    });

    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    // Se "cueza" navigate: sin esto, cada login/logout intentaría navegar
    // de verdad (con rutas vacías) y el test solo miraría esa consecuencia
    // en vez de la lógica que nos interesa.
    navegar = vi.spyOn(router, 'navigate').mockResolvedValue(true);
  });

  afterEach(() => {
    // Verifica que NO quedó ninguna petición HTTP sin "responder": si un
    // test olvidara un flush, aquí saltaría en vez de fallar en silencio.
    httpMock.verify();
    localStorage.clear();
  });

  function inyectar(): AuthService {
    return TestBed.inject(AuthService);
  }

  // ---------------------------------------------------------------------
  // ESTADO INICIAL
  // ---------------------------------------------------------------------

  it('arranca sin sesión si no hay token guardado', () => {
    const auth = inyectar();

    expect(auth.autenticado()).toBe(false);
    expect(auth.token()).toBeNull();
  });

  it('recupera la sesión de localStorage al arrancar (sobrevive F5)', () => {
    localStorage.setItem(CLAVE, jwt(3600));

    const auth = inyectar();

    expect(auth.autenticado()).toBe(true);
    expect(auth.token()).not.toBeNull();
  });

  it('DESCARTA un token que ya caducó en vez de dar por buena la sesión', () => {
    // Si esto fallara, el usuario vería "con sesión" pero recibiría un 401
    // en la primera petición (o, peor, el guard dejaría entrar a un panel
    // que no puede usar).
    localStorage.setItem(CLAVE, jwt(-60));

    const auth = inyectar();

    expect(auth.autenticado()).toBe(false);
    expect(localStorage.getItem(CLAVE)).toBeNull();
  });

  // ---------------------------------------------------------------------
  // LOGIN
  // ---------------------------------------------------------------------

  it('login correcto guarda el token y navega al panel', () => {
    const auth = inyectar();
    const token = jwt(3600);

    let error: unknown = null;
    auth.login({ username: 'admin', password: 'x' }).subscribe({
      error: (e) => (error = e),
    });

    // Comprobamos que la petición salió bien formada
    const req = httpMock.expectOne(`${API}/auth/login`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ username: 'admin', password: 'x' });

    req.flush({ token, type: 'Bearer', expiresIn: 28800 });

    expect(error).toBeNull();
    expect(auth.autenticado()).toBe(true);
    expect(localStorage.getItem(CLAVE)).toBe(token);
    expect(navegar).toHaveBeenCalledWith(['/admin']);
  });

  it('login con credenciales incorrectas NO guarda nada y propaga el error', () => {
    const auth = inyectar();

    let errorCapturado: unknown = null;
    auth.login({ username: 'admin', password: 'mala' }).subscribe({
      error: (e) => (errorCapturado = e),
    });

    httpMock
      .expectOne(`${API}/auth/login`)
      .flush({}, { status: 401, statusText: 'Unauthorized' });

    // El componente de login es quien pinta el aviso: por eso el servicio
    // DEBE re-lanzar el error (no tragarlo).
    expect(errorCapturado).not.toBeNull();
    expect(auth.autenticado()).toBe(false);
    expect(localStorage.getItem(CLAVE)).toBeNull();
    expect(navegar).not.toHaveBeenCalledWith(['/admin']);
  });

  // ---------------------------------------------------------------------
  // LOGOUT
  // ---------------------------------------------------------------------

  it('logout borra el token y manda al login', () => {
    localStorage.setItem(CLAVE, jwt(3600));
    const auth = inyectar();

    auth.logout();

    expect(auth.autenticado()).toBe(false);
    expect(localStorage.getItem(CLAVE)).toBeNull();
    expect(navegar).toHaveBeenCalledWith(['/admin/login']);
  });
});
