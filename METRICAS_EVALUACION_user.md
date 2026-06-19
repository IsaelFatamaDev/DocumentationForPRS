# Evaluación de Métricas de Calidad — vg-ms-users-management

**Microservicio:** Gestión de Usuarios
**Puerto Dev:** 9083 | **Puerto Prod:** 9083
**Arquitectura:** Hexagonal — Spring Boot 3.5 · Spring WebFlux · R2DBC · RabbitMQ · AWS S3
**Responsabilidad:** Registro, gestión de estado y ciclo de vida de usuarios del sistema con segmentación por institución y rol

> **Nota:** Este es un microservicio de backend puro. Las métricas de Sección 1 (PDF/reportes) y Sección 3 (Frontend) se evalúan desde la perspectiva de la contribución del backend a esas capacidades; la generación visual de PDFs y la interfaz pertenecen al módulo `vg-web-sigei`.

---

## SECCIÓN 1: Funcionalidad de Reportes (20%)

| N° | MÉTRICA | PUNTAJE | OBSERVACIÓN |
| -- | ------- | ------- | ----------- |
| M01 | Generación PDF | **3** | No genera PDF directamente. Expone endpoints con `UserResponse` completo que el frontend usa con `jsPDF`/`html2canvas` para reportes de directorio de usuarios. Estructura de respuesta limpia y sin nulos inesperados. |
| M02 | Optimización | **3** | Respuestas JSON compactas. `findByInstitutionIdScoped` reduce volumen de datos por institución. Sin paginación formal en endpoints de lista. Payload puede ser elevado para instituciones grandes. |
| M03 | Precisión | **5** | Datos devueltos coinciden exactamente con la tabla `users`. Mapeo bidireccional verificado en `UserPersistenceMapperTest`. `UserMapperTest` cubre `toDomain` y `toResponse`. Sin transformaciones que alteren valores. |
| M04 | Performance | **5** | WebFlux + R2DBC con I/O completamente reactivo. Prueba de carga documentada en `users_load_test.jmx` (JMeter). Consultas con índices por `institution_id`, `role` y `status`. Medición formal disponible. |
| M05 | Identidad | **4** | Retorna `photo_url` (almacenada en AWS S3 via `StorageService`). `POST /{id}/photo` sube foto del usuario. El frontend usa la URL en PDF de directorio. Subida con `FilePart` reactivo funcional. |
| M06 | Filtros | **5** | Filtros completos: por estado (`/status/{status}`), por rol y estado (`/role/{role}/status/{status}`), por institución (`/institution/{institutionId}`), verificación de duplicados (`/exists/document`, `/exists/email`, `/exists/phone`), perfil propio (`/me`). |
| M07 | Robustez | **5** | `Flux.empty()` retorna 200 con lista vacía. `NotFoundException` retorna 404. `NoResourceFoundException` retorna 404 estructurado (agregado en esta revisión). Sin 500 por datos vacíos ni rutas inválidas. |
| M08 | Trazabilidad | **5** | Auditoría completa: `created_at`, `updated_at`, `created_by`, `updated_by`. Controller extrae username via `RequestContext.from(httpRequest)` y lo asigna en `create()` y `update()`. Migración `V3__add_audit_fields_to_users.sql` aplicada. |
| M09 | Consistencia | **3** | No aplica al backend directamente. Tipos de dato uniformes, enums de rol y estado validados, sin nulos en campos requeridos. El frontend usa datos sin re-validación. |
| M10 | Descarga | **3** | Descarga de PDF es responsabilidad del frontend. Backend sirve JSON con headers correctos. CORS configurado en Gateway. Sin errores CORS en endpoints de este servicio. |

**Subtotal Sección 1:** 41 / 50 | **Promedio:** 4.1 / 5

---

## SECCIÓN 2: Backend & Lógica de Negocio (20%)

