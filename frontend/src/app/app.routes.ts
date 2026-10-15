import { Routes } from '@angular/router';

/**
 * RUTAS de la aplicación (el "mapa" URL → componente).
 *
 * <p><b>Lazy loading con loadComponent:</b> cada página se descarga como
 * chunk separado SOLO cuando el usuario navega a ella. La carga inicial de
 * la home es mucho más ligera (clave para el LCP que miden los buscadores).</p>
 *
 * <p>Con components standalone no hace falta un módulo: basta
 * {@code loadComponent: () => import(...)}.</p>
 */
export const routes: Routes = [
  {
    path: '',
    title: 'Inicio | Adrián Teruel',     // title cambia la pestaña del navegador (SEO básico)
    loadComponent: () =>
      import('./features/home/home').then((m) => m.Home),
  },
  {
    path: 'trayectoria',
    title: 'Trayectoria | Adrián Teruel',
    loadComponent: () =>
      import('./features/trayectoria/trayectoria').then((m) => m.Trayectoria),
  },
  {
    path: 'proyectos',
    title: 'Proyectos | Adrián Teruel',
    loadComponent: () =>
      import('./features/proyectos/proyectos').then((m) => m.Proyectos),
  },
  {
    path: 'contacto',
    title: 'Contacto | Adrián Teruel',
    loadComponent: () =>
      import('./features/contacto/contacto').then((m) => m.Contacto),
  },

  // ---------------------------------------------------------------------
  // Redirecciones y 404
  // ---------------------------------------------------------------------
  // Rutas antiguas/raras → home (si en el futuro cambias la estructura)
  { path: 'home', redirectTo: '', pathMatch: 'full' },

  // Cualquier otra URL no definida → página de error
  {
    path: '**',
    title: 'Página no encontrada | Adrián Teruel',
    loadComponent: () =>
      import('./features/no-encontrado/no-encontrado').then((m) => m.NoEncontrado),
  },
  // ↑ El wildcard SIEMPRE va el último: Angular evalía las rutas en orden.
];
