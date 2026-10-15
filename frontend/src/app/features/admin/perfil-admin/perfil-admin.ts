import { Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { PerfilService } from '../../../core/services/perfil.service';
import { AdminService } from '../../../core/services/admin.service';
import { PerfilUpdateRequest } from '../../../core/models/portfolio.models';

/**
 * PÁGINA: Edición del perfil personal (PUT /api/perfil).
 *
 * <p>Patrón: cargamos el perfil actual con `toSignal` para RellenAR el
 * formulario, y al guardar enviamos un PUT con todos los campos (el backend
 * usa PUT completo, no PATCH parcial).</p>
 *
 * <p>Diferencia con "proyectos-admin": aquí NO hay lista ni borrado — solo
 * UN recurso (el perfil) que se edita. Es más simple.</p>
 */
@Component({
  selector: 'app-perfil-admin',
  imports: [FormsModule],
  templateUrl: './perfil-admin.html',
  styleUrl: './perfil-admin.scss',
})
export class PerfilAdmin {

  private perfilService = inject(PerfilService);
  private adminService = inject(AdminService);

  /** Perfil actual desde la API (para rellenar el form). */
  perfil = toSignal(this.perfilService.getPerfil(), {
    initialValue: null,
  });

  /** Formulario editable. Se inicializa con `inicializarForm()`. */
  form = signal<PerfilUpdateRequest>(PerfilAdmin.formVacio());

  /** Feedback éxito/error. */
  mensaje = signal<{ tipo: 'ok' | 'mal'; texto: string } | null>(null);
  estado = signal<'idle' | 'guardando'>('idle');

  constructor() {
    // Cuando el perfil llega de la API, copiamos sus valores al formulario.
    // Como `perfil` es un signal, podemos leerlo en un effect… pero aquí
    // basta con suscribirnos una vez al cargar (es un GET único).
    this.perfilService.getPerfil().subscribe({
      next: (p) => {
        if (p) {
          this.form.set({
            nombre: p.nombre,
            tituloProfesional: p.tituloProfesional,
            bio: p.bio,
            fotoUrl: p.fotoUrl,
            email: p.email,
            telefono: p.telefono,
            linkedinUrl: p.linkedinUrl,
            githubUrl: p.githubUrl,
            ubicacion: p.ubicacion,
          });
        }
      },
    });
  }

  /** Actualiza un campo del formulario desde el template. */
  cambiarCampo(campo: keyof PerfilUpdateRequest, valor: unknown): void {
    this.form.update((f) => ({ ...f, [campo]: valor }));
  }

  /** Envía el PUT con todos los campos. */
  guardar(): void {
    if (this.estado() === 'guardando') {
      return;
    }
    this.estado.set('guardando');

    this.adminService.actualizarPerfil(this.form()).subscribe({
      next: (actualizado) => {
        this.estado.set('idle');
        this.mostrarMensaje('ok', 'Perfil actualizado correctamente.');
        // Nota: no recargamos la web pública aquí; el usuario la verá
        // actualizada al navegar (Angular recoge el nuevo GET).
      },
      error: (err) => {
        console.error('Error al guardar perfil', err);
        this.estado.set('idle');
        this.mostrarMensaje('mal', 'No se pudo guardar. Revisa los campos.');
      },
    });
  }

  /** Modelo por defecto (perfil "en blanco"). */
  private static formVacio(): PerfilUpdateRequest {
    return {
      nombre: '',
      tituloProfesional: null,
      bio: null,
      fotoUrl: null,
      email: '',
      telefono: null,
      linkedinUrl: null,
      githubUrl: null,
      ubicacion: null,
    };
  }

  private mostrarMensaje(tipo: 'ok' | 'mal', texto: string): void {
    this.mensaje.set({ tipo, texto });
    setTimeout(() => this.mensaje.set(null), 4000);
  }
}
