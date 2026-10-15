import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { API_URL } from '../api-url.token';
import { AuthService } from './auth.service';

/**
 * ============================================================================
 *  INTERCEPTOR DE AUTENTICACIÓN (Fase 4)
 * ============================================================================
 * Un interceptor es un "hook" que recorre TODAS las peticiones HTTP antes de
 * que salgan (y todas las respuestas antes de que lleguen al componente).
 *
 * <p>Nos sirve para dos cosas:</p>
 * <ol>
 *   <li><b>Añadir el JWT</b> a las llamadas a NUESTRA API (solo si hay sesión)
 *       — así ningún servicio tiene que acordarse de poner la cabecera.</li>
 *   <li><b>Gestionar el 401 globalmente</b>: si el backend dice "no autorizado"
 *       (token caducado), cerramos sesión y mandamos al login SIN que cada
 *       componente tenga que manejar ese caso.</li>
 * </ol>
 *
 * <p><b>Funcional vs de clase:</b> desde Angular 15 se usan "functional
 * interceptors" (una función pura) en vez de clases con decoradores. Se
 * registran en `provideHttpClient(withInterceptors([...]))` y tienen acceso a
 * `inject()`.</p>
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {

  const authService = inject(AuthService);
  const router = inject(Router);
  const api = inject(API_URL);

  // Copia de la petición. `clone` es INMUTABLE: req no se toca (los
  // interceptors se encadenan; si mutaras romperías a los demás).
  let peticion = req;

  // ¿Es una petición a NUESTRA API y hay token?
  // (si el frontend llamara a otra URL externa, no le metemos nuestro JWT)
  const esNuestraApi = req.url.startsWith(api);
  const token = authService.token();

  if (esNuestraApi && token) {
    peticion = req.clone({
      setHeaders: {
        // Cabecera estándar: "Bearer <token>" (RFC 6750)
        Authorization: `Bearer ${token}`,
      },
    });
  }

  // `next(peticion)` envía la petición (al siguiente interceptor o a la red).
  // catchError intercepta el CANAL DE ERROR de la respuesta.
  return next(peticion).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse && error.status === 401) {
        // 401 en una llamada a nuestra API = token inválido/caducado.
        // OJO: no lo hacemos en el login (ahí el 401 = credenciales malas
        // y lo debe mostrar el formulario, no expulsar de una página en la
        // que ya estamos).
        if (esNuestraApi && !req.url.includes('/auth/login')) {
          authService.logout();   // borra token + redirige al login
        }
      }
      // Re-lanzamos el error para que el componente también pueda reaccionar
      // (catchError no "consume" el error si devuelves throwError).
      return throwError(() => error);
    }),
  );
};
