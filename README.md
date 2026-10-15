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
- [ ] **Fase 4** — Admin: guard, interceptor, formularios de edición
- [ ] **Fase 5** — Pulido: diseño, responsive, SEO básico
- [ ] **Fase 6** — Tests (JUnit + Mockito / specs de Angular)
- [ ] **Fase 7** — Despliegue real + CI/CD (Render + Vercel)

---

## 🧪 Comandos útiles de Git

```bash
git status                  # qué se ha cambiado
git add .                   # preparar todo para el commit
git commit -m "Fase 0: ..." # guardar una versión con mensaje
git push                    # subirlo a GitHub
```
