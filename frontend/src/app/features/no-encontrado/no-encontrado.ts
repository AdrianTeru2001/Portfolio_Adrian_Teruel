import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

/**
 * PÁGINA 404 (ruta comodín **).
 * Mantenerla amable es buena práctica de UX: nunca dejes al usuario en un
 * callejón sin salida — siempre ofrécele un camino de vuelta.
 */
@Component({
  selector: 'app-no-encontrado',
  imports: [RouterLink],
  template: `
    <section class="error404">
      <p class="error404__codigo">404</p>
      <h1>Página no encontrada</h1>
      <p class="error404__texto">La página que buscas no existe o se ha movido.</p>
      <a routerLink="/" class="error404__enlace">← Volver al inicio</a>
    </section>
  `,
  styles: `
    .error404 {
      max-width: 1100px;
      margin: 0 auto;
      padding: 6rem 1.25rem;
      text-align: center;
      color: #cbd5e1;
    }
    .error404__codigo {
      font-size: 6rem;
      font-weight: 800;
      margin: 0;
      background: linear-gradient(135deg, #38bdf8, #6366f1);
      -webkit-background-clip: text;
      background-clip: text;
      color: transparent;
      line-height: 1;
    }
    h1 { color: #f1f5f9; margin: 1rem 0 0.5rem; }
    .error404__texto { color: #94a3b8; margin: 0 0 2rem; }
    .error404__enlace {
      color: #38bdf8;
      text-decoration: none;
      font-weight: 600;
      &:hover { text-decoration: underline; }
    }
  `,
})
export class NoEncontrado {}
