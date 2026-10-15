import { Routes } from '@angular/router';
import { adminGuard } from './core/auth/admin.guard';

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

  // ---------------------------------------------------------------------
  // ÁREA ADMIN (Fase 4) — protegida por adminGuard (canMatch)
  // ---------------------------------------------------------------------
  // IMPORTANTE: /admin/login se declara ANTES que /admin. El guard canMatch
  // sobre /admin también "casaría" con /admin/login si viniera primero, y
  // redirigiría al dashboard a quien intentara ir al login. Así, el login
  // siempre es accesible sin sesión.
  {
    path: 'admin/login',
    title: 'Acceso | Admin',
    loadComponent: () =>
      import('./features/admin/login/login').then((m) => m.Login),
  },

  // Ruta "padre" /admin → carga AdminLayout (barra admin + outlet) y aplica
  // el GUARD a todo su árbol de rutas hijas.
  {
    path: 'admin',
    canMatch: [adminGuard],   // ← la clave: sin sesión no entra NI se descarga
    loadComponent: () =>
      import('./features/admin/layout/admin-layout')
        .then((m) => m.AdminLayout),
    children: [
      // /admin → dashboard (la portada del panel)
      {
        path: '',
        title: 'Dashboard | Admin',
        loadComponent: () =>
          import('./features/admin/dashboard/dashboard')
            .then((m) => m.Dashboard),
      },
      // /admin/proyectos → CRUD de proyectos
      {
        path: 'proyectos',
        title: 'Proyectos | Admin',
        loadComponent: () =>
          import('./features/admin/proyectos-admin/proyectos-admin')
            .then((m) => m.ProyectosAdmin),
      },
      // /admin/perfil → edición del perfil
      {
        path: 'perfil',
        title: 'Perfil | Admin',
        loadComponent: () =>
          import('./features/admin/perfil-admin/perfil-admin')
            .then((m) => m.PerfilAdmin),
      },
      // Cualquier subruta admin desconocida → dashboard
      { path: '**', redirectTo: '' },
    ],
  },

  // Cualquier otra URL no definida → página de error
  {
    path: '**',
    title: 'Página no encontrada | Adrián Teruel',
    loadComponent: () =>
      import('./features/no-encontrado/no-encontrado').then((m) => m.NoEncontrado),
  },
  // ↑ El wildcard SIEMPRE va el último: Angular evalía las rutas en orden.
];
