# SER - Securizacion de Microservicios con Keycloak (USERS + STUDENTS + TEACHER ASSIGNMENT)

## 1. Objetivo del punto SER

Documentar y demostrar la securizacion de los microservicios backend Maestro (USERS) y Transaccionales (STUDENTS y TEACHER ASSIGNMENT), asi como el frontend, usando Keycloak como proveedor de identidad, JWT como mecanismo de autenticacion y Spring Security como framework de autorizacion.

## 2. Alcance

- API Gateway como unico punto de entrada publico.
- Microservicio Maestro: `vg-ms-users-management`.
- Microservicio Transaccional: `vg-ms-students`.
- Microservicio Transaccional distribuido: `vg-ms-teacher-assignment`.
- Frontend: `vg-web-sigei`.
- Proveedor IAM: Keycloak (realm `sigei`).

## 3. Principio de arquitectura de seguridad

Los microservicios no deben exponerse directamente a internet. Esto no es una limitacion, es una buena practica.

Modelo aplicado:

1. El cliente (frontend) solo consume el Gateway.
2. El Gateway valida el JWT emitido por Keycloak.
3. El Gateway enruta al microservicio correspondiente.
4. Cada microservicio vuelve a validar JWT y roles (defensa en profundidad).

Beneficio: aun si alguien intenta saltarse el Gateway o hay trafico interno no confiable, el backend mantiene su propia capa de autorizacion.

## 4. Tecnologias utilizadas

- Keycloak (OIDC/OAuth2)
- JWT (access token y refresh token)
- Spring Security WebFlux
- Spring Boot OAuth2 Resource Server
- Interceptores Axios en frontend

## 5. Evidencia tecnica en codigo

### 5.1 Gateway

- Configuracion de seguridad y validacion JWT:
  - `vg-ms-gateway/src/main/java/pe/edu/vallegrande/sigei/vgmsgateway/config/SecurityConfig.java`
- Configuracion issuer/jwk para Keycloak:
  - `vg-ms-gateway/src/main/resources/application-dev.yml`

Comportamiento:

- Permite solo rutas publicas (`/api/auth/**`, `/actuator/**`, `/fallback/**`).
- El resto requiere token valido.
- Convierte roles de Keycloak (`realm_access` y `resource_access`) a `ROLE_*`.

### 5.2 USERS (Maestro)

- Seguridad por perfil y autorizacion por endpoint/metodo:
  - `vg-ms-users-management/src/main/java/pe/edu/vallegrande/sigei/vgmsusermanagement/infrastructure/config/SecurityConfig.java`
- Configuracion JWT Keycloak:
  - `vg-ms-users-management/src/main/resources/application-dev.yml`
  - `vg-ms-users-management/src/main/resources/application-prod.yml`
- Dependencia OAuth2 Resource Server:
  - `vg-ms-users-management/pom.xml`

Comportamiento:

- `GET /api/users/me`: autenticado.
- Lectura (`GET /api/users/**`): roles de lectura.
- Escritura (`POST/PUT/PATCH/DELETE /api/users/**`): roles de administracion.
- Swagger permitido en `dev` y bloqueado en `prod`.

### 5.3 STUDENTS (Transaccional)

- Seguridad por perfil y autorizacion por endpoint/metodo:
  - `vg-ms-students/src/main/java/pe/edu/vallegrande/sigei/students/infrastructure/config/SecurityConfig.java`
- Configuracion JWT Keycloak:
  - `vg-ms-students/src/main/resources/application-dev.yml`
  - `vg-ms-students/src/main/resources/application-prod.yml`
- Dependencia OAuth2 Resource Server:
  - `vg-ms-students/pom.xml`

Comportamiento:

- Lectura de estudiantes/apoderados: roles institucionales.
- Escritura de estudiantes/apoderados: roles administrativos.
- `GET /api/students/my-children/**`: exclusivo para roles de familia (`APODERADO`, `PADRE`, `MADRE`).
- Swagger permitido en `dev` y bloqueado en `prod`.

### 5.4 TEACHER ASSIGNMENT (Transaccional distribuido)

- Seguridad por perfil y autorizacion por endpoint/metodo:
  - `vg-ms-teacher-assignment/src/main/java/pe/edu/vallegrande/sigei/vgmsteacherassignment/infrastructure/config/SecurityConfig.java`
- Configuracion JWT Keycloak:
  - `vg-ms-teacher-assignment/src/main/resources/application-dev.yml`
  - `vg-ms-teacher-assignment/src/main/resources/application-prod.yml`
- Dependencias de seguridad:
  - `vg-ms-teacher-assignment/pom.xml`

Comportamiento:

- Lectura general de asignaciones (`GET /api/teacher-assignments/**`): roles de gestion institucional.
- Lectura para horario docente (`GET /api/teacher-assignments/teacher/**` y `GET /api/assignments-management/**`): incluye rol `DOCENTE`.
- Escritura de asignaciones, aulas y horarios (`POST/PUT/PATCH/DELETE`): roles administrativos (`ADMINISTRADOR`, `DIRECTOR`, `SUBDIRECTOR`).
- Swagger permitido en `dev` y bloqueado en `prod`.

