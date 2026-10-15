import { TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection, Component } from '@angular/core';
import { Meta, Title } from '@angular/platform-browser';
import { provideRouter, Router } from '@angular/router';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { SeoService } from './seo.service';

/**
 * TESTS de SeoService (Fase 6).
 *
 * <p>En una SPA el navegador solo recibe UN index.html: sin este servicio,
 * Google y las redes sociales verían las MISMAS meta tags en todas las
 * páginas. Estos tests protegen los 3 comportamientos con los que jugaba la
 * Fase 5:</p>
 *
 * <ol>
 *   <li><b>description / og:</b> se actualizan al navegar.</li>
 *   <li><b>robots:</b> el área admin es `noindex` Y, lo importante, se
 *       RESTAURA a `index, follow` al volver a una ruta pública (los meta
 *       tags son estado compartido en el `<head>`).</li>
 *   <li><b>Fusión del árbol de rutas:</b> los `data` se heredan de padre a
 *       hijo. Este es el bug clásico que documenta el servicio: si solo se
 *       leyera la hoja, `/admin/...` perdería el `noindex` declarado en el
 *       padre y Google indexaría el panel.</li>
 * </ol>
 *
 * <p>Se usan rutas DUMMY (con un componente vacío) en vez de las reales:
 * aislar el test de las páginas de verdad evita que cambiar un texto de la
 * home rompa tests de SEO.</p>
 *
 * <p><b>Truco aprendido al escribir estos tests:</b> `Meta.getTag()` de
 * Angular 21 construye un selector CSS del estilo
 * {@code meta[<selector>]}, así que leer un tag exige pasar el ATRIBUTO
 * completo: `getTag('name="description"')` (y NO `getTag('description')`,
 * que buscaría un atributo `description` inexistente y devolvería
 * `null`). `updateTag` sí acepta el objeto `{name, content}` directo.</p>
 */
describe('SeoService', () => {

  /** Página mínima: el contenido no importa, solo sus meta tags. */
  @Component({
    selector: 'x-pagina-vacia',
    standalone: true,
    template: '<p>vacio</p>',
  })
  class PaginaVacia {}

  const TITLE_PUBLICA = 'Trayectoria | Test';
  const DESC_PUBLICA = 'Descripción de trayectoria para el test';

  let title: Title;
  let meta: Meta;
  let router: Router;

  beforeEach(async () => {
    TestBed.configureTestingModule({
      providers: [
        provideZonelessChangeDetection(),
        provideRouter([
          {
            path: 'trayectoria',
            title: TITLE_PUBLICA,
            data: { description: DESC_PUBLICA },
            component: PaginaVacia,
          },
          {
            // Ruta PADRE con robots... pero sin description: sirve para
            // comprobar tanto la herencia como que no se pisa lo anterior.
            path: 'secreta',
            data: { robots: 'noindex, nofollow' },
            component: PaginaVacia,
            children: [
              {
                // ...y la HOJA sin robots: solo el bucle de fusión puede
                // darle el noindex del padre.
                path: 'panel',
                title: 'Panel | Test',
                component: PaginaVacia,
              },
            ],
          },
        ]),
      ],
    });

    title = TestBed.inject(Title);
    meta = TestBed.inject(Meta);
    router = TestBed.inject(Router);

    // SeoService se inicializa desde App en producción; aquí a mano.
    TestBed.inject(SeoService).inicializar();
  });

  afterEach(async () => {
    // Volvemos a una ruta pública: así ningún test hereda un `<head>`
    // sucio (noindex) de un test anterior.
    await router.navigate(['/trayectoria']);
  });

  // ---------------------------------------------------------------------
  // TAGS POR RUTA
  // ---------------------------------------------------------------------

  it('al navegar actualiza <title>, description y og:description', async () => {
    await router.navigate(['/trayectoria']);

    // El título de la pestaña (lo aplica el TitleStrategy de Angular con el
    // `title:` de la ruta; el servicio se ocupa del resto de tags)
    expect(title.getTitle()).toBe(TITLE_PUBLICA);

    // description = lo que pinta Google bajo el enlace
    expect(meta.getTag('name="description"')?.content).toBe(DESC_PUBLICA);
    // og:description y description deben coincidir: lo que se ve al
    // compartir en LinkedIn/WhatsApp es lo mismo que en el buscador
    // (getTag acepta un selector: 'property="og:description"')
    expect(meta.getTag('property="og:description"')?.content)
      .toBe(DESC_PUBLICA);
    expect(meta.getTag('property="og:title"')?.content).toBe(TITLE_PUBLICA);
  });

  it('robots es "index, follow" por defecto en rutas públicas', async () => {
    await router.navigate(['/trayectoria']);

    expect(meta.getTag('name="robots"')?.content).toBe('index, follow');
  });

  // ---------------------------------------------------------------------
  // HERENCIA DE `data` A TRAVÉS DEL ÁRBOL (la parte difícil)
  // ---------------------------------------------------------------------

  it('una ruta HIJA hereda el robots noindex declarado en su PADRE', async () => {
    // La hoja /secreta/panel NO declara robots: solo la fusión raíz→hoja
    // del servicio puede aplicar el noindex del padre. Si el servicio
    // leyera solo la hoja, este test fallaría con 'index, follow' y el
    // panel acabaría indexado en Google.
    await router.navigate(['/secreta/panel']);

    expect(meta.getTag('name="robots"')?.content).toBe('noindex, nofollow');
  });

  it('al volver a una ruta pública RESTAURA el robots por defecto', async () => {
    await router.navigate(['/secreta/panel']);
    expect(meta.getTag('name="robots"')?.content).toBe('noindex, nofollow');

    await router.navigate(['/trayectoria']);

    // Sin este reset, quien visitara el admin antes que el panel público
    // se llevaría un "noindex" fantasma en TODAS las páginas (y Google
    // dejaría de indexar tu portfolio entero).
    expect(meta.getTag('name="robots"')?.content).toBe('index, follow');
  });

  it('una ruta SIN description NO borra la description anterior', async () => {
    await router.navigate(['/trayectoria']);
    expect(meta.getTag('name="description"')?.content).toBe(DESC_PUBLICA);

    // /secreta/panel no declara description → el tag debe quedar intacto
    // (vaciarlo dejaría la home sin "anuncio" en Google tras un clic)
    await router.navigate(['/secreta/panel']);

    expect(meta.getTag('name="description"')?.content).toBe(DESC_PUBLICA);
  });
});
