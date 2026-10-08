# =============================================================================
#  Dockerfile del backend (Spring Boot)
# =============================================================================
#  Un Dockerfile son las instrucciones para CONSTRUIR una imagen Docker:
#  una "foto" del sistema (app + runtime) que se ejecuta en cualquier
#  máquina sin instalar Java ni Maven.
#
#  Usamos un build MULTI-STAGE en 2 fases:
#
#    Fase 1 (build):        imagen grande con Maven + JDK → compila y empaqueta
#    Fase 2 (runtime):      imagen pequeña SOLO con la JRE + el JAR resultante
#
#  Así la imagen final NO arrastra el código fuente ni las dependencias de
#  compilación → más segura y mucho más ligera (~250MB vs ~700MB).
# =============================================================================

# ---------------------------------------------------------------------------
# FASE 1: COMPILACIÓN
# ---------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build
# ^ Maven + JDK 21 (la misma versión de Java que usa el proyecto)

WORKDIR /app
# ^ Directorio de trabajo dentro del contenedor (como "cd /app")

# Copiamos SOLO el pom.xml primero y descargamos dependencias:
#   "docker layer caching": Docker guarda cada instrucción como capa.
#   Si luego cambias SOLO código (src/), esta capa no se invalida y el
#   `mvn dependency:go-offline` NO se repite → builds mucho más rápidos.
COPY pom.xml .
RUN mvn -B dependency:go-offline -q

# Ahora sí, copiamos el código fuente y compilamos.
COPY src ./src
RUN mvn -B package -DskipTests -q
#    -B      → modo batch (sin prompts, logs limpios)
#    -DskipTests → no ejecuta tests en el build de la imagen
#                   (los tests se ejecutan en CI/CD antes de llegar aquí)

# ---------------------------------------------------------------------------
# FASE 2: EJECUCIÓN
# ---------------------------------------------------------------------------
# eclipse-temurin:21-jre = solo la Máquina Virtual de Java (no el JDK completo)
FROM eclipse-temurin:21-jre

WORKDIR /app

# Etiqueta estándar: describe la imagen (buena práctica)
LABEL maintainer="adrianteru2001@gmail.com" \
      description="API REST del portfolio personal - Spring Boot"

# Usuario SIN privilegios: si un atacante escapa del contenedor, no es root.
RUN useradd --system --no-create-home appuser

# Copiamos SOLO el .jar compilado desde la fase anterior.
# "--from=build" = del stage llamado "build".
COPY --from=build /app/target/*.jar app.jar

# Cambiamos al usuario no privilegiado antes de ejecutar.
USER appuser

# Puerto que expone la aplicación (documentación; el mapeo real es del compose).
EXPOSE 8080

# El flag "-XX:MaxRAMPercentage" permite que la JVM use un % de la RAM del
# contenedor (aquí 75%). "java -jar app.jar" + "$JAVA_OPTS" permite tunear
# la JVM desde variables de entorno sin reconstruir la imagen.
ENTRYPOINT ["sh", "-c", "java -XX:MaxRAMPercentage=75.0 -jar $JAVA_OPTS app.jar"]