| N° | MÉTRICA | PUNTAJE | OBSERVACIÓN |
| -- | ------- | ------- | ----------- |
| M01 | Microservicios | **5** | Arquitectura hexagonal perfecta: `domain/models/vo`, `domain/ports/in+out`, `application/usecases`, `application/dto`, `application/mappers`, `infrastructure/adapters/in+out`, `infrastructure/config`. Sin dependencias de infraestructura en el dominio. |
| M02 | CRUDs | **5** | CRUD completo: `GET /me`, `GET /` (scoped), `GET /{id}`, `GET /status/{status}`, `GET /role/{role}/status/{status}`, `GET /institution/{institutionId}`, `POST`, `PUT /{id}`, `DELETE /{id}` (soft), `PATCH /{id}/restore`, `POST /{id}/photo`. Lógica en use cases. |
| M03 | Validaciones | **5** | DTOs con `@NotNull`, `@NotBlank`, `@Size`, `@Pattern`, `@Email` en todos los campos requeridos. `@Valid` en endpoints POST y PUT. Validaciones de negocio en use cases (unicidad email, teléfono, documento). Respuesta 400 con detalle por campo. |
| M04 | Manejo de Errores | **5** | `GlobalExceptionHandler` con `@RestControllerAdvice`: `NotFoundException` (404), `ConflictException` (409), `DomainException` (422), `WebExchangeBindException` (400), `NoResourceFoundException` (404, agregado en esta revisión), `Exception` (500). Sin stacktrace expuesto. |
| M05 | Rendimiento | **5** | WebFlux + R2DBC con I/O no bloqueante. JMeter (`users_load_test.jmx`) documenta prueba de carga. Consultas optimizadas con índices. `RequestContext` extrae headers en O(1). |
| M06 | Integración | **5** | Publica eventos `UserCreated` vía RabbitMQ. Escucha eventos con `GuardianEventListener`. `WebClientConfig` para consumo de otros servicios. Integración asíncrona con `subscribeOn(Schedulers.boundedElastic())`. |
| M07 | Escalabilidad | **5** | Variables de entorno para todos los valores críticos: `DB_HOST`, `DB_NAME`, `KEYCLOAK_ISSUER_URI`, `RABBITMQ_HOST`, `AWS_ACCESS_KEY_ID`, `AWS_S3_BUCKET` en `application.yml`. Perfiles `dev`/`prod`/`test`. Sin valores hardcodeados. |
| M08 | Auditoría | **5** | Completo: `created_at`, `updated_at` (existentes) + `created_by`, `updated_by` (migración V3). `RequestContext.from(request)` extrae `X-Username` del header del Gateway. Asignado en `create()` (`createdBy` + `updatedBy`) y `update()` (`updatedBy`). |
| M09 | Seguridad API | **5** | OAuth2 Resource Server + JWT. `JwtAuthorizationHelper`: `isOwnResource()` e `isAdmin()`. `GET /{id}` verifica propiedad. `GET /` scoped por rol/institución. `GET /me` verifica autenticación. Sin token → 401. Sin scope → 403. |
| M10 | Transaccionalidad | **5** | `flatMap`/`then`/`switchIfEmpty` en operaciones de escritura. `onErrorMap` y `onErrorResume` para propagación de errores. Evento publicado solo en `doOnSuccess`. Validaciones previas a escritura. Sin estados inconsistentes detectados. |

**Subtotal Sección 2:** 50 / 50 | **Promedio:** 5.0 / 5

---

## SECCIÓN 3: Frontend & Experiencia de Usuario (20%)

> Esta sección evalúa la API REST del microservicio desde la perspectiva de qué tan bien habilita al frontend `vg-web-sigei`. La evaluación de componentes React es parte del documento del frontend.

| N° | MÉTRICA | PUNTAJE | OBSERVACIÓN |
| -- | ------- | ------- | ----------- |
| M01 | Integración API | **5** | `ApiResponse<T>` consistente en todas las respuestas. CORS en Gateway. Convención REST estándar. Interceptor Axios del frontend adjunta JWT automáticamente. Verificado en `SIGEI_Users_Postman_Collection.json`. |
| M02 | Responsive | **3** | No aplica al backend. Las respuestas JSON son procesadas por el frontend con TailwindCSS 4. El backend no genera HTML. |
| M03 | Feedback UX | **5** | Mensajes en español en cada respuesta: `"Usuario creado"`, `"Usuario actualizado"`, `"Usuario eliminado"`, `"Perfil del usuario"`. Errores con mensaje legible para Toast. Códigos HTTP correctos (201, 200, 401, 403, 404, 409). |
| M04 | Validación UI | **5** | Bean Validation con respuesta estructurada de campos inválidos via `WebExchangeBindException`. Duplicados retornan 409 con mensaje específico. `exists/*` endpoints permiten validación en tiempo real al salir del campo. |
| M05 | Navegación | **5** | `GET /me` permite perfil sin conocer el ID. `GET /` scoped permite al DIRECTOR ver solo su institución. Respuestas 401/403 estructuradas manejables en interceptor Axios. |
| M06 | Usabilidad | **5** | `UserResponse` contiene todos los datos para la vista en un solo request. `GET /exists/*` permite validación progresiva sin esperar al submit. Reduce RTTs del frontend. |
| M07 | Consistencia UI | **5** | `ApiResponse<T>` idéntico en todos los endpoints. El frontend usa el mismo handler genérico. Enums `UserRole` y `UserStatus` son strings descriptivos usables directamente en UI. |
| M08 | Soft Delete | **5** | `DELETE /{id}` (INACTIVE) + `PATCH /{id}/restore` (ACTIVE). `GET /status/INACTIVE` disponible. Frontend implementa vista diferenciada con badges por estado. Sin borrado físico de datos. |
| M09 | Accesibilidad | **3** | No aplica al backend. El backend retorna campos con nombres semánticos. No genera HTML. |
| M10 | Gestión de Rol | **5** | `findAllScoped`, `findByStatusScoped`, `findByRoleAndStatusScoped`, `findByInstitutionIdScoped` aplican filtrado por rol en el backend. El frontend recibe solo los datos que le corresponden al rol autenticado. |

