import { describe, expect, it } from 'vitest';
import { Subject, of, throwError } from 'rxjs';
import { recurso } from './recurso';

/**
 * TESTS de `recurso()` — el patrón de estado HTTP de la Fase 5.
 *
 * ============================================================================
 *  CÓMO SE EJECUTAN ESTOS TESTS
 * ============================================================================
 * `ng test` usa el builder `@angular/build:unit-test` con **Vitest** como
 * runner (Vitest reemplaza a Karma, deprecado). Corre en Node con entorno
 * **jsdom** (un DOM falso en memoria): sin navegador, sin abrir puertos,
 * en milisegundos.
 *
 * Los imports (`describe`, `it`, `expect`) vienen EXPLÍCITAMENTE de 'vitest'
 * en vez de globals: así el test es legible por sí solo y no depende de
 * config extra de "globals".
 *
 * ============================================================================
 *  ¿POR QUÉ `recurso()` MERECE SU PROPIO TEST?
 * ============================================================================
 * Es la pieza que evita el "Cargando… eterno" cuando la API falla. Si se
 * rompiera, TODAS las páginas públicas dejarían de cargar datos (o
 * explotarían al leer un signal con excepción), y ningún otro test lo
 * detectaría: los tests de componentes usarían datos falsos.
 */
describe('recurso', () => {
  it('arranca en "cargando" y pasa a "ok" cuando llegan los datos', () => {
    // Un Subject NO emite hasta que lo llamamos: así podemos observar el
    // estado intermedio "cargando" (con of() sería instantáneo y no lo veríamos)
    const fuente = new Subject<number>();

    const r = recurso<number>(() => fuente);

    expect(r.estado()).toBe('cargando');

    fuente.next(42);

    expect(r.estado()).toBe('ok');
    expect(r.datos()).toBe(42);
  });

  it('si la petición FALLA pasa a "error" y conserva el valor inicial', () => {
    // throwError simula una API caída (500, red caída, CORS...)
    const r = recurso<string[]>(
      () => throwError(() => new Error('boom')),
      [],          // valor inicial: lo que verá el template mientras carga
    );

    // Lo esencial: el signal queda en 'error' y NO propaga la excepción.
    // Si lo hiciera, Angular reventaría el template (eso es exactamente lo
    // que pasaba antes con toSignal sin catchError).
    expect(r.estado()).toBe('error');
    expect(r.datos()).toEqual([]);
  });

  it('recargar() relanza la petición y vuelve a pasar por "cargando"', () => {
    let llamadas = 0;

    const r = recurso<number>(() => {
      llamadas++;
      return of(llamadas);   // cada llamada devuelve un valor distinto
    });

    // La primera petición se dispara al CREAR el recurso
    expect(llamadas).toBe(1);
    expect(r.datos()).toBe(1);

    // El botón "Reintentar" del aviso de error llama a recargar()
    r.recargar();

    expect(llamadas).toBe(2);
    expect(r.datos()).toBe(2);
    expect(r.estado()).toBe('ok');
  });

  it('recargar() tras un fallo recupera el estado a "ok"', () => {
    let falla = true;
    const r = recurso<string>(() =>
      falla ? throwError(() => new Error('500')) : of('recuperado'),
    );

    expect(r.estado()).toBe('error');

    falla = false;          // "la API vuelve a estar en pie"
    r.recargar();

    expect(r.estado()).toBe('ok');
    expect(r.datos()).toBe('recuperado');
  });

  it('sin valor inicial, los datos son null mientras carga', () => {
    const fuente = new Subject<{ nombre: string }>();
    const r = recurso<{ nombre: string }>(() => fuente);

    // Esto es lo que obliga al template a usar "?." (perfil.datos()?.nombre)
    expect(r.datos()).toBeNull();

    fuente.next({ nombre: 'Adrián' });

    expect(r.datos()?.nombre).toBe('Adrián');
  });
});
