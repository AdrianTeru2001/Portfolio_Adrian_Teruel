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

## 🚀 Puesta en marcha (por ahora, solo la BBDD)

```bash
# 1. Crear tu copia local de las variables de entorno
cp .env.example .env        # en Windows: copy .env.example .env

# 2. Arrancar PostgreSQL
docker compose up -d

# 3. Comprobar que vive
docker compose ps           # debe indicar "healthy"
docker compose logs -f      # Ctrl+C para salir del log

# 4. Pararlo cuando termines (los datos se conservan en el volumen)
docker compose down

# ⚠️ docker compose down -v   →  BORRA el volumen = pierdes los datos
```

Conéctate a la BBDD con DBeaver/pgAdmin: host `localhost`, puerto `5432`,
usuario `portfolio_user`, contraseña `portfolio_password`, BBDD `portfolio`.

---

## 🗺️ Roadmap de fases

- [x] **Fase 0** — Estructura del repo, Docker Compose con PostgreSQL, documentación
- [ ] **Fase 1** — Backend: entidades, Flyway + seeds, CRUD + DTOs
- [ ] **Fase 2** — Seguridad: JWT, login, endpoints protegidos
- [ ] **Fase 3** — Frontend: páginas públicas consumiendo la API
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
