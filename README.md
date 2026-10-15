# 🎯 Portfolio_Adrian_Teruel

Portfolio personal full-stack de **Adrián Teruel Reina**: datos personales,
estudios, experiencia, competencias y proyectos, con panel de administración
protegido por JWT. Pensado como carta de presentación para recruiters y, a la
vez, como proyecto técnico que incluir en el CV.

---

## 🧰 Stack tecnológico

| Capa | Tecnología | Motivo |
|---|---|---|
| Backend | **Java 21 + Spring Boot 3** | API REST robusta, ecosistema estándar en empresas |
| Seguridad | **Spring Security + JWT** | Autenticación stateless para el panel admin |
| Persistencia | **Spring Data JPA + PostgreSQL** | ORM declarativo + BBDD relacional fiable |
| Migraciones | **Flyway** | Versionado del esquema y seeds reproducibles |
| Frontend | **Angular 17+ (standalone)** | SPA moderna con Signals y routing |
| Contenedores | **Docker + Docker Compose** | Entorno idéntico en cualquier máquina |
| CI/CD | **GitHub Actions** | Tests y despliegue automático al hacer push |

---

## 📁 Estructura del proyecto

```
Portfolio_Adrian_Teruel/
├── backend/             # API REST (Spring Boot)          → Fase 1-2
├── frontend/            # SPA (Angular)                   → Fase 3-4
├── docker-compose.yml   # Orquesta PostgreSQL (+ más adelante backend y frontend)
├── .env.example         # Plantilla de variables de entorno (los valores reales van en .env)
├── .gitignore
└── README.md            # Este fichero
```

---

## 🚀 Puesta en marcha

```bash
# 1. Crear tu copia local de las variables de entorno
cp .env.example .env        # en Windows: copy .env.example .env

# 2. Arrancar la pila completa (PostgreSQL + API + frontend nginx)
docker compose up -d --build

# 3. Comprobar que viven
docker compose ps           # los 3 servicios; postgres debe decir "healthy"
docker compose logs -f      # Ctrl+C para salir del log

# 4. Pararlo cuando termines (los datos se conservan en el volumen)
docker compose down

# ⚠️ docker compose down -v   →  BORRA el volumen = pierdes los datos
```

Una vez levantado:

| Servicio | URL |
|---|---|
| Frontend (nginx) | http://localhost:4200 |
| API REST | http://localhost:8080/api |
| PostgreSQL | `localhost:5433` (⚠️ en este equipo el 5432 lo ocupa un PG nativo) |

### 💻 Desarrollo del frontend (Fase 3)

Para iterar con recarga en caliente (sin reconstruir Docker):

```bash
cd frontend
# ⚠️ Requiere Node ≥ 20.19 (recomendado 22). Con fnm:
eval "$(fnm env)" && fnm use 22

npm install    # solo la primera vez
npm start      # → http://localhost:4200 (recarga en caliente)
```

Conéctate a la BBDD con DBeaver/pgAdmin: host `localhost`, puerto `5433`,
usuario `portfolio_user`, contraseña `portfolio_password`, BBDD `portfolio`.

### API REST (Fase 1)

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/perfil` | Datos personales |
| GET | `/api/estudios` | Timeline académica |
| GET | `/api/experiencias` | Timeline laboral |
| GET | `/api/competencias` | Skills por categoría |
| GET | `/api/proyectos` | Todos (`?tecnologia=x` filtra) |
| GET | `/api/proyectos/destacados` | Destacados para la home |
| **POST** | **`/api/auth/login`** | **`{username, password}` → `{token, type, expiresIn}`** |
| POST/PUT/DELETE | `/api/proyectos`, `/api/perfil`... | CRUD — **requiere `Authorization: Bearer <token>`** (401 sin él) |
| POST | `/api/contacto` | Formulario de contacto (público) |

**Credenciales del panel admin (seed V3):** usuario `admin`, contraseña
`AdminPortfolio2026!` — ⚠️ **cámiala antes de desplegar** (ver
`V3__usuarios_admin.sql`).

**Ejemplo de uso del token:**

```bash
# 1. Login
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"AdminPortfolio2026!"}' \
  | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# 2. Llamada autenticada
