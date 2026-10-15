/**
 * ============================================================================
 *  MODELOS DEL DOMINIO
 * ============================================================================
 *  Interfaces TypeScript que "espejan" los DTOs/entidades que devuelve la API.
 *
 *  ¿Para qué sirve si TypeScript ya infiere tipos del JSON?
 *  - Documentación: leer `Proyecto` es más claro que un JSON suelto.
 *  - Seguridad de tipos: si el backend renombra un campo, AQUÍ te enteras
 *    (error en compilación) en vez de un `undefined` en producción.
 *  - Autocompletado en todos los componentes.
 *
 *  Nota: los campos opcionales (`?`) coinciden con columnas nullable del backend.
 * ============================================================================
 */

/** Devuelve GET /api/perfil */
export interface Perfil {
  id: number;
  nombre: string;
  tituloProfesional: string | null;
  bio: string | null;
  fotoUrl: string | null;
  email: string;
  telefono: string | null;
  linkedinUrl: string | null;
  githubUrl: string | null;
  ubicacion: string | null;
}

/** Devuelve GET /api/estudios */
export interface Estudio {
  id: number;
  titulo: string;
  centro: string;
  fechaInicio: string;   // "2023-09-01" (ISO date)
  fechaFin: string | null;
  enCurso: boolean;
  descripcion: string | null;
  orden: number | null;
}

/** Devuelve GET /api/experiencias */
export interface Experiencia {
  id: number;
  puesto: string;
  empresa: string;
  fechaInicio: string;
  fechaFin: string | null;
  enCurso: boolean;
  descripcion: string | null;
  orden: number | null;
}

/**
 * Categorías de competencias.
 * USAMOS UN UNION TYPE en vez de un enum: en TypeScript un string literal
 * ya valida en compilación ("LENGUAJE" | "FRAMEWORK" | ...) y es más ligero
 * que un enum (que genera código). Si el backend añade una categoría nueva,
 * solo hay que ampliarla aquí.
 */
export type Categoria = 'LENGUAJE' | 'FRAMEWORK' | 'HERRAMIENTA'
                      | 'BASE_DE_DATOS' | 'BLANDA';

/** Devuelve GET /api/competencias */
export interface Competencia {
  id: number;
  nombre: string;
  categoria: Categoria;
  nivel: number;         // 1..5
  subgrupo: string | null;
}

/** Devuelve GET /api/proyectos */
export interface Proyecto {
  id: number;
  nombre: string;
  descripcion: string | null;
  tecnologias: string | null;
  urlRepo: string | null;
  urlDemo: string | null;
  imagenUrl: string | null;
  destacado: boolean;
  fecha: string | null;
}

/** Cuerpo que envía POST /api/contacto (validado también por el backend) */
export interface MensajeContactoRequest {
  nombre: string;
  email: string;
  contenido: string;
}
