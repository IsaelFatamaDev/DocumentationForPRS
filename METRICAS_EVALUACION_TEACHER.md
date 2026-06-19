# Evaluación de Métricas de Calidad — vg-ms-teacher-assignment

**Microservicio:** Gestión de Asignación Docente
**Puerto Dev:** 9099 | **Puerto Prod:** 9099
**Arquitectura:** Hexagonal — Spring Boot 3.5 · Spring WebFlux · R2DBC · RabbitMQ
**Responsabilidad:** Asignaciones de docentes a cursos, aulas y horarios por año académico

> **Nota:** Este es un microservicio de backend puro. Las métricas de Sección 1 (PDF/reportes) y Sección 3 (Frontend) se evalúan desde la perspectiva de la contribución del backend a esas capacidades; la generación visual de PDFs y la interfaz pertenecen al módulo `vg-web-sigei`.

---

## SECCIÓN 1: Funcionalidad de Reportes (20%)

| N° | MÉTRICA | PUNTAJE | OBSERVACIÓN |
|----|---------|---------|-------------|
| M01 | Generación PDF | **3** | No genera PDF directamente — es responsabilidad del frontend (`jsPDF`/`html2canvas`). El backend expone `GET /api/teacher-assignments` y endpoints por docente/institución/año con estructura JSON limpia, lo que permite al frontend renderizar el reporte sin transformaciones adicionales. |
| M02 | Optimización | **3** | Las respuestas JSON son compactas (sin campos innecesarios). No existe paginación en los listados, lo cual puede elevar el payload para instituciones con muchos docentes. Pendiente implementar `Pageable` en endpoints de lista. |
| M03 | Precisión | **5** | Los datos devueltos en los endpoints coinciden exactamente con lo registrado en la tabla `teacher_assignments` de PostgreSQL. Mapeo bidireccional verificado en `PersistenceMapperTest`. Sin transformaciones que alteren valores. |
| M04 | Performance | **4** | Arquitectura reactiva con WebFlux + R2DBC garantiza I/O no bloqueante. Consultas por índice (`teacher_user_id`, `institution_id`, `academic_year`). Sin medición formal de latencia documentada (sin JMeter ni Postman Runner). |
| M05 | Identidad | **3** | La identidad institucional se aplica en el frontend. El backend retorna `photo_url` del docente (Cloudinary) que el frontend usa en el PDF. Campo disponible y mapeado correctamente en `AssignmentResponse`. |
| M06 | Filtros | **5** | Filtros completos implementados: por docente (`/teacher/{teacherUserId}`), institución (`/institution/{institutionId}`), año académico (`/academic-year/{year}`), estado (`/status/{status}`) y tipo (`/type/{type}`). Cubren todos los criterios de filtrado esperados en un reporte. |
| M07 | Robustez | **5** | Cuando no hay datos, el repositorio retorna `Flux.empty()` y el use case retorna lista vacía `[]` con HTTP 200. `GlobalExceptionHandler` captura `NotFoundException` y retorna 404 con mensaje legible. Sin 500 expuesto en casos de datos vacíos. |
| M08 | Trazabilidad | **4** | Campos `created_at` y `updated_at` en BD. **Agregado:** `created_by` y `updated_by` vía migración `V9__add_audit_fields.sql` y controller `TeacherAssignmentRest`. Permite trazar quién generó/modificó cada asignación. Falta log de evento "reporte generado". |
| M09 | Consistencia | **3** | Responsabilidad del frontend. El backend garantiza que los datos devueltos son consistentes (sin duplicados, sin nulos inesperados en campos requeridos). Validaciones Bean Validation en DTOs de entrada previenen datos malformados. |
| M10 | Descarga | **3** | La descarga del PDF es frontend. El backend sirve datos vía HTTP con headers `Content-Type: application/json`. CORS configurado en Gateway. Sin errores CORS verificados en los endpoints de este servicio desde el frontend. |

**Subtotal Sección 1:** 38 / 50 | **Promedio:** 3.8 / 5

