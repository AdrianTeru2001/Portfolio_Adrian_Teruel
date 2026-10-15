import { inject, Injectable } from '@angular/core';
import { Meta } from '@angular/platform-browser';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { filter, map } from 'rxjs';

/**
 * ============================================================================
 *  SERVICIO SEO (Fase 5 — pulido)
 * ============================================================================
 * En una SPA (Single Page Application) el navegador solo recibe UN index.html
 * con el mismo `<title>` y las mismas meta tags para TODAS las rutas. Google
 * y los lectores de pantalla leen esas etiquetas, así que si no las
 * ACTUALIZAS al navegar, todas las páginas parecen idénticas a los bots.
 *
 * <p>Este servicio escucha cada cambio de ruta (`NavigationEnd`) y actualiza:</p>
 * <ul>
 *   <li>`<title>` de la pestaña</li>
 *   <li>`<meta name="description">` (el texto que aparece en Google)</li>
 *   <li>`og:title` / `og:description` (lo que se pinta al compartir en
 *       LinkedIn, WhatsApp...)</li>
 * </ul>
 *
 * <p>Los datos vienen declarados en `app.routes.ts` con `data: {...}` en cada
 * ruta — así añadir una página nueva = añadir sus meta tags ahí mismo.</p>
 *
 * <p><b>Nota importante de SEO para SPAs:</b> los buscadores modernos
 * (Google) ejecutan JavaScript, así que sí ven el contenido dinámico. Pero
 * otros bots (los de redes sociales, por ejemplo) NO ejecutan JS: para
 * ellos, las meta tags del HTML inicial son todo lo que hay. Por eso
 * en producción conviene un pre-renderizado (Angular SSR / Prerender) —
 * queda anotado como mejora futura en la Fase 7.</p>
 */
@Injectable({ providedIn: 'root' })
export class SeoService {

  // Meta y Title son servicios oficiales de Angular para tocar el <head>
  private meta = inject(Meta);
  private title = inject(Title);
  private router = inject(Router);
  private rutaActual = inject(ActivatedRoute);

  /**
   * Arranca la "escucha" de navegaciones. Se llama UNA vez desde el
   * componente raíz (App).
   *
   * <p><b>ATENCIÓN — el árbol de rutas:</b> el `ActivatedRoute` que inyectamos
   * es la RAÍZ del árbol de navegación, y sus `data` son siempre `{}`. Los
   * `data: { description }` que declaramos en app.routes.ts viven en los nodos
   * internos. Por eso RECORREMOS el árbol entero (raíz → hoja) y FUSIONAMOS
   * los `data` de cada nodo: sin ese bucle el servicio no aplicaría NINGUNA
   * meta tag (error clásico y silencioso).</p>
   *
   * <p>Fusionar el árbol completo (y no solo la hoja) permite declarar
   * `data: { robots: 'noindex' }` UNA sola vez en la ruta padre `/admin` y
   * que lo hereden todas sus páginas hijas.</p>
   *
   * <p>Usamos `snapshot` (síncrono) en vez del observable `data` porque
   * `NavigationEnd` solo se emite cuando la navegación está RESUELTA:
   * lazy loading incluido, así que el snapshot ya es el definitivo.</p>
   */
  inicializar(): void {
    this.router.events
      .pipe(
        // Solo nos interesan los eventos de navegación COMPLETADA
        filter((evento): evento is NavigationEnd =>
          evento instanceof NavigationEnd),

        // Fusión de `data` desde la raíz hasta la hoja activa
        map(() => {
          const fusionado: Record<string, unknown> = {};
          let ruta: ActivatedRoute | null = this.rutaActual;
          while (ruta) {
            Object.assign(fusionado, ruta.snapshot.data);
            ruta = ruta.firstChild;
          }
          return fusionado;
        }),
      )
      .subscribe((datos) => {
        const meta = datos as {
          title?: string;
          description?: string;
          robots?: string;
        };

        if (meta.title) {
          this.title.setTitle(meta.title);
        }

        // description + og:description siempre juntos: el texto que se
        // comparte debe coincidir con el que ve el usuario en Google
        if (meta.description) {
          this.meta.updateTag({
            name: 'description',
            content: meta.description,
          });
          this.meta.updateTag({
            property: 'og:title',
            content: meta.title ?? 'Adrián Teruel',
          });
          this.meta.updateTag({
            property: 'og:description',
            content: meta.description,
          });
        }

        // robots: por defecto "index, follow"; el área admin declara
        // "noindex, nofollow" en sus rutas para que Google NO la liste.
        // Al volver a una ruta pública, este mismo `updateTag` restaura
        // el valor por defecto (los meta tags son estado compartido en el
        // <head>: hay que resetearlos al salir, no solo al entrar).
        this.meta.updateTag({
          name: 'robots',
          content: meta.robots ?? 'index, follow',
        });
      });
  }
}
