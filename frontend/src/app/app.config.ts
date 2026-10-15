import { ApplicationConfig, provideBrowserGlobalErrorListeners, provideZonelessChangeDetection } from '@angular/core';
import { provideRouter, withInMemoryScrolling } from '@angular/router';

import { routes } from './app.routes';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import { authInterceptor } from './core/auth/auth.interceptor';

/**
 * CONFIGURACIÓN GLOBAL de la aplicación (app.config.ts).
 *
 * <p>Sustituye al viejo app.module.ts (Angular en modo "standalone" no usa
 * módulos). Aquí se registran los PROVIDERS globales:</p>
 *
 * <ul>
 *   <li><b>provideRouter</b> → registra las rutas + mejoras:
 *     <ul>
 *       <li>{@code withInMemoryScrolling}: al navegar, vuelve arriba del todo
 *           (si no, Angular deja el scroll donde estaba — raro al cambiar de
 *           página larga a corta)</li>
 *       <li>{@code withTitle} no existe en Angular 21: los títulos de pestaña
 *           se declaran con {@code title:} en cada ruta (app.routes.ts)</li>
 *     </ul>
 *   </li>
 *   <li><b>provideHttpClient(withFetch)</b> → registra HttpClient (llamadas a
 *       la API) usando la API fetch nativa del navegador en vez de
 *       XMLHttpRequest (más moderna, permite AbortController/streams)</li>
 *   <li><b>provideZonelessChangeDetection</b> → Angular SIN Zone.js:
 *       detecta cambios por signals/pipes en vez de parchear todo el
 *       navegador (más rápido, camino por defecto en Angular 20+)</li>
 *   <li><b>provideBrowserGlobalErrorListeners</b> → errores globales visibles
 *       en consola en desarrollo</li>
 * </ul>
 */
export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZonelessChangeDetection(),
    provideRouter(routes, withInMemoryScrolling({ scrollPositionRestoration: 'top' })),
    provideHttpClient(withFetch(), withInterceptors([authInterceptor])),
  ],
};
