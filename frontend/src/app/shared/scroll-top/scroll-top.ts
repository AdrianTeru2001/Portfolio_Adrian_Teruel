import { Component, HostListener, signal } from '@angular/core';

/**
 * COMPONENTE: botón "volver arriba" (scroll-to-top).
 *
 * <p>Detalle de UX: en páginas largas (trayectoria, proyectos) volver arriba
 * a mano es tedioso. Este botón aparece cuando has bajado cierto punto y,
 * al pulsarlo, sube suavemente al inicio.</p>
 *
 * <p><b>Concepto clave — `@HostListener`:</b> es un decorador que escucha
 * eventos en el NAVEGADOR (window/document) desde dentro de un componente
 * Angular, sin tener que tocar el HTML. Aquí escuchamos el evento `scroll`.</p>
 *
 * <p><b>Rendimiento:</b> el evento `scroll` dispara MUCHAS veces al hacer
 * scroll (decenas por segundo). Para no saturar, solo hacemos `.set()` cuando
 * el estado booleano realmente CAMBIA (si ya era visible, no notificamos).
 * Esa es una micro-optimización habitual con signals.</p>
 */
@Component({
  selector: 'app-scroll-top',
  template: `
    <!--
      aria-hidden en el icono (decorativo) + aria-label en el botón:
      el lector de pantalla anuncia "volver arriba", no el carácter "↑".
    -->
    <button
      type="button"
      class="scroll-top"
      [class.visible]="visible()"
      [attr.tabindex]="visible() ? 0 : -1"
      (click)="subir()"
      aria-label="Volver arriba">
      <span aria-hidden="true">↑</span>
    </button>
  `,
  styles: `
    .scroll-top {
      position: fixed;
      right: 1.25rem;
      bottom: 1.25rem;
      z-index: 90;
      width: 2.9rem;
      height: 2.9rem;
      border-radius: 50%;
      border: none;
      background: linear-gradient(135deg, #38bdf8, #6366f1);
      color: #fff;
      font-size: 1.3rem;
      cursor: pointer;
      box-shadow: 0 4px 18px rgba(56, 189, 248, 0.35);
      /* Empieza invisible y escalado a 0 */
      opacity: 0;
      transform: translateY(12px) scale(0.8);
      pointer-events: none;   /* no captura clics cuando está oculto */
      transition: opacity 0.2s ease, transform 0.2s ease;
    }

    /* Estado "visible" (cuando el usuario ha bajado lo suficiente) */
    .scroll-top.visible {
      opacity: 1;
      transform: translateY(0) scale(1);
      pointer-events: auto;
    }

    .scroll-top:hover {
      transform: translateY(-2px);
    }
  `,
})
export class ScrollTop {

  /** ¿Se muestra el botón? (true tras bajar de cierto punto) */
  visible = signal(false);

  /**
   * Umbral de scroll (en píxeles) a partir del cual aparece el botón.
   * ~400px equivale a "ya has bajado bastante".
   */
  private readonly UMBRAL = 400;

  /**
   * `@HostListener('window:scroll')` → se ejecuta en cada evento de scroll
   * de la ventana. El método recibe el evento (que no usamos aquí).
   *
   * <p>Solo `.set()` si el valor CAMBIA de false→true o true→false; si no,
   * Angular re-renderizaría sin motivo (y en zoneless eso sí consume CPU).</p>
   */
  @HostListener('window:scroll')
  onScroll(): void {
    const debeVerse = window.scrollY > this.UMBRAL;
    if (debeVerse !== this.visible()) {
      this.visible.set(debeVerse);
    }
  }

  /** Vuelve arriba con scroll suave. */
  subir(): void {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
}
