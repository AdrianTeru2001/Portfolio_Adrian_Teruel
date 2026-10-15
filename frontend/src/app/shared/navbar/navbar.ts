import { Component, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

/**
 * COMPONENTE: barra de navegación superior.
 *
 * <p>Componentes STANDALINE (Angular 14+): cada componente lleva su propia
 * lista {@code imports} en el decorador; no existen los NgModules. Menos
 * boilerplate y lazy-loading natural.</p>
 *
 * <p>Menú móvil con {@code signal}: un signal es un valor reactivo — al hacer
 * {@code .set(...)}, Angular sabe exactamente qué vistas re-renderizar
 * (en Angular "zoneless" ni siquiera necesita Zone.js).</p>
 */
@Component({
  selector: 'app-navbar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './navbar.html',
  styleUrl: './navbar.scss',
})
export class Navbar {
  // Menú cerrado por defecto; solo cambia en pantallas pequeñas.
  menuAbierto = signal(false);

  /** Cierra el menú tras pulsar un enlace (mejor UX en móvil). */
  alternarMenu(): void {
    this.menuAbierto.update((abierto) => !abierto);
  }

  cerrarMenu(): void {
    this.menuAbierto.set(false);
  }
}
