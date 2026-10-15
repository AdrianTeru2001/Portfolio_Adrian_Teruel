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
├── app.ts / app.html / app.scss   # shell: navbar + <router-outlet/> + footer
├── app.routes.ts                  # rutas con LAZY LOADING (loadComponent)
├── app.config.ts                  # providers: router, http, signals
├── core/                          # "lo que no es UI"
│   ├── api-url.token.ts           # InjectionToken de la URL de la API
│   ├── models/portfolio.models.ts # interfaces TS = espejo de los DTOs Java
│   └── services/                  # HttpClient → Observables tipados
├── shared/                        # componentes reutilizables
│   ├── navbar/                    # menú responsive con signal
│   └── footer/                    # demo de toSignal()
└── features/                      # una carpeta por página
    ├── home/                      # hero + destacados
    ├── trayectoria/               # timeline + competencias (computed)
    ├── proyectos/                 # buscador + chips (filtro en cliente)
    ├── contacto/                  # formulario con validación (ngModel)
    └── no-encontrado/             # 404
```

## Conceptos clave aplicados (para repasar)

| Concepto | Dónde |
|---|---|
| `@if` / `@for` / `@empty` (control flow) | todos los templates |
| `signal` + `computed` | `proyectos.ts`, `trayectoria.ts` |
| `toSignal(observable)` | `footer.ts`, `home.ts` |
| Lazy loading por ruta | `app.routes.ts` (`loadComponent`) |
| `[(ngModel)]` + validación | `contacto.html` |
| Inyección con `inject()` + token | `core/api-url.token.ts` |
| Zoneless (`provideZonelessChangeDetection`) | `app.config.ts` |

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
- [ ] Fase 4: guard + interceptor + admin