---

## SECCIÓN 2: Backend & Lógica de Negocio (20%)

| N° | MÉTRICA | PUNTAJE | OBSERVACIÓN |
|----|---------|---------|-------------|
| M01 | Microservicios | **5** | Arquitectura hexagonal completa: `domain/models`, `domain/ports/in+out`, `application/usecases`, `infrastructure/adapters/in+out`. Separación correcta de responsabilidades. Ninguna dependencia de infraestructura en el dominio. |
| M02 | CRUDs | **5** | CRUD completo: `GET /api/teacher-assignments` (lista activa), `GET /{id}`, `POST` (crear), `PUT /{id}` (actualizar), `DELETE /{id}` (soft delete), `PATCH /{id}/restore`. Filtros adicionales por docente, institución, año, estado y tipo. Lógica de negocio validada en use case (`hasTeacherConflict`, `hasClassroomConflict`). |
| M03 | Validaciones | **4** | ✅ **Corregido:** `@NotBlank` en todos los campos requeridos de `CreateAssignmentRequest`. `@Pattern` en `assignmentType` (REGULAR\|SUBSTITUTE\|SUPPORT), `startDate`/`endDate` (fecha ISO), `academicYear` (4 dígitos). `@Valid` en los endpoints `POST` y `PUT`. Pendiente: validar que `endDate >= startDate`. |
| M04 | Manejo de Errores | **5** | `GlobalExceptionHandler` cubre: `NotFoundException` (404), `ConflictException` (409), `DomainException` (422), `WebExchangeBindException` (400), `NoResourceFoundException` (404) y `Exception` genérica (500). Respuesta JSON estructurada con `timestamp`, `status`, `error`, `path`. Sin stacktrace expuesto. |
| M05 | Rendimiento | **4** | WebFlux + R2DBC: I/O completamente reactivo y no bloqueante. Sin llamadas síncronas a BD. Validaciones en memoria antes de ir a repositorio. Sin JMeter ni Postman Runner para medición formal de latencia ≤ 300ms. |
| M06 | Integración | **4** | Publica eventos vía RabbitMQ con `IAssignmentEventPublisher` en `doOnSuccess`. No consume WebClient de otros servicios en esta versión. Integración asíncrona correcta; el publisher usa `subscribeOn(Schedulers.boundedElastic())` para no bloquear el flujo principal. |
| M07 | Escalabilidad | **5** | `application.yml` utiliza variables de entorno para todos los valores críticos: `${SERVER_PORT}`, `${DB_HOST}`, `${DB_NAME}`, `${KEYCLOAK_ISSUER_URI}`, `${RABBITMQ_HOST}`. Perfiles `dev`/`prod`/`test` diferenciados. Sin valores hardcodeados de infraestructura en código. |
| M08 | Auditoría | **5** | ✅ **Completado:** `created_at`, `updated_at` (existentes) + `created_by`, `updated_by` (agregados vía `V9__add_audit_fields.sql`). El controller extrae `preferred_username` del JWT (`@AuthenticationPrincipal Jwt`) y lo asigna en `create()` y `update()`. |
| M09 | Seguridad API | **5** | `SecurityConfig` con OAuth2 Resource Server + JWT. `JwtAuthorizationHelper` con `isOwnResource()` e `isAdmin()`. Endpoint `/teacher/{teacherUserId}` verifica que el docente solo vea sus propias asignaciones. Sin token → 401. Rol incorrecto → 403. |
| M10 | Transaccionalidad | **4** | Operaciones de escritura encadenadas con `flatMap`/`then`. Errores propagados con `onErrorMap` y `onErrorResume`. Evento publicado solo en `doOnSuccess`. Sin `@Transactional` (incompatible con R2DBC). Pendiente: rollback explícito en operaciones multi-entidad. |

**Subtotal Sección 2:** 46 / 50 | **Promedio:** 4.6 / 5

---

## SECCIÓN 3: Frontend & Experiencia de Usuario (20%)

