import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Route, Router, UrlSegment, UrlTree } from '@angular/router';
import { provideZonelessChangeDetection } from '@angular/core';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { AuthService } from './auth.service';
import { adminGuard } from './admin.guard';

/**
 * TESTS del guard de rutas (Fase 6).
 *
 * <p>El guard es la ÚNICA puerta del panel admin en el cliente. Si se
 * rompiera, cualquiera podría ver el panel (aunque no pudiera escribir sin
 * token... una mala pantalla también es un problema de privacidad).</p>
 *
 * <p>Para ejecutar una "función guard" en un test hay que correrla dentro
 * del contexto de inyección (usa `inject()` por dentro):
 * {@code TestBed.runInInjectionContext(() => adminGuard(...))}.</p>
 */
describe('adminGuard', () => {
  const CLAVE = 'portfolio_token';

  function jwtValido(): string {
    const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
    const payload = btoa(
      JSON.stringify({
        sub: 'admin',
        exp: Math.floor(Date.now() / 1000) + 3600,
      }),
    );
    return `${header}.${payload}.firma-falsa`;
  }

  /**
   * Ejecuta el guard como lo haría el Router (con argumentos de relleno).
   * El tipo sale de la propia función: `CanMatchFn` puede devolver valor,
   * UrlTree, Promise u Observable (`MaybeAsync<GuardResult>`).
   */
  function ejecutarGuard(): ReturnType<typeof adminGuard> {
    return TestBed.runInInjectionContext(() =>
      adminGuard({ path: 'admin' } as Route, [] as UrlSegment[]),
    );
  }

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideZonelessChangeDetection(),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    });
  });

  afterEach(() => localStorage.clear());

  it('CON sesión devuelve true y deja entrar', () => {
    localStorage.setItem(CLAVE, jwtValido());
    // Inyectamos para que el servicio lea el token ya guardado
    TestBed.inject(AuthService);

    expect(ejecutarGuard()).toBe(true);
  });

  it('SIN sesión devuelve un UrlTree hacia /admin/login', () => {
    TestBed.inject(AuthService);

    const resultado = ejecutarGuard();

    expect(resultado).toBeInstanceOf(UrlTree);
    // serializeUrl da la forma legible del árbol que Angular encolará.
    // Devolver el UrlTree (y no navegar a mano) es lo que permite que el
    // Router gestione la cola de navegaciones si hay varios guards.
    expect(TestBed.inject(Router).serializeUrl(resultado as UrlTree))
      .toBe('/admin/login');
  });

  it('CON token caducado también redirige (no da por buena la sesión)', () => {
    const caducado = `${btoa(JSON.stringify({ alg: 'HS256' }))}.${btoa(
      JSON.stringify({ sub: 'admin', exp: Math.floor(Date.now() / 1000) - 60 }),
    )}.firma`;
    localStorage.setItem(CLAVE, caducado);
    TestBed.inject(AuthService);

    expect(ejecutarGuard()).toBeInstanceOf(UrlTree);
  });
});
