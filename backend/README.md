# 📦 `backend/` — API REST con Spring Boot

> Esta carpeta está **aún vacía de código**: se rellenará en la **Fase 1**.
> Este README explica qué va a vivir aquí y por qué, para que cuando empeces
> sepas qué buscar.

---

## ¿Qué es Spring Boot y por qué lo usamos?

**Spring Boot** es un framework Java que simplifica la creación de aplicaciones
"productivas" (aquí, una API REST). Su lema es **"convención sobre
configuración"**: en vez de escribir decenas de ficheros XML para montar un
servidor, incluyes una dependencia y arranca.

| Concepto | Explicación breve |
|---|---|
| **Starter** | Dependencias pre-armadas (`spring-boot-starter-web` trae Tomcat + Jackson + validación, todo listo) |
| **Autoconfiguración** | Spring "adivina" tu setup (si hay PostgreSQL en el classpath, configura el pool de conexiones) |
| **Actuadores / Actuator** | Endpoints de salud (`/actuator/health`), métricas... útiles para comprobar que el servicio vive |
| **Embedded server** | No despliegas un `.war` en un Tomcat externo: el servidor viene dentro del JAR y se ejecuta con `java -jar` |

---

## Estructura prevista

```
backend/
├── Dockerfile                  # instrucciones para contenerizar la app (Fase 1)
├── pom.xml                     # ← manifiesto de Maven: dependencias y build
└── src/
    ├── main/
    │   ├── java/com/adrianteru/portfolio/
    │   │   ├── PortfolioApplication.java   # clase principal con @SpringBootApplication
    │   │   ├── config/       # SecurityConfig, CORS, JWT filter
    │   │   ├── controller/   # @RestController → expone la API HTTP
    │   │   ├── dto/          # objetos de entrada/salida (NUNCA se devuelven entidades)
    │   │   ├── entity/       # clases JPA mapeadas a tablas (@Entity)
    │   │   ├── repository/   # interfaces JpaRepository (consultas SQL declarativas)
    │   │   ├── service/      # lógica de negocio (@Service)
    │   │   └── security/     # JwtService, UserDetails
    │   └── resources/
    │       ├── application.yml        # configuración (puerto, BBDD, JWT)
    │       └── db/migration/          # scripts de Flyway: V1__schema.sql, V2__seed.sql
    └── test/java/                      # tests (JUnit 5 + Mockito)
```

## Las 3 piezas clave que conectan todo

```
   HTTP GET /api/proyectos
          │
          ▼
  ┌──────────────┐   llama   ┌──────────────┐   llama   ┌──────────────┐
  │  Controller  │ ────────► │   Service    │ ────────► │  Repository  │
  │ (recibe/dev │           │ (reglas de   │           │ (SQL hacia   │
  │  ve JSON)    │           │  negocio)    │           │  PostgreSQL) │
  └──────────────┘           └──────────────┘           └──────────────┘
```

1. **Repository** (Spring Data JPA): defines interfaces como
   `interface ProyectoRepository extends JpaRepository<Proyecto, Long>` y
   Spring genera el SQL automáticamente.
2. **Service**: lógica que no es "leer/escribir" (p. ej. validar que un
   proyecto tenga URL válida, o comprobar el JWT).
3. **Controller**: traduce HTTP ↔ llamadas al Service y devuelve JSON.

---

## Comandos que usaremos

```bash
cd backend

# Arrancar en local (recarga caliente al cambiar código)
./mvnw spring-boot:run
# En Windows:  mvnw.cmd spring-boot:run

# Ejecutar tests
./mvnw test

# Empaquetar en JAR ejecutable
./mvnw -DskipTests package
```

> `mvnw` es el **Maven Wrapper**: un pequeño script incluido en el proyecto que
> descarga la versión exacta de Maven que necesitas. Así "si funciona en mi
> máquina, funciona en la tuya" sin instalar Maven globalmente.

---

## Estado

- [x] Fase 0: carpeta y documentación creadas
- [x] Fase 1: proyecto Spring Boot, entidades, Flyway, CRUD — ✅ API verificada (CRUD completo, validación 400, 404, tests en verde)
- [ ] Fase 2: Spring Security + JWT
