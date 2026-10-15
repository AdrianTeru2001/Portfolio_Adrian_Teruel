import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Navbar } from './shared/navbar/navbar';
import { Footer } from './shared/footer/footer';
import { ScrollTop } from './shared/scroll-top/scroll-top';
import { SeoService } from './core/seo/seo.service';

/**
 * COMPONENTE RAÍZ (el "shell" de la SPA).
 *
 * <p>Su única misión: montar la estructura fija y dejar que el router
 * pinte la página actual dentro de {@code <router-outlet/>}.</p>
 *
 * <p>{@code <router-outlet>} es un "hueco" dinámico: Angular inserta ahí el
 * componente de la ruta activa (sin recargar la página completa — eso es
 * exactamente lo que significa SPA).</p>
 *
 * <p><b>SEO (Fase 5):</b> en el constructor arrancamos el {@link SeoService},
 * que actualiza `<title>` y meta tags en cada navegación. Es el sitio
 * correcto: una sola vez al arrancar la app.</p>
 */
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Navbar, Footer, ScrollTop],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  // Arranca la actualización de meta tags al navegar (SEO de SPA)
  constructor() {
    inject(SeoService).inicializar();
  }
}
