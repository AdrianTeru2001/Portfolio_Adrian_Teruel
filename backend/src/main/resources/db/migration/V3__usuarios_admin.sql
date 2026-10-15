-- =============================================================================
--  V3__usuarios_admin.sql  —  FASE 2: usuarios administradores (JWT)
-- =============================================================================
--  Nueva tabla para autenticar el panel de administración.
--
--  ¿Por qué una tabla separada de "perfil"?
--  Porque son conceptos distintos: el perfil son datos PÚBLICOS del portfolio;
--  aquí solo viven las credenciales de QUIÉN puede editar. Mezclarlas haría
--  que un endpoint público pudiera filtrar datos de autenticación por accidente.
-- =============================================================================

CREATE TABLE usuarios_admin (
    id            BIGSERIAL PRIMARY KEY,
    -- Username único: será lo que se escriba en el formulario de login.
    username      VARCHAR(60)  NOT NULL UNIQUE,
    -- Contraseña BCRYPT: NUNCA el texto plano. El hash empieza por $2a$/$2b$
    -- e incluye el "cost factor" ($10$ = 2^10 iteraciones). Spring Security
    -- puede verificarlo con BCryptPasswordEncoder.matches(claro, hash).
    password      VARCHAR(100) NOT NULL,
    -- Para deshabilitar un admin sin borrar su fila (auditoría simple).
    activo        BOOLEAN      NOT NULL DEFAULT TRUE,
    -- Auditoría básica: cuándo se creó/último acceso.
    creado_en     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    ultimo_acceso TIMESTAMPTZ
);

-- -----------------------------------------------------------------------------
-- USUARIO ADMIN INICIAL
-- -----------------------------------------------------------------------------
--   username: admin
--   password: AdminPortfolio2026!     ← ⚠️ CAMBIA ESTO antes de desplegar
--   (el hash de abajo corresponde a esa contraseña; se generó con
--    BCryptPasswordEncoder de Spring Security y está verificado)
--
--  On conflict: si el username ya existe, no lo toca (idempotente).
-- -----------------------------------------------------------------------------
INSERT INTO usuarios_admin (username, password)
VALUES ('admin', '$2a$10$iRIs3ECrUGnE3aZKYO0mTeiNK0hqwxIALxnkRYFTtuNYlToIeRwte')
ON CONFLICT (username) DO NOTHING;

-- Para cambiar la contraseña más adelante: genera un nuevo hash con la clase
-- de prueba o desde el panel admin (Fase 4) y ejecuta:
--   UPDATE usuarios_admin SET password = '<nuevo_hash>' WHERE username = 'admin';
