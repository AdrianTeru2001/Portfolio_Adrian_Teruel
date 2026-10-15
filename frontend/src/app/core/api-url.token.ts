import { InjectionToken } from '@angular/core';

/**
 * TOKEN DE INYECCIÓN para la URL base de la API.
 *
 * <p><b>¿Por qué un InjectionToken y no una constante?</b></p>
 * Si fuera una simple `export const API_URL = '...'`, cambiar la URL en
 * producción obligaría a RECOMPILAR el código. Con token + valor por defecto,
 * la URL se puede sobreescribir por variable de entorno en el build de CI/CD
 * sin tocar código fuente (patrón "dependency injection" de Angular).</p>
 *
 * <p>Uso en servicios:</p>
 * <pre>
 *   private api = inject(API_URL);   // "http://localhost:8080/api"
 * </pre>
 *
 * <p>Valor por defecto: backend en local. En producción (docker/CI) se
 * sustituye por la URL real del despliegue.</p>
 */
export const API_URL = new InjectionToken<string>('API_URL', {
  providedIn: 'root',
  factory: () => 'http://localhost:8080/api',
});
