-- =============================================================================
--  V2__seed.sql  —  Datos iniciales (seed / "siembra")
-- =============================================================================
--  Carga de ejemplo para que la API devuelva datos desde el primer arranque.
--
--  ⚠️  EDITA ESTOS DATOS CON LOS TUYOS reales (o borra y carga desde el panel
--  admin cuando esté listo en la Fase 4). Son de ejemplo a propósito.
--
--  Estrategia idempotente: usamos ON CONFLICT DO NOTHING / IF NOT EXISTS para
--  que, aunque este script se ejecutara dos veces, no duplique datos.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- PERFIL (una única fila, id=1)
-- -----------------------------------------------------------------------------
INSERT INTO perfil (id, nombre, titulo_profesional, bio, foto_url, email,
                    telefono, linkedin_url, github_url, ubicacion)
VALUES (1,
        'Adrián Teruel Reina',
        'Desarrollador Full Stack Java / Angular',
        'Trabajador de desarrollo de aplicaciones con pasión por seguir aprendiendo y construir ' ||
        'productos completos: desde la base de datos hasta la interfaz de usuario. ' ||
        'Busco oportunidades profesionales para seguir creciendo.',
        NULL,                                -- foto_url: pon aquí la ruta/foto cuando la tengas
        'adrianteru2001@gmail.com',
        NULL,
        'https://www.linkedin.com/in/adri%C3%A1n-teruel-reina-43841b1b9/',
        'https://github.com/AdrianTeru2001',
        'España')
ON CONFLICT (id) DO NOTHING;
-- ^ Si ya existía la fila con id=1, no la toca (permite re-ejecutar sin daño)

-- -----------------------------------------------------------------------------
-- ESTUDIOS (ejemplo)
-- -----------------------------------------------------------------------------
INSERT INTO estudios (titulo, centro, fecha_inicio, fecha_fin, en_curso, descripcion, orden) VALUES
    ('Grado Superior en Desarrollo de Aplicaciones Web',
     'IES Ruíz Gijón',
     DATE '2020-09-01', DATE '2023-06-20', FALSE,
     'Formación en desarrollo web: Java, bases de datos, JavaScript y metodologías ágiles.', 1);

-- -----------------------------------------------------------------------------
-- EXPERIENCIAS (ejemplo — sustituye por las tuyas o déjalas vacías)
-- -----------------------------------------------------------------------------
INSERT INTO experiencias (puesto, empresa, fecha_inicio, fecha_fin, en_curso, descripcion, orden) VALUES
    ('Desarrollador Full Stack',
     'Deloitte',
     DATE '2023-03-25', DATE '2025-02-15', FALSE,
     'PROYECTO RENFE - Resolución de incidencias, nuevos desarrollos, mantenimientos... ' || 
     'Tecnologías: Java, JavaScript, Spring, CSS, BootStrap, Maven, JQuery, SQL, Git, HTML, JSP...', 2),
     ('Desarrollador de Backend',
     'Ayesa',
     DATE '2025-02-16', DATE '2026-09-10', TRUE,
     'Proyecto HERMES - Soporte técnico, resolución de incidencias y defectos, migración de una aplicación legacy a una nueva plataforma web y desarrollo de nuevas funcionalidades. ' ||
     'Tecnologías: Java, SpringBoot, Oracle SQL, Git, Angular...', 1);

-- -----------------------------------------------------------------------------
-- COMPETENCIAS (ejemplo — ajusta niveles a tu realidad)
-- -----------------------------------------------------------------------------
INSERT INTO competencias (nombre, categoria, nivel, subgrupo) VALUES
    ('Java',            'LENGUAJE',     4, 'Backend'),
    ('SQL',             'LENGUAJE',     4, 'Backend'),
    ('TypeScript',      'LENGUAJE',     3, 'Frontend'),
    ('HTML/CSS',        'LENGUAJE',     4, 'Frontend'),
    ('JavaScript',      'LENGUAJE',     3, 'Frontend'),
    ('Spring Boot',     'FRAMEWORK',    4, 'Backend'),
    ('Angular',         'FRAMEWORK',    3, 'Frontend'),
    ('Hibernate / JPA', 'FRAMEWORK',    3, 'Backend'),
    ('Git',             'HERRAMIENTA',  4, NULL),
    ('Docker',          'HERRAMIENTA',  2, NULL),
    ('IntelliJ IDEA',   'HERRAMIENTA',  4, NULL),
    ('PostgreSQL',      'BASE_DE_DATOS', 4, NULL),
    ('Trabajo en equipo','BLANDA',      4, NULL),
    ('Comunicación',    'BLANDA',       4, NULL);

-- -----------------------------------------------------------------------------
-- PROYECTOS (ejemplo — sustituye por los tuyos reales con links a GitHub)
-- -----------------------------------------------------------------------------
INSERT INTO proyectos (nombre, descripcion, tecnologias, url_repo, url_demo,
                       imagen_url, destacado, fecha, orden) VALUES
    ('Portfolio Personal',
     'API REST y SPA para mostrar mis datos, estudios y proyectos. Spring Boot + Angular + PostgreSQL con Docker.',
     'Spring Boot, Angular, PostgreSQL, Docker',
     'https://github.com/AdrianTeru2001/Portfolio_Adrian_Teruel',
     NULL, NULL, TRUE, DATE '2026-10-08', 1);
