import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PerfilService } from '../../core/services/perfil.service';
import { ContactoService } from '../../core/services/contacto.service';
import { recurso } from '../../core/http/recurso';
import { MensajeContactoRequest, Perfil } from '../../core/models/portfolio.models';

/**
 * PÁGINA: Contacto (formulario).
 *
 * <p>Dos enfoques de formularios en Angular:</p>
 * <ul>
 *   <li><b>Template-driven (FormsModule)</b> → lo usamos aquí: más simple,
 *       ideal para formularios pequeños. Validaciones en el HTML
 *       (required, minlength, pattern...).</li>
 *   <li><b>Reactive (ReactiveFormsModule)</b> → control desde la clase TS;
 *       mejor para formularios complejos/validaciones cruzadas.</li>
 * </ul>
 *
 * <p>Estados del envío (signal 'estado'):</p>
 * <pre>
 *   idle → enviando → exito | error
 * </pre>
 * El backend es la ÚLTIMA línea de validación: aunque el HTML valide,
 * él devuelve 400 si algo no cuadra (defensa en profundidad).
 */
@Component({
  selector: 'app-contacto',
  imports: [FormsModule],
  templateUrl: './contacto.html',
  styleUrl: './contacto.scss',
})
export class Contacto {
  private perfilService = inject(PerfilService);
  private contactoService = inject(ContactoService);

  // Datos de contacto del perfil (recurso con estado, tolerante a fallos)
  perfil = recurso<Perfil>(() => this.perfilService.getPerfil());

  /** Modelo enlazado con [(ngModel)] — dos vías de datos. */
  mensaje: MensajeContactoRequest = { nombre: '', email: '', contenido: '' };

  /** Estado del envío para condicionar la UI (botón deshabilitado, avisos). */
  estado = signal<'idle' | 'enviando' | 'exito' | 'error'>('idle');

  enviar(): void {
    // Guardia: si el formulario no es válido según el HTML, no enviamos.
    // (form ngForm lo exponemos al template con #form="ngForm")
    if (this.estado() === 'enviando') {
      return;   // evita doble envío mientras espera la respuesta
    }

    this.estado.set('enviando');

    // .subscribe con observers separados: next (éxito) y error (fallo HTTP)
    this.contactoService.enviar(this.mensaje).subscribe({
      next: () => {
        this.estado.set('exito');
        // Reinicia el formulario para el siguiente mensaje
        this.mensaje = { nombre: '', email: '', contenido: '' };
      },
      error: (err) => {
        console.error('Error al enviar mensaje', err);
        this.estado.set('error');
      },
    });
  }
}
