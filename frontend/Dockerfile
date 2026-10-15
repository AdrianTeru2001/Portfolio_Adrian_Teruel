# =============================================================================
#  Dockerfile del frontend (Angular + nginx)
# =============================================================================
#  Build MULTI-STAGE como en el backend:
#
#    Fase 1 (build): imagen con Node 22 → `ng build` genera HTML/CSS/JS
#                    optimizados (minificados, con hashes) en dist/frontend
#    Fase 2 (runtime): nginx puro (~40MB) sirviendo esos ficheros estáticos
#
#  ¿Por qué nginx y no "ng serve"?
#  ng serve es el servidor de DESARROLLO (recarga en caliente, sin optimizar).
#  En producción sirve nginx: es muchísimo más rápido para estáticos y
#  gestiona miles de conexiones simultáneas.
# =============================================================================

# ---------------------------------------------------------------------------
# FASE 1: COMPILACIÓN
# ---------------------------------------------------------------------------
FROM node:22-alpine AS build
# ^ Alpine = variante mínima de Linux (~50MB vs ~350MB de node:normal)

WORKDIR /app

# Copiamos SOLO package*.json + .npmrc primero → capa de caché de Docker:
# si solo cambia código TS, npm install NO se repite (ver Dockerfile backend).
# El .npmrc DEBE estar aquí antes de `npm ci`: npm debe usar la MISMA
# configuración con la que se generó el lock (legacy-peer-deps esquiva el
# bug de npm 10 al resolver peers; sin él el contenedor "resuelve" un árbol
# distinto y npm ci falla con "package.json and lock are in sync").
COPY package*.json .npmrc ./
RUN npm ci
#    ^ "npm ci" = instalación LIMPIA y REPETIBLE a partir del package-lock.json
#      (npm install puede variar versiones según cuándo se ejecute; en CI/CD
#       "ci" garantiza que todos instalan EXACTAMENTE las mismas versiones)

COPY . .
# --configuration production = optimizaciones: minificación, tree-shaking,
# hashes de caché en nombres de fichero (index-ABC123.js)
RUN npx ng build --configuration production

# ---------------------------------------------------------------------------
# FASE 2: SERVIDOR
# ---------------------------------------------------------------------------
FROM nginx:alpine

# Copiamos el build resultante al directorio público de nginx
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
#                                  ^ Angular 17+ genera browser/ dentro de dist/

# Configuración de nginx (SPA routing + gzip) — ver nginx.conf
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

# nginx:alpine ya trae CMD ["nginx", "-g", "daemon off;"] por defecto
