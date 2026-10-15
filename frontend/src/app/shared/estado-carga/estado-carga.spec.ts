import { TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { ComponentFixture } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { EstadoCarga } from './estado-carga';

/**
 * TESTS del componente EstadoCarga (Fase 6).
 *
 * <p>Es el componente que decide qué ve el usuario cuando la API tarda
 * (skeleton) o FALLA (aviso + botón Reintentar). Si se rompiera, las
 * páginas públicas mostrarían un hueco vacío sin explicación — una mala
 * primera impresión justo para un recruiter que entra a tu portfolio.</p>
 *
 * <p><b>Notas de la mecánica de tests de componentes:</b></p>
 * <ul>
 *   <li>{@code TestBed.createComponent} construye el componente pero NO
 *       pinta hasta {@code fixture.detectChanges()} (nosotros controlamos
 *       cuándo "pasa el tiempo").</li>
 *   <li>{@code estado} es un `input.required()` → hay que asignarlo con
 *       `setInput` ANTES del primer render (si no, Angular lanza NG0950).</li>
 *   <li>Se proporciona `provideZonelessChangeDetection()` porque la app es
 *       zoneless (sin Zone.js): los tests deben reflejar la misma
 *       configuración que producción.</li>
 * </ul>
 */
describe('EstadoCarga', () => {
  let fixture: ComponentFixture<EstadoCarga>;
  let raiz: HTMLElement;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [EstadoCarga],
      providers: [provideZonelessChangeDetection()],
    });

    fixture = TestBed.createComponent(EstadoCarga);
    raiz = fixture.nativeElement as HTMLElement;
  });

  /** Pinta el componente con el estado indicado (como haría la página). */
  function pintar(
    estado: 'cargando' | 'ok' | 'error',
    extras?: { tipo?: 'tarjetas' | 'texto' | 'timeline'; cantidad?: number },
  ): void {
    fixture.componentRef.setInput('estado', estado);
    if (extras?.tipo) {
      fixture.componentRef.setInput('tipo', extras.tipo);
    }
    if (extras?.cantidad !== undefined) {
      fixture.componentRef.setInput('cantidad', extras.cantidad);
    }
    fixture.detectChanges();
  }

  // ---------------------------------------------------------------------
  // ESTADO: CARGANDO (skeleton)
  // ---------------------------------------------------------------------

  it('mientras carga pinta el skeleton y anuncia "Cargando contenido…"', () => {
    pintar('cargando');

    expect(raiz.querySelector('.skeleton')).not.toBeNull();

    // El role="status" hace que el lector de pantalla anuncie la carga;
    // sin él, quien navega con accesibilidad solo oiría silencio.
    const anuncio = raiz.querySelector('[role="status"]');
    expect(anuncio?.textContent).toContain('Cargando contenido…');

    expect(raiz.querySelector('.error-carga')).toBeNull();
  });

  it('el skeleton dibuja tantas filas como "cantidad" indique', () => {
    pintar('cargando', { tipo: 'tarjetas', cantidad: 5 });

    expect(raiz.querySelectorAll('.skeleton__card')).toHaveLength(5);
  });

  it('el aspecto del skeleton cambia según el tipo (tarjetas vs timeline)', () => {
    pintar('cargando', { tipo: 'timeline' });

    expect(raiz.querySelector('.skeleton--timeline')).not.toBeNull();
    expect(raiz.querySelector('.skeleton__item')).not.toBeNull();
    expect(raiz.querySelector('.skeleton__card')).toBeNull();
  });

  // ---------------------------------------------------------------------
  // ESTADO: ERROR (aviso + reintento)
  // ---------------------------------------------------------------------

  it('si falla muestra el aviso con su botón de reintento', () => {
    pintar('error');

    const aviso = raiz.querySelector('.error-carga');
    expect(aviso).not.toBeNull();
    expect(aviso?.textContent).toContain('No se pudo cargar el contenido.');

    expect(raiz.querySelector('.error-carga__btn')?.textContent)
      .toContain('Reintentar');

    expect(raiz.querySelector('.skeleton')).toBeNull();
  });

  it('pulsar "Reintentar" emite el evento reintentar (para que la página recargue)', () => {
    pintar('error');

    // El componente NO recarga él mismo: solo AVISA a la página, que es
    // quien posee el recurso HTTP (responsabilidad única).
    const emitido = vi.fn();
    fixture.componentRef.instance.reintentar.subscribe(emitido);

    (raiz.querySelector('.error-carga__btn') as HTMLButtonElement).click();

    expect(emitido).toHaveBeenCalledTimes(1);
  });

  // ---------------------------------------------------------------------
  // ESTADO: OK
  // ---------------------------------------------------------------------

  it('con datos cargados no pinta ni skeleton ni aviso (deja paso al contenido)', () => {
    pintar('ok');

    expect(raiz.querySelector('.skeleton')).toBeNull();
    expect(raiz.querySelector('.error-carga')).toBeNull();
  });

  afterEach(() => fixture?.destroy());
});