**Subtotal Sección 3:** 46 / 50 | **Promedio:** 4.6 / 5

---

## SECCIÓN 4: Calidad de Software y Pruebas (20%)

| N° | MÉTRICA | PUNTAJE | OBSERVACIÓN |
| -- | ------- | ------- | ----------- |
| M01 | Unit Testing | **5** | Cobertura extensiva: `CreateUserUseCaseImplTest`, `CreateUserUseCaseImplParameterizedTest`, `DeleteUserUseCaseImplTest`, `GetUserUseCaseImplTest`, `RestoreUserUseCaseImplTest`, `UpdateUserUseCaseImplTest`, `UpdateUserUseCaseImplParameterizedTest`. Todos los use cases cubiertos con Mockito + StepVerifier. |
| M02 | Integración | **4** | `GuardianEventListenerTest` prueba integración RabbitMQ. `SIGEI_Users_Postman_Collection.json` documenta flujo completo. Sin `@SpringBootTest` de flujo end-to-end encadenado entre microservicios. |
| M03 | Smoke Tests | **4** | `SIGEI_Security_Tests.postman_collection.json` documenta pruebas de seguridad. `SIGEI_Environment.postman_environment.json` configura entorno. Sin script `curl` explícito para `/actuator/health` post-despliegue. |
| M04 | Bug Rate | **5** | Correcciones completadas: `NoResourceFoundException` sin handler, campos de auditoría faltantes. Suite de tests suficientemente exhaustiva para detectar regresiones. Menos de 2 bugs críticos sin corregir al cierre de la revisión. |
| M05 | Performance Test | **5** | `users_load_test.jmx` presente — prueba de carga JMeter documentada y ejecutable. Verifica capacidad de 20 usuarios concurrentes. Artefacto incluido en el repositorio. |
| M06 | Seguridad | **5** | Sin SQL injection (R2DBC parametrizado). Sin stacktrace en producción. `sonar-project.properties` con SonarQube. `SIGEI_Security_Tests` verifica 401 y 403. Secrets en variables de entorno. |
| M07 | Manejo de Logs | **5** | `@Slf4j` en todos los use cases y controller. `log.debug` en `GET /me` con userId/role. `log.warn` en handlers de excepción. `log.error` con stacktrace para 500. Sin `System.out.println`. Nivel configurable por perfil. |
| M08 | Limpieza de Código | **5** | Tests de DTOs, mappers y config (12+ clases de prueba). `sonar-project.properties` con Quality Gate activo. Sin imports sin usar ni métodos muertos detectados. `UserPersistenceMapperTest` y `UserMapperTest` verifican cobertura de mapeo. |
| M09 | Compatibilidad | **4** | API REST compatible con cualquier cliente HTTP. Verificado con `SIGEI_Users_Postman_Collection.json`. CORS en Gateway. Sin dependencias de características de navegador. |
| M10 | Documentación QA | **4** | Colecciones Postman (`SIGEI_Users_Postman_Collection.json`, `SIGEI_Security_Tests.postman_collection.json`) y `users_load_test.jmx` documentan casos de prueba ejecutables. Falta tabla formal PASS/FAIL con responsable asignado. |

**Subtotal Sección 4:** 46 / 50 | **Promedio:** 4.6 / 5

---

## SECCIÓN 5: Infraestructura, Seguridad y Documentación (20%)

