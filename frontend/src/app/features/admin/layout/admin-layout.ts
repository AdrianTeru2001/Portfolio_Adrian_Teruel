import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';

/**
 * COMPONENTE: layout del panel admin (el "shell" del área privada).
 *
 * <p>Estructura análoga a la app pública (navbar + outlet), pero con:</p>
 * <ul>
 *   <li>Enlaces a las secciones admin (dashboard, proyectos, perfil)</li>
 *   <li>Botón "Salir" que invoca {@code authService.logout()}</li>
 *   <li>Un aviso visual claro de que estás en la ZONA PRIVADA</li>
 * </ul>
 *
 * <p>Las rutas hijas se pintan dentro del `<router-outlet/>` de abajo
 * (routing anidado: `admin/proyectos`, `admin/perfil`...).</p>
 */
@Component({
  selector: 'app-admin-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './admin-layout.html',
  styleUrl: './admin-layout.scss',
})
export class AdminLayout {

  // Inyectamos AuthService para el botón de logout (y mostrar usuario)
  protected readonly authService = inject(AuthService);

  /** Invocado por el botón "Salir". */
  salir(): void {
    this.authService.logout();
  }
}
