# MVP Gestión de Proyectos y Tareas — CODESA

## Resumen Arquitectónico

Esta solución es un **MVP completo de gestión de proyectos y tareas** con autenticación JWT, compuesta por:

- **3 microservicios backend** independientes (Java 21 + Spring Boot 3.4.5 + Spring Cloud 2024.0.1)
- **1 frontend Angular 20** (standalone components, interceptor, guard)
- **PostgreSQL compartido** con Flyway para migraciones por servicio
- **API Gateway** como único punto de entrada

```
┌─────────────────────────────────────────────────────────────────┐
│                    ANGULAR 20 (:4200)                           │
│               (Login, Projects, Tasks)                          │
└────────────────────────────┬────────────────────────────────────┘
                             │ Solo GET/POST/PUT/DELETE
                             │ Bearer JWT token
                             ▼
┌────────────────────────────────────────────────────────────────┐
│           API-GATEWAY (:8080, WebFlux)                         │
│  - GlobalFilter: valida JWT HS256                             │
│  - Inyecta X-Auth-User y X-Auth-Roles                         │
│  - Rutas: /auth/** → :8081 | /api/** → :8082                │
└──────────┬─────────────────────────────────┬──────────────────┘
           │                                 │
    /auth/login                      /api/projects
    /auth/register                   /api/projects/{id}/tasks
    /auth/profile                    
           │                                 │
           ▼                                 ▼
┌─────────────────────┐         ┌────────────────────────┐
│   API-AUTH (:8081)  │         │ API-PROYECTOS (:8082)  │
│                     │         │                        │
│ - Usuarios          │         │ - Proyectos            │
│ - Login (emite JWT) │         │ - Tareas               │
│ - Perfil            │         │ - Filtro por cabeceras │
│ - BCrypt            │         │ - Reglas: 403, ARCHIVED│
└──────────┬──────────┘         └────────────┬───────────┘
           │                                 │
           └────────────┬────────────────────┘
                        │
                        ▼
              ┌──────────────────────┐
              │  PostgreSQL (:5432)  │
              │  mydatabase          │
              │  (users, proyectos,  │
              │   tareas)            │
              └──────────────────────┘
```

## Stack Obligatorio
- ✅ Java 21 + Spring Boot 3.4.5
- ✅ Angular 20 (standalone components, interceptor, guard)
- ✅ PostgreSQL 16 con Flyway
- ✅ JWT HS256 con validación en el borde (gateway)
- ✅ BCrypt para contraseñas

## Arquitectura de Responsabilidades

### api-auth (:8081)
- **Dominio:** usuarios y autenticación
- **Dueño de:** tabla `users`, JWT (emisión)
- **Endpoints públicos:** `/auth/login`, `/auth/register`
- **Endpoints protegidos:** `/auth/profile` (lee `X-Auth-User` del gateway)
- **Flujo:** GET /auth/login → valida BCrypt → emite JWT HS256 → cliente almacena en localStorage

### api-gateway (:8080)
- **Rol:** único punto de entrada, proxy, validación de tokens
- **Sin BD:** stateless, reactivo (WebFlux)
- **GlobalFilter:** intercambia todo menos `/auth/login` y `/auth/register`
  - Extrae `Authorization: Bearer <token>`
  - Valida con `JwtService` (HS256)
  - Si OK: inyecta `X-Auth-User` y `X-Auth-Roles`, elimina `Authorization`
  - Si error: **401**
- **CORS:** permite origen `http://localhost:4200`

### api-proyectos (:8082)
- **Dominio:** proyectos y tareas
- **Dueño de:** tablas `proyectos`, `tareas`
- **Autenticación:** `HeaderAuthenticationFilter` (construye `Authentication` desde cabeceras)
- **Regla de negocio clave (403):** un `USER` accede SOLO a sus proyectos; `ADMIN` a todos
  - Validación en `ProyectoService.obtener()` / `actualizar()` / etc.
  - Si acceso denegado → `AccessDeniedException` → **403**
- **Regla de negocio clave (409 Conflict):** no crear tareas en proyectos `ARCHIVED`
  - Validación en `TareaService.crear()`: si `proyecto.estado == ARCHIVED` → error 409

## Pre-requisitos

- Java 21 (OpenJDK o similar)
- Node.js 22+ con npm
- Docker + Docker Compose (para opción "todo en contenedores")
- Angular CLI 20 (`npm install -g @angular/cli`)

## Instalación y Ejecución

### Opción A: Todo en Docker Compose (recomendado para demo y entrega)