| N° | MÉTRICA | PUNTAJE | OBSERVACIÓN |
| -- | ------- | ------- | ----------- |
| M01 | Contenedores | **5** | `Dockerfile` multistage presente (builder Maven + runtime JRE 17 Alpine). `docker-compose.yml` local en el repositorio para levantar el servicio de forma independiente con su base de datos. Imagen funcional verificada. |
| M02 | Keycloak | **5** | `SecurityConfig` con OAuth2 Resource Server + `jwk-set-uri`. `KeycloakRoleConverter` mapea roles desde el token. `KeycloakRoleConverterTest` verifica la conversión. `SecurityConfigTest` verifica configuración de seguridad. Realm `sigei` via variables de entorno. |
| M03 | Base de Datos | **5** | BD propia `sigei_users`. Flyway con 3 migraciones: `V1__create_users_table.sql`, `V2__add_photo_url_to_users.sql`, `V3__add_audit_fields_to_users.sql`. `R2dbcConfigTest` verifica configuración de conexión. Volúmenes en `docker-compose.yml`. |
| M04 | HTTPS / SSL | **4** | TLS gestionado por Traefik en capa de gateway (`sigei.ddns.net`). Comunicación interna en red Docker. Frontend apunta a `https://`. Configuración Traefik fuera del repositorio de este microservicio. |
| M05 | Monitoreo | **5** | Actuator presente. `/actuator/health` expuesto en perfil `prod`. `init-scripts/` con scripts de inicialización de BD. `docker-compose.yml` con `healthcheck`. Logs estructurados INFO/WARN/ERROR. |
| M06 | API Doc | **5** | `springdoc-openapi-starter-webflux-ui` en `pom.xml`. Swagger UI en `/swagger-ui/index.html`. `index.html` con configuración personalizada de Swagger. Try-it-out funcional con JWT. |
| M07 | Diagramas | **3** | `README.md` presente con descripción y estructura. `14_vg-ms-users-management.md` documenta el microservicio. Sin diagrama C4 nivel 2 ni diagrama de secuencia del flujo "crear usuario → publicar evento → listener". |
| M08 | Diccionario BD | **4** | Migraciones Flyway documentan el schema completo. `14_vg-ms-users-management.md` describe la tabla a alto nivel. Sin documento formal con FK, índices y restricciones detalladas en formato Excel/MD estándar. |
| M09 | Manual de Usuario | **2** | Sin guía de usuario paso a paso. `README.md` es técnico (setup). Sin manual para ADMINISTRADOR (gestionar usuarios) ni para DIRECTOR (ver usuarios de su institución). |
| M10 | Pipeline CI/CD | **5** | `pipeline.jenkins` presente con etapas: checkout → Maven build → pruebas JUnit → imagen Docker. `sonar-project.properties` integra SonarQube. Pipeline más completo entre los tres microservicios evaluados. |

**Subtotal Sección 5:** 43 / 50 | **Promedio:** 4.3 / 5

---

## Resumen Global

| Sección | Peso | Subtotal | Promedio (1-5) |
| ------- | ---- | -------- | -------------- |
| Sección 1: Funcionalidad de Reportes | 20% | 41 / 50 | **4.1** |
| Sección 2: Backend & Lógica de Negocio | 20% | 50 / 50 | **5.0** |
| Sección 3: Frontend & UX (contribución backend) | 20% | 46 / 50 | **4.6** |
| Sección 4: Calidad de Software y Pruebas | 20% | 46 / 50 | **4.6** |
| Sección 5: Infraestructura, Seguridad y Docs | 20% | 43 / 50 | **4.3** |
| **TOTAL GLOBAL** | **100%** | **226 / 250** | **4.52 / 5** |

---

## Correcciones Aplicadas en Esta Revisión

| N° | Problema | Solución | Archivo |
| -- | -------- | -------- | ------- |
| 1 | Sin handler para `NoResourceFoundException` (500 en rutas inválidas) | Agregado handler con respuesta 404 estructurada | `GlobalExceptionHandler.java` |
| 2 | Sin campos de auditoría `created_by`/`updated_by` | Migración V3, entidad, modelo, mapper y controller actualizados | `V3__add_audit_fields_to_users.sql` + 4 archivos |
| 3 | `create()` sin extracción de usuario para auditoría | Agregado `ServerHttpRequest` y asignación via `RequestContext` | `UserRest.java` |
| 4 | `update()` sin extracción de usuario para auditoría | Agregado `ServerHttpRequest` y asignación de `updatedBy` | `UserRest.java` |

## Mejoras Pendientes

| Prioridad | Área | Descripción |
| --------- | ---- | ----------- |
| Alta | Docs | Crear manual de usuario para ADMINISTRADOR y DIRECTOR |
| Media | Pruebas | Agregar prueba `@SpringBootTest` de flujo end-to-end |
| Media | Docs | Crear diagrama C4 nivel 2 y diagrama de secuencia del flujo principal |
| Media | Docs | Crear diccionario de datos formal con FK e índices |
| Baja | Reportes | Implementar paginación en endpoints de lista para reducir payload |
