-- =============================================================================
--  V1__schema.sql  —  Esquema de la base de datos
-- =============================================================================
--  FLYWAY: herramienta de "migraciones" versionadas.
--
--  ¿Cómo funciona? Los ficheros de src/main/resources/db/migration deben
--  llamarse:   V<versión>__<descripción>.sql     (nota: DOS guiones bajos)
--
--    V1__schema.sql   → se ejecuta 1º
--    V2__seed.sql     → después
--    V3__otra.sql     → y así sucesivamente
--
--  Flyway guarda en una tabla "flyway_schema_history" qué scripts ya aplicó,
--  así que:
--    - Al arrancar la app, solo ejecuta los que falten.
--    - NUNCA vuelve a ejecutar un script ya aplicado (idempotente).
--    - Si modificas un script ya aplicado, Flyway DETIENE el arranque
--      ("checksum mismatch") → obliga a crear V3 en vez de editar V1.
--      Esa disciplina es lo que hace seguro evolucionar el esquema en equipo.
--
--  ⚠️ UNA VEZ APLICADO, NO SE EDITA NUNCA ESTE FICHERO.
--     Cualquier cambio futuro = nuevo fichero V3__xxx.sql.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- TABLA: perfil (datos personales, una sola fila con id=1)
-- -----------------------------------------------------------------------------
CREATE TABLE perfil (
    id                BIGSERIAL PRIMARY KEY,      -- BIGSERIAL = entero autoincremental (bigint + sequence)
    nombre            VARCHAR(100)  NOT NULL,
    titulo_profesional VARCHAR(150),
    bio               TEXT,                        -- TEXT = ilimitado (biografías largas)
    foto_url          VARCHAR(500),
    email             VARCHAR(150),
    telefono          VARCHAR(30),
    linkedin_url      VARCHAR(300),
    github_url        VARCHAR(300),
    ubicacion         VARCHAR(100)
);

-- -----------------------------------------------------------------------------
-- TABLA: estudios (formación académica)
-- -----------------------------------------------------------------------------
CREATE TABLE estudios (
    id          BIGSERIAL PRIMARY KEY,
    titulo      VARCHAR(200) NOT NULL,
    centro      VARCHAR(200) NOT NULL,
    fecha_inicio DATE        NOT NULL,
    fecha_fin   DATE,                              -- NULL si está en curso
    en_curso    BOOLEAN     NOT NULL DEFAULT FALSE,
    descripcion TEXT,
    orden       INTEGER                          -- prioridad de visualización
);

-- -----------------------------------------------------------------------------
-- TABLA: experiencias (trabajo / prácticas)
-- -----------------------------------------------------------------------------
CREATE TABLE experiencias (
    id          BIGSERIAL PRIMARY KEY,
    puesto      VARCHAR(200) NOT NULL,
    empresa     VARCHAR(200) NOT NULL,
    fecha_inicio DATE        NOT NULL,
    fecha_fin   DATE,
    en_curso    BOOLEAN     NOT NULL DEFAULT FALSE,
    descripcion TEXT,
    orden       INTEGER
);

-- -----------------------------------------------------------------------------
-- TABLA: competencias (skills con nivel)
-- -----------------------------------------------------------------------------
CREATE TABLE competencias (
    id        BIGSERIAL PRIMARY KEY,
    nombre    VARCHAR(100) NOT NULL,
    categoria VARCHAR(30)  NOT NULL,   -- LENGUAJE | FRAMEWORK | HERRAMIENTA | BASE_DE_DATOS | BLANDA
    nivel     INTEGER      NOT NULL CHECK (nivel BETWEEN 1 AND 5),
    --        ^ CHECK = restricción a nivel de BBDD: aunque la API valide,
    --          nadie podría insertar nivel 99 directamente con SQL.
    subgrupo  VARCHAR(100)
);

-- Índice para agrupar/filtrar por categoría con frecuencia (los rankings de
-- skills se consultan mucho en el frontend). Un índice = búsqueda más rápida
-- a cambio de un poco más de espacio y tiempo al escribir.
CREATE INDEX idx_competencias_categoria ON competencias (categoria);

-- -----------------------------------------------------------------------------
-- TABLA: proyectos
-- -----------------------------------------------------------------------------
CREATE TABLE proyectos (
    id          BIGSERIAL PRIMARY KEY,
    nombre      VARCHAR(200) NOT NULL,
    descripcion TEXT,
    tecnologias VARCHAR(300),
    url_repo    VARCHAR(300),
    url_demo    VARCHAR(300),
    imagen_url  VARCHAR(500),
    destacado   BOOLEAN     NOT NULL DEFAULT FALSE,
    fecha       DATE,
    orden       INTEGER
);

-- -----------------------------------------------------------------------------
-- TABLA: mensajes_contacto (formulario público)
-- -----------------------------------------------------------------------------
CREATE TABLE mensajes_contacto (
    id        BIGSERIAL PRIMARY KEY,
    nombre    VARCHAR(100) NOT NULL,
    email     VARCHAR(150) NOT NULL,
    contenido TEXT         NOT NULL,
    fecha     TIMESTAMPTZ  NOT NULL DEFAULT now(),  -- now() = fecha y hora del servidor
    --        ^ TIMESTAMPTZ guarda con zona horaria UTC → inmune a cambios de TZ
    leido     BOOLEAN      NOT NULL DEFAULT FALSE
);

-- NOTA DE SEGURIDAD: este esquema no define usuarios todavía porque la
-- autenticación (tabla usuarios_admin + bcrypt) llega en la Fase 2 con V3.
