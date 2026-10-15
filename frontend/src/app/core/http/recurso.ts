import { Signal, signal } from '@angular/core';
import { Observable, catchError, of } from 'rxjs';

/**
 * ============================================================================
 *  ESTADO DE UN RECURSO HTTP (Fase 5 — pulido)
 * ============================================================================
 * Patrón "recurso con estado": convierte un Observable de la API en un
 * SIGNAL que expone NO solo los datos, sino TAMBIÉN si están cargando,
 * si han llegado bien o si han fallado.
 *
 * <p><b>¿Por qué hace falta?</b> Antes usábamos `toSignal(obs, {initialValue})`
 * y, si la API fallaba, el template se quedaba en un "Cargando…" eterno
 * (o directamente rompía). Un usuario no debe ver nunca un spinner infinito:
 * hay que decirle qué pasó y ofrecer un botón de reintento.</p>
 *
 * <pre>
 *   proyectos = recurso(() =&gt; this.http.get&lt;Proyecto[]&gt;(url), []);
 *
 *   // en el template:
 *   @if (proyectos.estado() === 'cargando') { ... skeleton ... }
 *   @if (proyectos.estado() === 'error')    { ... aviso + reintentar ... }
 *   @if (proyectos.estado() === 'ok')       { ... proyectos.datos() ... }
 * </pre>
 */
export type EstadoHttp<T> =
  | { estado: 'cargando'; datos: T | null }
  | { estado: 'ok'; datos: T }
  | { estado: 'error'; datos: T | null };

/**
 * Recurso HTTP con estado + capacidad de recarga.
 *
 * Devolvemos un OBJETO (no un signal plano) porque necesitamos tres cosas:
 *   - `estado`: signal con 'cargando' | 'ok' | 'error' (fácil de leer en el template)
 *   - `datos`:  signal con los datos (o el valor inicial si no hay)
 *   - `recargar()`: relanza la petición (para el botón "Reintentar")
 *
 * <p><b>¿Por qué dos signals separados?</b> Por legibilidad en el template:
 * `recursos.estado() === 'ok'` y `recursos.datos()` se leen mucho mejor que
 * acceder a un objeto anidado. Internamente ambos se actualizan juntos.</p>
 *
 * <p><b>¿Por qué no reasignar el signal en el componente?</b> En zoneless,
 * Angular solo re-renderiza cuando cambia un SIGNAL que el template lee.
 * Si el componente hiciera `this.perfil = recurso(...)`, nadie sabría que
 * algo cambió. En cambio, `recargar()` hace `.set()` DENTRO del signal, y eso
 * sí notifica a las vistas.</p>
 */
export interface Recurso<T> {
  /** Signal con el estado actual: 'cargando' | 'ok' | 'error'. */
  estado: Signal<EstadoHttp<T>['estado']>;
  /** Signal con los datos (valor inicial mientras carga o si falla). */
  datos: Signal<T>;
  /** Relanza la petición (vuelve a 'cargando' y re-llama a la fuente). */
  recargar: () => void;
}

/**
 * SOBRECARGAS de `recurso`.
 *
 * <p>Gracias a estas dos firmas, el TIPO de `datos` depende de si pasas
 * valor inicial:</p>
 * <pre>
 *   recurso(() => http.get&lt;Perfil&gt;())              → Recurso&lt;Perfil | null&gt;
 *   recurso(() => http.get&lt;Proyecto[]&gt;(), [])      → Recurso&lt;Proyecto[]&gt;
 * </pre>
 * <p>Así el compilador SABE que `perfil.datos()` puede ser null (y exige
 * manejarlo en el template), mientras que `proyectos.datos()` es siempre un
 * array. Sin este ajuste tendríamos que hacer `as T` (mentir al compilador)
 * o forzar `?.` innecesario en sitios donde no hace falta.</p>
 */
export function recurso<T>(fuente: () => Observable<T>): Recurso<T | null>;
export function recurso<T>(fuente: () => Observable<T>, inicial: T): Recurso<T>;

/**
 * Implementación de `recurso` (no la llames directamente: usa las firmas de
 * arriba, que añaden la inferencia de tipos correcta).
 *
 * @param fuente  Fábrica del Observable (se invoca CADA VEZ que se llama,
 *                así `recargar()` puede volver a lanzar la petición).
 * @param inicial Valor mientras carga y en caso de error (null por defecto).
 */
export function recurso<T>(
  fuente: () => Observable<T>,
  inicial: T | null = null,
): Recurso<T | null> {

  const estado = signal<EstadoHttp<T | null>['estado']>('cargando');
  const datos = signal<T | null>(inicial);

  // Dispara la petición. `cargar` es reutilizable → lo llama `recargar()`.
  function cargar(): void {
    estado.set('cargando');

    fuente()
      .pipe(
        // Si algo falla (red caída, 500, CORS...) → null por el canal OK
        catchError(() => of(null)),
      )
      .subscribe({
        next: (valor) => {
          if (valor === null) {
            estado.set('error');
          } else {
            datos.set(valor);
            estado.set('ok');
          }
        },
        // No debería llegar aquí (catchError lo cubre), pero por si acaso
        error: () => estado.set('error'),
      });
  }

  cargar();

  return {
    estado: estado.asReadonly(),
    datos: datos.asReadonly(),
    recargar: cargar,
  };
}