Desde la raíz `PRUEBA_CODESA/`:
```bash
docker compose up --build
```

**Qué sucede:**
- PostgreSQL (:5432) levanta con healthcheck
- api-auth (:8081) espera a que PostgreSQL esté listo, luego inicia con profile `docker`
- api-proyectos (:8082) espera a que PostgreSQL esté listo, luego inicia
- api-gateway (:8080) espera a que auth y proyectos estén listos, luego inicia

**Comunicación interna (dentro de Docker):**
- api-auth → postgresql://postgres:5432/codesa_prueba (nombre del servicio, no localhost)
- api-proyectos → postgresql://postgres:5432/codesa_prueba
- api-gateway → http://api-auth:8081 y http://api-proyectos:8082 (nombres de servicios, no localhost)

Todos en la red `codesa-network` donde pueden comunicarse por nombre de servicio.

Luego, en otra terminal (para el frontend):
```bash
cd frontend
npm install
ng serve
# Abre http://localhost:4200
```

Para parar y limpiar todo:
```bash
docker compose down -v  # -v elimina volúmenes (BD)
```

#### 2. Compilar y ejecutar los servicios backend
Desde la raíz `PRUEBA_CODESA/`:

```bash
# Terminal 1: api-auth
cd api-auth
./gradlew bootRun
# Flyway aplica V1__users.sql (seed de admin/admin123 y user/user123)
# Escucha en :8081

# Terminal 2: api-proyectos
cd api-proyectos
./gradlew bootRun
# Flyway aplica V1__proyectos_tareas.sql (demo data)
# Escucha en :8082

# Terminal 3: api-gateway
cd api-gateway
./gradlew bootRun
# Escucha en :8080
```

### 3. Instalar y ejecutar Angular
```bash
cd frontend
npm install
ng serve
# Abre http://localhost:4200
```

## Credenciales de Prueba

**ADMIN:**
- Usuario: `admin`
- Contraseña: `admin123`
- Rol: `ADMIN` (accede a todos los proyectos)

**USER:**
- Usuario: `user`
- Contraseña: `user123`
- Rol: `USER` (accede solo a proyectos propios)

## Flujo Funcional

### 1. Login
1. Acceder a `http://localhost:4200`
2. Form de login → `POST :8080/auth/login` (sin token, es público)
3. Gateway reenvía a `:8081/auth/login` sin validar
4. api-auth valida BCrypt, emite JWT HS256, devuelve `{ token, expiresInMs, ... }`
5. Frontend almacena token en `localStorage`
6. Redirige a `/projects`

### 2. Listado de Proyectos
1. Angular envía `GET :8080/api/projects` con header `Authorization: Bearer <token>`
2. Gateway:
   - Extrae token, valida con `JwtService`
   - Inyecta `X-Auth-User: user` (o `admin`) y `X-Auth-Roles: USER` (o `ADMIN`)
   - Reenvía a `:8082/api/projects`
3. api-proyectos:
   - `HeaderAuthenticationFilter` lee cabeceras, construye `Authentication`
   - `ProyectoService.listar()`: si es USER, filtra solo `owner == user`; si ADMIN, todos
   - Devuelve lista de proyectos

### 3. Crear Proyecto
1. Angular: `POST :8080/api/projects { nombre, descripcion }`
2. Gateway valida + inyecta cabeceras → `:8082/api/projects`
3. api-proyectos: `ProyectoService.crear()` asigna `owner = X-Auth-User`, estado `ACTIVE`

### 4. Crear Tarea (Demostración de Regla ARCHIVED)
1. Archivar un proyecto: `PUT :8080/api/projects/{id}/archive`
2. Intentar crear tarea: `POST :8080/api/projects/{id}/tasks`
3. api-proyectos: `TareaService.crear()` detecta `proyecto.estado == ARCHIVED`
4. Lanza error 409 → Angular muestra "No se pueden crear tareas en proyectos archivados"

### 5. Acceso Indebido (Demostración de Regla 403)
1. USER intenta acceder a proyecto de otro usuario: `GET :8080/api/projects/{other-id}`
2. api-proyectos: `ProyectoService.obtener()` valida propiedad
3. Si no es dueño ni ADMIN → `AccessDeniedException` → **403 Forbidden**
4. Angular muestra error

## Limitaciones y Mejoras Futuras