curl -X POST http://localhost:8080/api/proyectos \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Mi proyecto","destacado":false}'
```

---

## 🗺️ Roadmap de fases

- [x] **Fase 0** — Estructura del repo, Docker Compose con PostgreSQL, documentación
- [x] **Fase 1** — Backend: entidades, Flyway + seeds, CRUD + DTOs
- [x] **Fase 2** — Seguridad: JWT, login, endpoints protegidos
- [x] **Fase 3** — Frontend: páginas públicas consumiendo la API (Angular 21, signals, lazy loading, Docker/nginx)
- [x] **Fase 4** — Admin: guard, interceptor, login JWT y CRUD de proyectos + edición de perfil
- [x] **Fase 5** — Pulido: estados de carga/error, SEO estructurado, accesibilidad, detalles UX
- [ ] **Fase 6** — Tests (JUnit + Mockito / specs de Angular)
- [ ] **Fase 7** — Despliegue real + CI/CD (Render + Vercel)

### Detalle de la Fase 5 (pulido)

| Área | Qué se hizo | Dónde |
|------|-------------|-------|
| **Robustez UX** | Patrón `recurso()` → cada petición expone `cargando / ok / error` + `recargar()`. Antes, si la API caía, la web se quedaba en "Cargando…" eterno sin aviso. | `frontend/src/app/core/http/recurso.ts` |
| **Skeletons** | Componente reutilizable `<app-estado-carga>` con *skeleton screens* (bloques con brillo viajero) mientras carga y aviso + botón **Reintentar** si falla. | `frontend/src/app/shared/estado-carga/` |
| **SEO estructurado** | JSON-LD (Schema.org `Person`) con tus datos reales y enlaces a LinkedIn/GitHub → puede desencadenar la "ficha de persona" en Google. | `frontend/src/index.html` |
| **Meta tags por ruta** | `SeoService` que al navegar actualiza `<title>`, `description` y `og:title/og:description` (una SPA sirve el mismo HTML para todas las rutas, hay que actualizarlo con JS). | `frontend/src/app/core/seo/seo.service.ts` |
| **Rastreo** | `robots.txt` (con `Disallow: /admin`) y `sitemap.xml` con las 4 páginas públicas. | `frontend/public/` |
| **noindex admin** | El área `/admin` declara `data: { robots: 'noindex' }` → no aparece en buscadores. Se fusiona el `data` de todo el árbol de rutas. | `frontend/src/app/app.routes.ts` |
| **Accesibilidad** | *Skip-link* ("Saltar al contenido"), `scope="col"` + `<caption>` en la tabla admin, clase `.visually-hidden`, anuncio `role="status"` en cargas. | `app.html`, tablas, `styles.scss` |
| **Transición de rutas** | Animación de entrada CSS al cambiar de página (sin módulo `@angular/animations`), respetando `prefers-reduced-motion`. | `frontend/src/app/app.scss` |
| **Botón "volver arriba"** | Flotante que aparece tras bajar ~400 px, con `@HostListener('window:scroll')`. | `frontend/src/app/shared/scroll-top/` |
| **Favicon de marca** | SVG con las iniciales **AT** y el gradiente de la marca (~1 KB, nítido en Retina) en vez del `.ico` por defecto. | `frontend/public/favicon.svg` |
| **Responsive** | Corregido un *overflow horizontal* real en pantallas de 320-360 px: `minmax(300px,1fr)` → `minmax(min(300px,100%),1fr)` en los 4 grids. | `home`, `proyectos`, `trayectoria`, `estado-carga` |

---

## 🧪 Comandos útiles de Git

```bash
git status                  # qué se ha cambiado
git add .                   # preparar todo para el commit
git commit -m "Fase 0: ..." # guardar una versión con mensaje
git push                    # subirlo a GitHub
```
