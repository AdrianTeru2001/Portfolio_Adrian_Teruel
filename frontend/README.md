# 🅰️ `frontend/` — SPA con Angular

Proyecto **Angular 21** (standalone components + signals + zoneless) consumiendo
la API REST del `backend/`.

## Comandos

```bash
cd frontend

# ⚠️ Este proyecto necesita Node ≥ 20.19 (recomendado 22)
#    Con fnm:   eval "$(fnm env)" && fnm use 22

npm install        # solo la primera vez o si cambia package.json
npm start          # ng serve → http://localhost:4200 (recarga en caliente)
npm run build      # build producción → dist/frontend/browser
```

## Estructura (patrón por features)

```
src/app/
├── app.ts / app.html / app.scss   # shell: skip-link + navbar + outlet + footer
├── app.routes.ts                  # rutas con LAZY LOADING (loadComponent)
├── app.config.ts                  # providers: router, http, signals
├── core/                          # "lo que no es UI"
│   ├── api-url.token.ts           # InjectionToken de la URL de la API
│   ├── auth/                      # Fase 4: JWT en el frontend
│   │   ├── auth.service.ts        # login/logout + signal de sesión
│   │   ├── auth.interceptor.ts    # añade Authorization + gestiona 401
│   │   └── admin.guard.ts         # protege /admin (canMatch)
│   ├── http/recurso.ts            # Fase 5: estado cargando/ok/error + recargar()
│   ├── seo/seo.service.ts         # Fase 5: title/description/OG por ruta
│   ├── models/portfolio.models.ts # interfaces TS = espejo de los DTOs Java
│   └── services/                  # HttpClient → Observables tipados
│       ├── admin.service.ts       # escrituras (crear/editar/borrar)
├── shared/                        # componentes reutilizables
│   ├── navbar/                    # menú responsive con signal
│   ├── footer/                    # datos del perfil (patrón recurso)
│   ├── estado-carga/              # Fase 5: skeleton + aviso "Reintentar"
│   └── scroll-top/                # Fase 5: botón "volver arriba"
└── features/                      # una carpeta por página
    ├── home/                      # hero + destacados
    ├── trayectoria/               # timeline + competencias (computed)
    ├── proyectos/                 # buscador + chips (filtro en cliente)
    ├── contacto/                  # formulario con validación (ngModel)
    ├── no-encontrado/             # 404
    └── admin/                     # panel privado (Fase 4, tras login)
        ├── login/                 # formulario de acceso (POST /auth/login)
        ├── layout/                # shell admin (navbar + <router-outlet/>)
        ├── dashboard/             # resumen y accesos
        ├── proyectos-admin/       # CRUD completo (tabla + form crear/editar)
        └── perfil-admin/          # edición del perfil (PUT /perfil)

frontend/public/                   # se copia tal cual a dist/ (sin compilar)
├── robots.txt                     # qué puede rastrear Google (+ Disallow /admin)
├── sitemap.xml                    # mapa del sitio (cambia el dominio al desplegar)
└── favicon.svg                    # favicon de marca (iniciales AT)
```

## Conceptos clave aplicados (para repasar)

| Concepto | Dónde |
|---|---|
| `@if` / `@for` / `@empty` (control flow) | todos los templates |
| `signal` + `computed` | `proyectos.ts`, `trayectoria.ts` |
| **Estado HTTP con `signal` (cargando/ok/error)** | `core/http/recurso.ts` |
| **Sobrecargas de función para inferir tipos** | `core/http/recurso.ts` |
| `input()` / `output()` funcionales | `estado-carga.ts` |
| Lazy loading por ruta | `app.routes.ts` (`loadComponent`) |
| `[(ngModel)]` + validación | `contacto.html` |
| Inyección con `inject()` + token | `core/api-url.token.ts` |
| Zoneless (`provideZonelessChangeDetection`) | `app.config.ts` |
| **Meta tags dinámicas (`Meta` + `Title`)** | `core/seo/seo.service.ts` |
| **JSON-LD (Schema.org Person)** | `src/index.html` |
| **`@HostListener('window:scroll')`** | `shared/scroll-top/scroll-top.ts` |
| **Accesibilidad: skip-link y `role="status"`** | `app.html`, `estado-carga.html` |
| **`prefers-reduced-motion` (CSS)** | `styles.scss` |
| **JWT: service + signal de sesión** | `core/auth/auth.service.ts` |
| **Interceptor funcional (`withInterceptors`)** | `core/auth/auth.interceptor.ts` |
| **Guard `canMatch`** | `core/auth/admin.guard.ts` |
| **Rutas anidadas + lazy loading** | `app.routes.ts` (bloque `/admin`) |
| **CRUD con mutación local de signals** | `proyectos-admin.ts` |

## Docker

```bash
docker compose up -d --build   # desde la raíz: nginx sirve dist/ en :4200
```

El `Dockerfile` es multi-stage (node:22 compila → nginx sirve) y `nginx.conf`
incluye el fallback SPA (`try_files ... /index.html`) + gzip.

## Estado

- [x] Fase 0: carpeta y documentación creadas
- [x] Fase 3: proyecto Angular, páginas públicas, services — ✅ verificado:
      build OK (lazy chunks), las 5 rutas responden 200, nginx sirve con
      fallback SPA y gzip, CORS correcto, pila Docker completa (3 servicios)
- [x] Fase 4: panel admin — ✅ verificado:
      login (200/401), CRUD proyectos (201+Location / 200 / 204 / 404),
      PUT perfil (200), guard redirige a /admin/login sin sesión,
      interceptor añade `Authorization`, rutas admin responden 200
- [x] Fase 5: pulido — ✅ verificado:
      build de producción limpio (289 kB raw / 81.5 kB transferidos,
      sin errores ni avisos), `robots.txt` + `sitemap.xml` + `favicon.svg`
      servidos con 200 por nginx, JSON-LD presente en el HTML,
      las 6 rutas responden 200 (fallback SPA),
      todos los GET públicos de la API → 200 y POST sin token → 401