### Limitaciones del MVP
- **Logs:** logging básico; sin trazabilidad auditada
- **Tests:** solo context loads; sin cobertura unitaria completa
- **Seguridad:** JWT sin refresh; tokens expiran en 1 hora (hardcoded)
- **DB:** una sola base de datos; sin estrategia de backup

### Mejoras Futuras
1. **Frontend:**
   - Formularios con Reactive Forms avanzadas
   - Paginación, filtros y búsqueda en listados
   - Gestión de tareas integrada (editar estado, eliminar)

2. **Backend:**
   - Refresh tokens para extender sesiones sin re-login
   - Auditoría de cambios (quien/cuando/qué modificó)
   - Endpoints de estadísticas (tareas completadas, velocidad de equipo)
   - Rate limiting en el gateway
   - Manejar constantes para mensajes de error
   - Adicionar Swagger
   - 

3. **Testing:**
   - Tests unitarios de servicio con Mockito
   - Tests de integración con `@SpringBootTest` y testcontainers
   - Tests E2E con Cypress o Playwright

4. **DevOps:**
   - Docker Compose completo (all-in-one, incluyendo frontend)
   - CI/CD con GitHub Actions / GitLab CI
   - Métricas y monitoreo (Prometheus, Grafana)


## Estructura de Archivos Clave

```
PRUEBA_CODESA/
├── compose.yaml                      # BD PostgreSQL compartida
├── README.md                         # Este archivo
│
├── api-auth/                         # Servicio de autenticación
│   ├── build.gradle
│   └── src/main/java/codesa/com/co/
│       ├── ApiAuthApplication.java
│       ├── domain/User.java, Role.java
│       ├── repository/UserRepository.java
│       ├── security/JwtService.java
│       ├── service/AuthService.java
│       ├── web/AuthController.java
│       └── config/
│           ├── JwtProperties.java
│           ├── SecurityConfig.java
│           └── RestExceptionHandler.java
│   └── src/main/resources/
│       ├── application.properties
│       └── db/migration/V1__users.sql
│
├── api-proyectos/                   # Servicio de proyectos y tareas
│   ├── build.gradle
│   └── src/main/java/codesa/com/co/
│       ├── ApiProyectosApplication.java
│       ├── domain/Proyecto.java, Tarea.java, Estado.java
│       ├── repository/ProyectoRepository.java, TareaRepository.java
│       ├── service/ProyectoService.java, TareaService.java
│       ├── web/ProyectoController.java, TareaController.java
│       ├── security/HeaderAuthenticationFilter.java
│       └── config/
│           ├── SecurityConfig.java
│           └── RestExceptionHandler.java
│   └── src/main/resources/
│       ├── application.properties
│       └── db/migration/V1__proyectos_tareas.sql
│
├── api-gateway/                      # Gateway y validación JWT
│   ├── build.gradle
│   └── src/main/java/codesa/com/co/
│       ├── ApiGatewayApplication.java
│       ├── filter/JwtAuthenticationFilter.java
│       ├── security/JwtService.java
│       └── config/
│           ├── JwtProperties.java
│           └── GatewayConfig.java
│   └── src/main/resources/
│       └── application.yml
│
└── frontend/                         # Angular 20
    ├── package.json
    ├── angular.json
    └── src/
        ├── app/
        │   ├── app.component.ts
        │   ├── app.routes.ts
        │   ├── app.config.ts
        │   ├── core/
        │   │   ├── auth.service.ts
        │   │   ├── auth.interceptor.ts
        │   │   └── auth.guard.ts
        │   ├── services/
        │   │   ├── project.service.ts
        │   │   └── task.service.ts
        │   └── features/
        │       ├── login/login.component.ts
        │       └── projects/projects.component.ts
        ├── environments/environment.ts
        └── index.html
```

## Comandos Útiles

```bash
# Compilar todos
cd api-auth && ./gradlew clean build && cd ..
cd api-proyectos && ./gradlew clean build && cd ..
cd api-gateway && ./gradlew clean build && cd ..

# Limpiar BD (parar compose y borrar volumen)
docker compose down -v

# Revisar logs en tiempo real
docker logs -f codesa-postgres

# Verificar que los 3 servicios escuchan
lsof -i :8080  # gateway
lsof -i :8081  # auth
lsof -i :8082  # proyectos
lsof -i :5432  # postgres
lsof -i :4200  # angular
```

---

**Versión:** 0.0.1-SNAPSHOT  
**Stack:** Spring Boot 3.4.5 + Angular 20 + PostgreSQL + Docker  
**Ambiente:** desarrollo local (localhost)  
**Entrega:** 25 de junio de 2026