> Esta sección evalúa la API REST del microservicio desde la perspectiva de qué tan bien habilita al frontend `vg-web-sigei` para cumplir los requisitos de UX. La evaluación de componentes React/TailwindCSS se realiza en el documento del frontend.

| N° | MÉTRICA | PUNTAJE | OBSERVACIÓN |
|----|---------|---------|-------------|
| M01 | Integración API | **5** | Respuestas con `ApiResponse<T>` consistente (`success`, `data`, `message`). Headers CORS configurados en el Gateway. Los endpoints siguen convención REST estándar que Axios puede consumir sin configuración especial. |
| M02 | Responsive | **3** | No aplica directamente al backend. El backend devuelve JSON estructurado; la adaptación visual es responsabilidad del frontend con TailwindCSS. |
| M03 | Feedback UX | **4** | El backend devuelve mensajes descriptivos en español en cada respuesta (`"Asignación creada"`, `"Asignación actualizada"`, `"Asignación eliminada"`). Errores devuelven mensaje legible para mostrar en Toast. Códigos HTTP correctos (201, 200, 404, 409). |
| M04 | Validación UI | **4** | ✅ Bean Validation activa: DTOs con `@NotBlank`, `@Pattern`, `@Size` + `@Valid` en controller. El backend rechaza con 400 y detalle de campos inválidos en `WebExchangeBindException` handler, permitiendo al frontend mostrar mensajes por campo. |
| M05 | Navegación | **4** | El endpoint `GET /api/teacher-assignments/teacher/{teacherUserId}` con verificación de propiedad permite al frontend implementar rutas protegidas por rol. Respuesta 403 ante acceso no autorizado, manejable en el interceptor Axios. |
| M06 | Usabilidad | **4** | Todos los datos necesarios para visualizar una asignación están en un solo response (`AssignmentResponse`). No requiere múltiples llamadas para renderizar la vista de lista. Reduce RTTs del frontend. |
| M07 | Consistencia UI | **4** | Estructura de respuesta `ApiResponse<T>` idéntica en todos los endpoints del servicio. El frontend puede usar el mismo handler genérico para todos los casos de éxito y error. |
| M08 | Soft Delete | **5** | Soft delete correctamente implementado: `DELETE /{id}` cambia estado a `INACTIVE`, `PATCH /{id}/restore` lo reactiva. El endpoint `GET` retorna solo registros activos. El frontend puede implementar vista de inactivos con filtro `GET /status/INACTIVE`. |
| M09 | Accesibilidad | **3** | No aplica al backend. El backend devuelve campos con nombres descriptivos que el frontend puede usar en `aria-label`. No hay generación de HTML desde el backend. |
| M10 | Gestión de Rol | **4** | El backend aplica control de acceso por rol en el endpoint de docente. Responde con 403 estructurado. El frontend puede leer el claim `role` del JWT decodificado para mostrar/ocultar opciones de menú. |

**Subtotal Sección 3:** 40 / 50 | **Promedio:** 4.0 / 5

---

## SECCIÓN 4: Calidad de Software y Pruebas (20%)