### 5.5 Frontend

- Parseo robusto de JWT y resolucion de rol:
  - `vg-web-sigei/src/core/utils/token.js`
- Interceptor de token, refresh y cierre de sesion por expiracion:
  - `vg-web-sigei/src/core/api/interceptors.js`

Comportamiento:

- Adjunta `Authorization: Bearer <token>` en peticiones autenticadas.
- Si token expira, intenta refresh.
- Si refresh falla, limpia sesion y redirige a login.
- Resuelve rol desde `resource_access` (cliente configurable) o `realm_access`.

## 6. Flujo funcional de seguridad

1. Usuario inicia sesion en frontend.
2. Frontend llama `/api/auth/login` via Gateway.
3. Auth service/Keycloak emite `access_token` y `refresh_token`.
4. Frontend guarda tokens y envia Bearer token en cada request.
5. Gateway valida JWT contra issuer/jwk de Keycloak.
6. Gateway enruta a USERS, STUDENTS o TEACHER ASSIGNMENT.
7. USERS/STUDENTS/TEACHER ASSIGNMENT validan JWT y autorizan por rol + endpoint + metodo.
8. Respuesta al cliente.

## 7. Casos de demostracion para video (evidencia obligatoria)

Demostrar en vivo estos 5 casos:

1. Sin token -> 401 Unauthorized.
2. Token invalido o manipulado -> 401 Unauthorized.
3. Token expirado -> refresh o redireccion a login.
4. Token valido con rol incorrecto -> 403 Forbidden.
5. Token valido con rol correcto -> 200 OK.

## 8. Script recomendado para video (10 a 15 min)

### 8.1 Estructura del video

1. Presentacion (1 min): "En esta evidencia mostraremos la securizacion de USERS, STUDENTS y TEACHER ASSIGNMENT con Keycloak, JWT y Spring Security."

2. Arquitectura (2 min): explicar que los microservicios no estan expuestos directamente, mostrar que el Gateway es el unico entrypoint y remarcar defensa en profundidad.

3. Codigo backend (3 a 4 min): mostrar `SecurityConfig` de Gateway, USERS, STUDENTS y TEACHER ASSIGNMENT, ademas de `issuer-uri` y `jwk-set-uri` en yml.

4. Codigo frontend (2 min): mostrar `token.js` e `interceptors.js`, explicando expiracion y refresh token.

5. Pruebas funcionales en vivo (3 a 5 min): ejecutar los 5 casos de evidencia.

6. Cierre (1 min): explicar el beneficio empresarial (menos acceso no autorizado, cumplimiento, trazabilidad y escalabilidad).

### 8.2 Texto corto para explicar por que no exponer microservicios

"No exponer USERS, STUDENTS y TEACHER ASSIGNMENT directamente es una decision de seguridad. El Gateway centraliza autenticacion y control de acceso, y ademas cada microservicio vuelve a validar JWT y roles. Esto evita depender de una sola capa y reduce el impacto ante intentos de bypass internos o externos."

## 9. Preguntas de defensa (respuestas sugeridas)

### Pregunta: "Si ya valida el Gateway, por que validar tambien en microservicios?"

Respuesta:

"Por defensa en profundidad. El Gateway es la primera barrera, pero cada microservicio conserva seguridad propia para no confiar ciegamente en la red interna."

### Pregunta: "Que rol cumple Keycloak?"

Respuesta:

"Es el proveedor de identidad. Emite JWT firmados, administra realm, clientes, usuarios y roles. Nosotros validamos esos JWT en Gateway y microservicios."

### Pregunta: "Que pasa si expira el token?"

Respuesta:

"El frontend detecta expiracion, intenta refresh token y, si falla, limpia sesion y redirige a login para evitar uso de credenciales invalidas."

## 10. Mejoras propuestas (alternativas)

1. Mover tokens a cookies `HttpOnly + Secure + SameSite` para reducir riesgo XSS.
2. Agregar rate limiting y proteccion anti brute-force en Gateway.
3. Incluir auditoria de eventos de seguridad (login, refresh, acceso denegado).
4. Agregar mTLS entre Gateway y microservicios en ambientes productivos.
5. Integrar pruebas de seguridad automatizadas en CI/CD (SAST/DAST).

## 11. Checklist de entrega SER

- [ ] Video demostrativo mostrando codigo + ejecucion.
- [ ] Evidencia de casos 401/403/200.
- [ ] Explicacion de Keycloak + JWT + Spring Security.
- [ ] Justificacion de arquitectura con Gateway como unica exposicion.
- [ ] Propuesta de mejora adicional.

## 12. Nota para tu guia

Puedes copiar las secciones 1, 3, 5, 6 y 10 directamente en tu informe de guia.
Para el video, usa secciones 7 y 8 como guion.
