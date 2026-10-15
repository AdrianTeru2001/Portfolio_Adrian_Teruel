import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { LoginRequest } from '../../../core/models/portfolio.models';

/**
 * PÁGINA: Login del panel admin.
 *
 * <p>Patrón de estados del envío (igual que en Contacto, pero con un estado
 * de "error" diferenciado para credenciales incorrectas):</p>
 * <pre>
 *   idle → enviando → (ok: navega al panel) | error (401 credenciales)
 * </pre>
 */
@Component({
  selector: 'app-login',
  imports: [FormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {

  private authService = inject(AuthService);

  /** Credenciales enlazadas con [(ngModel)]. */
  credenciales: LoginRequest = { username: '', password: '' };

  /** Estado de la petición para condicionar la UI. */
  estado = signal<'idle' | 'enviando' | 'error'>('idle');

  enviar(): void {
    // Guardia anti doble envío (el usuario puede pulsar dos veces)
    if (this.estado() === 'enviando') {
      return;
    }
    this.estado.set('enviando');

    this.authService.login(this.credenciales).subscribe({
      // Éxito: AuthService ya redirige a /admin en su tap()
      next: () => this.estado.set('idle'),
      error: (err) => {
        console.error('Login fallido', err);
        // 401 = credenciales incorrectas; cualquier otro fallo de red u otro
        // código mostramos el mismo aviso genérico (sin filtrar info)
        this.estado.set('error');
      },
    });
  }
}
