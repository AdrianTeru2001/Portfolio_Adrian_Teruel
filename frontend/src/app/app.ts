import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Navbar } from './shared/navbar/navbar';
import { Footer } from './shared/footer/footer';

/**
 * COMPONENTE RAÍZ (el "shell" de la SPA).
 *
 * <p>Su única misión: montar la estructura fija y dejar que el router
 * pinte la página actual dentro de {@code <router-outlet/>}.</p>
 *
 * <p>{@code <router-outlet>} es un "hueco" dinámico: Angular inserta ahí el
 * componente de la ruta activa (sin recargar la página completa — eso es
 * exactamente lo que significa SPA).</p>
 */
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Navbar, Footer],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {}
