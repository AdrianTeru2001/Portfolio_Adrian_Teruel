# 🅰️ `frontend/` — SPA con Angular

> Esta carpeta está **aún vacía de código**: se rellenará en la **Fase 3**.
> Este README explica qué va a vivir aquí y por qué.

---

## ¿Qué es Angular y por qué lo usamos?

**Angular** es un framework de Google para construir **SPA** (*Single Page
Application*): el navegador carga UNA sola página HTML y a partir de ahí
JavaScript cambia el contenido al navegar, sin recargar. Eso significa que la
mayoría de las llamadas a nuestra API son asíncronas (`fetch`/`HttpClient`).

| Concepto | Explicación breve |
|---|---|
| **Componente** | Pieza de UI con su HTML + CSS + TS (ej: `tarjeta-proyecto.component.ts`) |
| **Servicio** | Clase con lógica reutilizable, normalmente para llamar a la API (`ProyectoService`). Se inyecta con `inject()` o constructor |
| **Módulo / Standalone** | Agrupaciones de componentes. Angular 17+ usa componentes *standalone* (sin NgModules) |
| **Routing** | `app.routes.ts` mapea URL → componente (`/proyectos` → `ListaProyectosPage`) |
| **Guard** | Función que intercepta navegaciones (la usaremos para proteger `/admin`) |
| **Interceptor** | Hook en cada petición HTTP (la usaremos para añadir el `Authorization: Bearer <token>`) |
| **Signals** | Sistema reactivo moderno de Angular: actualiza la vista cuando cambia un estado |

---

## Estructura prevista

```
frontend/
├── Dockerfile                # multi-stage: compila con Node, sirve con nginx (Fase 3)
├── angular.json              # configuración del CLI de Angular
├── package.json              # dependencias npm (el "pom.xml" de Node)
└── src/
    ├── index.html            # la única página HTML que carga el navegador
    ├── main.ts               # punto de arranque de la app
    └── app/
        ├── app.component.*   # raíz (contiene <router-outlet/>)
        ├── app.routes.ts     # definición de rutas
        ├── core/             # servicios, interceptors, guards (singletones)
        │   ├── auth/
        │   └── services/     # PerfilService, ProyectoService...
        ├── features/
        │   ├── public/       # home, sobre-mi, trayectoria, proyectos, contacto
        │   └── admin/        # login + CRUDs (protegido por guard)
        └── shared/           # componentes reutilizables (navbar, footer, tarjeta)
```

**Patrón por feature (carpeta por funcionalidad):** en vez de agrupar por tipo
(`components/`, `services/`), agrupamos por *área de negocio*. Así, si un día
borras la sección de contacto, borras una carpeta y ya está.

---

## Comandos que usaremos

```bash
cd frontend

# Crear el proyecto (solo una vez, Fase 3)
npm install -g @angular/cli        # CLI global (o usar npx)
ng new frontend --style=scss --routing --standalone

# Arrancar en local con recarga en caliente
ng serve          # → http://localhost:4200

# Build de producción (optimizado, minificado)
ng build --configuration production   # → salida en dist/

# Tests
ng test           # Karma + Jasmine
```

---

## Relación con el backend

```
  Angular (localhost:4200)  ──HTTP/JSON──►  Spring Boot (localhost:8080)
        SPA                                        API REST
   solo pinta UI                          valida, persiste, expone datos
```

- El frontend **NUNCA** toca la base de datos directamente: todo pasa por la API.
- Esa separación (front != back) permite: cambiar el frontend sin tocar la API,
  exponer la misma API a una app móvil futura, y aplicar seguridad (JWT) en un
  único punto.

---

## Estado

- [x] Fase 0: carpeta y documentación creadas
- [ ] Fase 3: proyecto Angular, páginas públicas, services
- [ ] Fase 4: guard + interceptor + admin