| N° | MÉTRICA | PUNTAJE | OBSERVACIÓN |
|----|---------|---------|-------------|
| M01 | Unit Testing | **4** | 4 clases de prueba: `CreateAssignmentUseCaseImplTest`, `DeleteAssignmentUseCaseImplTest`, `UpdateAssignmentUseCaseImplTest`, `PersistenceMapperTest`. Cubre los use cases críticos con Mockito + StepVerifier. Falta `GetAssignmentUseCaseImplTest`. Estimado cobertura ~65-70% en capa de use cases. |
| M02 | Integración | **2** | Sin prueba de integración documentada con `@SpringBootTest`. No existe colección Postman con flujo encadenado. El flujo "crear asignación → publicar evento → verificar en cola RabbitMQ" no está automatizado. |
| M03 | Smoke Tests | **2** | Sin script de smoke tests documentado post-despliegue. No hay colección Postman ni script `curl` para validar endpoints vitales en producción. Solo el `VgMsTeacherAssignmentApplicationTests` verifica arranque del contexto Spring. |
| M04 | Bug Rate | **4** | Durante la revisión se identificaron y corrigieron: (1) ausencia de Bean Validation en DTOs, (2) falta de `@Valid` en controller, (3) ausencia de campos de auditoría. Menos de 2 bugs críticos sin corregir al cierre de esta evaluación. |
| M05 | Performance Test | **2** | Sin prueba de carga documentada. No existe archivo JMeter ni Postman Collection Runner. No se ha verificado formalmente la capacidad de 20 usuarios concurrentes. |
| M06 | Seguridad | **5** | Sin SQL injection posible (R2DBC usa queries parametrizadas). Sin stacktrace expuesto en respuestas de error. Secrets en variables de entorno (`${DB_PASSWORD}`, `${KEYCLOAK_CLIENT_SECRET}`). Sin valores hardcodeados detectados en `src/`. |
| M07 | Manejo de Logs | **5** | `@Slf4j` en use cases y controller. `log.warn` en todos los handlers de excepción con contexto de path. `log.error` con stacktrace para errores 500. Sin `System.out.println` en código fuente. Nivel de log configurable por perfil. |
| M08 | Limpieza de Código | **4** | Código bien estructurado. Métodos privados con nombres semánticos (`hasTeacherConflict`, `hasClassroomConflict`, `isTemporal`). Observación menor: el controller inyecta `ITeacherAssignmentRepository` directamente para upload de foto (bypass hexagonal). |
| M09 | Compatibilidad | **4** | Backend REST puro; compatibilidad de cliente (Chrome/Firefox) es responsabilidad del frontend. La API es compatible con cualquier cliente HTTP. Headers `Content-Type: application/json` correctos. CORS manejado en Gateway. |
| M10 | Documentación QA | **2** | Sin plan de pruebas formal (Excel/MD) con casos de prueba, resultado esperado vs obtenido y estado PASS/FAIL. Los tests JUnit son el único artefacto de QA. Pendiente: documento de plan de pruebas por módulo. |

**Subtotal Sección 4:** 34 / 50 | **Promedio:** 3.4 / 5

---

## SECCIÓN 5: Infraestructura, Seguridad y Documentación (20%)

| N° | MÉTRICA | PUNTAJE | OBSERVACIÓN |
|----|---------|---------|-------------|
| M01 | Contenedores | **4** | `Dockerfile` multistage presente (builder Maven + runtime JRE 17 Alpine). Sin `docker-compose.yml` local en el repositorio del servicio (el compose raíz está a nivel de proyecto). La imagen se construye y ejecuta correctamente. |
| M02 | Keycloak | **5** | `SecurityConfig` configura OAuth2 Resource Server con `jwk-set-uri` apuntando al realm `sigei`. `KeycloakRoleConverter` mapea roles del token. `JwtAuthorizationHelper` valida `sub` y rol. Realm configurado en `application.yml` mediante variable de entorno. |
| M03 | Base de Datos | **5** | Base de datos propia `sigei_teacher_assignment` en PostgreSQL. Flyway gestiona migraciones (`V1` a `V9`). Volúmenes Docker configurados a nivel de compose raíz. Separación de BD garantizada: ninguna consulta accede a tablas de otro servicio. |
| M04 | HTTPS / SSL | **4** | TLS gestionado por Traefik en la capa de gateway. Este microservicio opera en red interna Docker; la comunicación externa pasa por HTTPS en el Gateway. Configuración de Traefik fuera del alcance de este repositorio. |
| M05 | Monitoreo | **4** | Spring Boot Actuator presente en `pom.xml`. `/actuator/health` expuesto. Perfiles `prod` restringen endpoints de actuator a `/actuator/health` y `/actuator/info`. Sin configuración de métricas Micrometer/Prometheus documentada. |
| M06 | API Doc | **5** | Dependencia `springdoc-openapi-starter-webflux-ui` en `pom.xml`. Swagger UI disponible en `/swagger-ui/index.html`. Endpoints documentados con anotaciones SpringDoc. Try-it-out funcional con JWT. |
| M07 | Diagramas | **3** | `README.md` presente con descripción del servicio. Sin diagrama C4 nivel 2 ni diagrama de secuencia del flujo "crear asignación → validar conflicto → publicar evento". Pendiente agregar diagramas arquitectónicos en `/docs`. |
| M08 | Diccionario BD | **3** | Las migraciones Flyway (`V1__*.sql` a `V9__*.sql`) documentan implícitamente el schema. Sin documento Markdown/Excel formal con diccionario de datos (tabla, columna, tipo, restricción, relaciones FK). |
| M09 | Manual de Usuario | **2** | Sin guía de usuario o manual de operación documentado. El `README.md` describe el servicio a alto nivel pero sin pasos de uso por rol (DOCENTE, DIRECTOR). |
| M10 | Pipeline CI/CD | **4** | `.gitlab-ci.yml` presente con etapas de build y test. Sin `Jenkinsfile` en este repositorio. El pipeline GitLab incluye compilación Maven + pruebas JUnit. Falta etapa de construcción de imagen Docker en el pipeline. |

