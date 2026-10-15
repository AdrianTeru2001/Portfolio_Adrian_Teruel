import { inject } from '@angular/core';
import { CanMatchFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

/**
 * ============================================================================
 *  GUARD DE RUTAS (Fase 4)
 * ============================================================================
 * Un "guard" es una función que Angular ejecuta ANTES de completar una
 * navegación. Devuelve:
 *   - `true`         → deja pasar
 *   - `false` / UrlTree → bloquea (idealmente redirige)
 *
 * <p><b>`canMatch` vs `canActivate`:</b></p>
 * <ul>
 *   <li>`canActivate` comprueba al ENTRAR en la ruta.</li>
 *   <li>`canMatch` comprueba antes incluso de que Angular descargue el
 *       componente (lazy chunk). Si no hay sesión, ni siquiera se baja el
 *       código del panel → más eficiente y más seguro.</li>
 * </ul>
 *
 * <p>Se registra en `app.routes.ts` con `canMatch: [adminGuard]`.</p>
 */
export const adminGuard: CanMatchFn = () => {

  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.autenticado()) {
    return true;   // hay sesión → entra al panel
  }

  // Sin sesión → redirigimos al login. Devolvemos un UrlTree (no hacemos
  // navigate a mano): es la forma "declarativa" que Angular encola bien
  // si varias guardas se disputan la misma navegación.
  return router.createUrlTree(['/admin/login']);
};