**Subtotal Sección 5:** 39 / 50 | **Promedio:** 3.9 / 5

---

## Resumen Global

| Sección | Peso | Subtotal | Promedio (1-5) |
|---------|------|----------|----------------|
| Sección 1: Funcionalidad de Reportes | 20% | 38 / 50 | **3.8** |
| Sección 2: Backend & Lógica de Negocio | 20% | 46 / 50 | **4.6** |
| Sección 3: Frontend & UX (contribución backend) | 20% | 40 / 50 | **4.0** |
| Sección 4: Calidad de Software y Pruebas | 20% | 34 / 50 | **3.4** |
| Sección 5: Infraestructura, Seguridad y Docs | 20% | 39 / 50 | **3.9** |
| **TOTAL GLOBAL** | **100%** | **197 / 250** | **3.94 / 5** |

---

## Correcciones Aplicadas en Esta Revisión

| # | Problema | Solución | Archivo |
|---|----------|----------|---------|
| 1 | Sin Bean Validation en `CreateAssignmentRequest` | Agregados `@NotBlank`, `@Pattern` en todos los campos | `CreateAssignmentRequest.java` |
| 2 | Sin Bean Validation en `UpdateAssignmentRequest` | Agregados `@Pattern`, `@Size` en campos editables | `UpdateAssignmentRequest.java` |
| 3 | `@Valid` faltante en controller | Agregado `@Valid` en `create()` y `update()` | `TeacherAssignmentRest.java` |
| 4 | Sin campos de auditoría `created_by`/`updated_by` | Migración Flyway V9, entidad, modelo, mapper y controller actualizados | `V9__add_audit_fields.sql` + 4 archivos |

## Mejoras Pendientes

| Prioridad | Área | Descripción |
|-----------|------|-------------|
| Alta | Pruebas | Agregar `GetAssignmentUseCaseImplTest` + smoke tests post-despliegue |
| Alta | Pruebas | Crear colección Postman con flujo de integración end-to-end |
| Alta | Pruebas | Configurar JMeter para prueba de carga con 20 usuarios concurrentes |
| Media | Validación | Agregar validación cruzada `endDate >= startDate` en use case |
| Media | Arquitectura | Mover upload de foto a un use case dedicado (eliminar bypass hexagonal en controller) |
| Media | Docs | Crear diagrama C4 y diagrama de secuencia en `/docs` |
| Media | Docs | Crear diccionario de datos de la tabla `teacher_assignments` |
| Baja | Pipeline | Agregar etapa de `docker build` en `.gitlab-ci.yml` |
| Baja | Docs | Crear manual de uso por rol (DOCENTE / DIRECTOR) |
