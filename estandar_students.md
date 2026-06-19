# Evaluación de Métricas de Calidad — vg-ms-students

**Microservicio:** Gestión de Estudiantes y Apoderados
**Puerto Dev:** 9081 | **Puerto Prod:** 9081
**Arquitectura:** Hexagonal — Spring Boot 3.5 · Spring WebFlux · R2DBC · RabbitMQ · Cloudinary
**Responsabilidad:** Registro, gestión y trazabilidad de estudiantes y apoderados con vinculación a aulas e instituciones

> **Nota:** Este es un microservicio de backend puro. Las métricas de Sección 1 (PDF/reportes) y Sección 3 (Frontend) se evalúan desde la perspectiva de la contribución del backend a esas capacidades; la generación visual de PDFs y la interfaz pertenecen al módulo `vg-web-sigei`.

---

## SECCIÓN 1: Funcionalidad de Reportes (20%)

| N° | MÉTRICA | PUNTAJE | OBSERVACIÓN |
| -- | ------- | ------- | ----------- |
| M01 | Generación PDF | **3** | No genera PDF directamente. Expone `GET /api/students/{id}` con `StudentDetailResponse` (incluye apoderados anidados) que permite al frontend con `jsPDF`/`html2canvas` generar fichas completas de alumno sin llamadas adicionales. |
| M02 | Optimización | **3** | `StudentDetailResponse` agrega apoderados en un solo request evitando N+1 desde el frontend. Sin paginación en endpoints de lista. Para aulas con muchos estudiantes el payload puede ser elevado. |
| M03 | Precisión | **5** | Datos devueltos coinciden exactamente con las tablas `students` y `guardians`. Mapeo verificado en `StudentPersistenceMapperTest`. Sin transformaciones que alteren valores críticos (CUI, documento, fechas). |
| M04 | Performance | **4** | WebFlux + R2DBC con I/O no bloqueante. `GET /{id}` ejecuta student + guardians en cadena reactiva con `flatMap`. Sin JMeter documentado. Sin medición formal de latencia para este servicio. |
| M05 | Identidad | **4** | `photo_url` en `StudentResponse` y `GuardianResponse` (Cloudinary). Endpoints `POST /{id}/photo` para student y guardian disponibles. Frontend usa las URLs en PDF de ficha de alumno. |
| M06 | Filtros | **5** | Filtros completos para students: estado, aula, institución, CUI, hijos por apoderado. Para guardians: por estudiante, por id, verificación de documento/teléfono/email/whatsapp. Cubren todos los criterios de filtrado necesarios en un reporte. |
| M07 | Robustez | **5** | `Flux.empty()` retorna 200 con lista vacía. `NotFoundException` retorna 404. `NoResourceFoundException` retorna 404 estructurado (agregado en esta revisión). Sin 500 ante datos vacíos. |
| M08 | Trazabilidad | **5** | `created_at`, `updated_at`, `created_by`, `updated_by` en tablas `students` y `guardians`. `StudentRest` y `GuardianRest` extraen `X-Username` del header y asignan auditoría en `create()` y `update()`. Migración `V6__add_audit_fields.sql`. |
| M09 | Consistencia | **3** | No aplica al backend directamente. Tipos de dato uniformes, `StudentStatus` validado, fechas en ISO 8601. El frontend usa datos sin re-validación para PDFs. |
| M10 | Descarga | **3** | Descarga de PDF es responsabilidad del frontend. Backend sirve JSON con headers correctos. CORS en Gateway. Sin errores CORS en endpoints de este servicio. |

**Subtotal Sección 1:** 40 / 50 | **Promedio:** 4.0 / 5

---

## SECCIÓN 2: Backend & Lógica de Negocio (20%)

| N° | MÉTRICA | PUNTAJE | OBSERVACIÓN |
| -- | ------- | ------- | ----------- |
| M01 | Microservicios | **5** | Arquitectura hexagonal con dos dominios: `Student` y `Guardian`. Ports/in+out, usecases y adapters separados por entidad. `IStudentRepository` e `IGuardianRepository` son puertos de salida con implementación en infraestructura. Sin dependencias de infraestructura en dominio. |
| M02 | CRUDs | **5** | **Students:** GET lista/id/estado/CUI/aula/institución/hijos, POST, PUT, DELETE (soft), PATCH restore, POST foto. **Guardians:** GET por estudiante/id, verificación de duplicados (4 tipos), POST, PUT, DELETE, POST foto. CRUD completo en ambas entidades. |
| M03 | Validaciones | **4** | Bean Validation en todos los DTOs de request. `@Valid` en endpoints POST y PUT de ambos controllers. Validaciones de negocio en use cases (CUI único, documento único). Endpoints `exists/*` para verificación previa. Pendiente: validar formato de CUI con `@Pattern`. |
| M04 | Manejo de Errores | **5** | `GlobalExceptionHandler` con `@RestControllerAdvice`: `NotFoundException` (404), `ConflictException` (409), `DomainException` (422), `WebExchangeBindException` (400), `NoResourceFoundException` (404, agregado en esta revisión), `Exception` (500). Sin stacktrace expuesto. |
| M05 | Rendimiento | **4** | WebFlux + R2DBC no bloqueante. `findById` encadena student + guardians reactivamente. Sin JMeter ni Postman Runner documentados. Sin medición formal de latencia ≤ 300ms para endpoints críticos. |
| M06 | Integración | **4** | Publica eventos `StudentCreated` vía RabbitMQ. Escucha eventos de apoderados. Sin WebClient para consumo sincrónico de otros servicios en esta versión. Integración asíncrona correcta con `subscribeOn`. |
| M07 | Escalabilidad | **5** | Variables de entorno para `DB_HOST`, `DB_NAME`, `KEYCLOAK_ISSUER_URI`, `RABBITMQ_HOST`, `CLOUDINARY_URL` en `application.yml`. Perfiles `dev`/`prod`/`test`. Sin IPs ni passwords hardcodeados en código. |
| M08 | Auditoría | **5** | Completo: `created_at`, `updated_at` (existentes) + `created_by`, `updated_by` en `students` y `guardians` (migración V6). `StudentRest.create()` y `update()` asignan `username` desde `X-Username`. `GuardianRest.create()` y `update()` ídem (corregido en esta revisión). |
| M09 | Seguridad API | **5** | OAuth2 Resource Server + JWT. `JwtAuthorizationHelper.isOwnDocument()` en endpoint `GET /my-children/{documentNumber}`: el apoderado solo ve los hijos cuyo documento coincide con su JWT. Sin token → 401. Acceso ajeno → 403 con mensaje descriptivo. |
| M10 | Transaccionalidad | **4** | `flatMap`/`then` para operaciones de escritura. Errores propagados con `onErrorMap`. Evento publicado en `doOnSuccess`. Validaciones previas a escritura. Pendiente: rollback explícito en flujo student + guardian del mismo request. |

**Subtotal Sección 2:** 46 / 50 | **Promedio:** 4.6 / 5

---

## SECCIÓN 3: Frontend & Experiencia de Usuario (20%)

> Esta sección evalúa la API REST del microservicio desde la perspectiva de qué tan bien habilita al frontend `vg-web-sigei`. La evaluación de componentes React es parte del documento del frontend.

| N° | MÉTRICA | PUNTAJE | OBSERVACIÓN |
| -- | ------- | ------- | ----------- |
| M01 | Integración API | **5** | `ApiResponse<T>` consistente en todas las respuestas. Dos controllers separados (`StudentRest`, `GuardianRest`) permiten al frontend gestionar estudiantes y apoderados de forma independiente. Sin errores CORS en endpoints. |
| M02 | Responsive | **3** | No aplica al backend. El backend devuelve JSON estructurado. La adaptación visual es responsabilidad del frontend con TailwindCSS 4. |
| M03 | Feedback UX | **5** | Mensajes en español: `"Estudiante creado"`, `"Apoderado actualizado"`, `"Foto actualizada"`, `"Estudiante restaurado"`. Errores con mensaje legible para Toast. Códigos HTTP correctos (201, 200, 403, 404, 409). |
| M04 | Validación UI | **5** | Bean Validation con respuesta estructurada en `WebExchangeBindException`. `GET /exists/cui`, `/exists/document`, `/exists/phone`, `/exists/email`, `/exists/whatsapp` permiten validación en tiempo real. Frontend puede mostrar error por campo. |
| M05 | Navegación | **5** | `GET /my-children/{documentNumber}` con validación de propiedad JWT habilita la vista de APODERADO. Respuestas 401/403 estructuradas manejables en interceptor Axios. Rutas RESTful consistentes. |
| M06 | Usabilidad | **5** | `StudentDetailResponse` agrega student + guardians en un solo request. Sin múltiples llamadas para la vista de detalle. Reduce RTTs y simplifica el estado del frontend. |
| M07 | Consistencia UI | **5** | `ApiResponse<T>` idéntico en ambos controllers. Enums `StudentStatus` son strings descriptivos. Mismo handler genérico en el frontend para todos los endpoints. |
| M08 | Soft Delete | **5** | `DELETE /{id}` (INACTIVE) + `PATCH /{id}/restore` (ACTIVE). `GET /status/INACTIVE` disponible para lista de inactivos. Frontend puede mostrar estudiantes inactivos diferenciados y restaurarlos. Sin borrado físico. |
| M09 | Accesibilidad | **3** | No aplica al backend. El backend retorna campos con nombres semánticos. No genera HTML. |
| M10 | Gestión de Rol | **4** | `GET /my-children` aplica control por rol (APODERADO solo ve sus hijos). Control de visibilidad para DOCENTE/DIRECTOR/ADMIN se aplica en Gateway y frontend. Backend responde 403 estructurado ante acceso no autorizado. |

**Subtotal Sección 3:** 45 / 50 | **Promedio:** 4.5 / 5

---

## SECCIÓN 4: Calidad de Software y Pruebas (20%)

| N° | MÉTRICA | PUNTAJE | OBSERVACIÓN |
| -- | ------- | ------- | ----------- |
| M01 | Unit Testing | **3** | 4 clases de prueba: `CreateStudentUseCaseImplTest`, `DeleteStudentUseCaseImplTest`, `UpdateStudentUseCaseImplTest`, `StudentPersistenceMapperTest`. Faltan: `GetStudentUseCaseImplTest`, use cases de guardians, `GuardianPersistenceMapperTest`, `RestoreStudentUseCaseImplTest`. Cobertura estimada ~50%. |
| M02 | Integración | **2** | Sin prueba de integración documentada. No hay colección Postman con flujo "registrar estudiante → asignar apoderado → verificar vinculación". El flujo `CreateStudent → evento RabbitMQ` no está automatizado. |
| M03 | Smoke Tests | **2** | Sin script de smoke tests post-despliegue. No hay colección Postman ni script `curl` para `/actuator/health` y endpoints vitales. Solo `VgMsStudentsApplicationTests` verifica arranque del contexto Spring. |
| M04 | Bug Rate | **4** | Correcciones completadas en esta revisión: `NoResourceFoundException` sin handler, `GuardianRest` sin auditoría en create/update, ausencia de campos `created_by`/`updated_by`. Menos de 2 bugs críticos sin corregir al cierre. |
| M05 | Performance Test | **1** | Sin prueba de carga documentada. No existe JMeter ni Postman Collection Runner. No se ha verificado capacidad de 20 usuarios concurrentes. Artefacto más urgente pendiente de crear. |
| M06 | Seguridad | **5** | Sin SQL injection (R2DBC parametrizado). Sin stacktrace en producción. `isOwnDocument()` protege datos de apoderados. Secrets en variables de entorno (`CLOUDINARY_URL`, `DB_PASSWORD`). Validación JWT activa en todos los endpoints sensibles. |
| M07 | Manejo de Logs | **4** | `@Slf4j` en use cases y controllers. `log.warn` en handlers de excepción. Sin `System.out.println`. Observación: use cases de Guardian tienen menos logs que los de Student. `GuardianRest` no tiene logs en todos los métodos. |
| M08 | Limpieza de Código | **4** | Código bien estructurado con nombres semánticos. `StudentRest` y `GuardianRest` inyectan el repositorio directamente para upload de foto (bypass hexagonal). Debería usarse un use case dedicado de gestión de foto. |
| M09 | Compatibilidad | **4** | API REST compatible con cualquier cliente HTTP. CORS en Gateway. URLs de Cloudinary retornadas son HTTPS, compatibles con todos los navegadores modernos. Sin dependencias de características específicas de navegador. |
| M10 | Documentación QA | **2** | Sin plan de pruebas formal. Sin tabla de casos de prueba con resultado esperado vs obtenido y estado PASS/FAIL. Los tests JUnit son el único artefacto de QA. Pendiente crear documento de plan de pruebas por módulo. |

**Subtotal Sección 4:** 31 / 50 | **Promedio:** 3.1 / 5

---

## SECCIÓN 5: Infraestructura, Seguridad y Documentación (20%)

| N° | MÉTRICA | PUNTAJE | OBSERVACIÓN |
| -- | ------- | ------- | ----------- |
| M01 | Contenedores | **4** | `Dockerfile` multistage presente (builder Maven + runtime JRE 17 Alpine). Sin `docker-compose.yml` local en el repositorio. El compose raíz del proyecto incluye este servicio. Imagen funcional verificada. |
| M02 | Keycloak | **5** | `SecurityConfig` con OAuth2 Resource Server + `jwk-set-uri`. `JwtAuthorizationHelper` valida claims del JWT. `isOwnDocument()` compara `document_number` del JWT con el del path para proteger datos de apoderados. Realm `sigei` via variables de entorno. |
| M03 | Base de Datos | **5** | BD propia `sigei_students`. Flyway con migraciones V1 a V6: schema inicial de `students` y `guardians`, campos de foto, campos de auditoría. FK `student_id` en tabla `guardians`. Volúmenes en compose raíz. |
| M04 | HTTPS / SSL | **4** | TLS gestionado por Traefik en capa de gateway. Comunicación interna en red Docker. URLs de Cloudinary son HTTPS. Configuración TLS fuera del repositorio de este microservicio. |
| M05 | Monitoreo | **4** | Actuator presente. `/actuator/health` expuesto. Perfiles `prod` restringen endpoints de actuator. Logs estructurados. Sin configuración de métricas Micrometer/Prometheus documentada. |
| M06 | API Doc | **5** | `springdoc-openapi-starter-webflux-ui` en `pom.xml`. Swagger UI en `/swagger-ui/index.html`. Dos grupos de endpoints documentados (students + guardians). Try-it-out funcional con JWT. |
| M07 | Diagramas | **3** | `README.md` presente. `12_vg-ms-students.md` describe el microservicio. Sin diagrama C4 nivel 2 ni diagrama de secuencia del flujo "registrar estudiante → asignar aula → publicar evento". |
| M08 | Diccionario BD | **3** | Migraciones Flyway documentan el schema de `students` y `guardians`. `12_vg-ms-students.md` describe entidades a alto nivel. Sin documento formal con FK, índices, restricciones y tipos de dato detallados. |
| M09 | Manual de Usuario | **2** | Sin guía de usuario paso a paso. Sin manual para SECRETARÍA (registrar estudiante) ni para APODERADO (consultar sus hijos). El `README.md` es técnico. |
| M10 | Pipeline CI/CD | **3** | `.gitlab-ci.yml` presente con etapas básicas. Sin `Jenkinsfile`. Sin etapa `docker build` en el pipeline actual. Sin integración SonarQube. Pipeline más básico entre los tres microservicios evaluados. |

**Subtotal Sección 5:** 38 / 50 | **Promedio:** 3.8 / 5

---

## Resumen Global

| Sección | Peso | Subtotal | Promedio (1-5) |
| ------- | ---- | -------- | -------------- |
| Sección 1: Funcionalidad de Reportes | 20% | 40 / 50 | **4.0** |
| Sección 2: Backend & Lógica de Negocio | 20% | 46 / 50 | **4.6** |
| Sección 3: Frontend & UX (contribución backend) | 20% | 45 / 50 | **4.5** |
| Sección 4: Calidad de Software y Pruebas | 20% | 31 / 50 | **3.1** |
| Sección 5: Infraestructura, Seguridad y Docs | 20% | 38 / 50 | **3.8** |
| **TOTAL GLOBAL** | **100%** | **200 / 250** | **4.00 / 5** |

---

## Correcciones Aplicadas en Esta Revisión

| N° | Problema | Solución | Archivo |
| -- | -------- | -------- | ------- |
| 1 | Sin handler para `NoResourceFoundException` (500 en rutas inválidas) | Agregado handler con respuesta 404 estructurada | `GlobalExceptionHandler.java` |
| 2 | Sin campos de auditoría `created_by`/`updated_by` en `students` y `guardians` | Migración V6, entidades, modelos, mappers actualizados | `V6__add_audit_fields.sql` + 4 archivos |
| 3 | `StudentRest.create()` y `update()` sin auditoría | Agregada extracción de `X-Username` y asignación | `StudentRest.java` |
| 4 | `GuardianRest.create()` y `update()` sin `ServerHttpRequest` ni auditoría | Agregado parámetro y lógica de auditoría completa | `GuardianRest.java` |

## Mejoras Pendientes

| Prioridad | Área | Descripción |
| --------- | ---- | ----------- |
| Alta | Pruebas | Crear prueba de carga JMeter para 20 usuarios concurrentes |
| Alta | Pruebas | Agregar `GetStudentUseCaseImplTest`, use cases de guardians y `GuardianPersistenceMapperTest` |
| Alta | Pruebas | Crear colección Postman con flujo "estudiante → apoderado → aula" |
| Alta | Arquitectura | Mover upload de foto a use case dedicado en `StudentRest` y `GuardianRest` |
| Media | Pruebas | Crear smoke tests post-despliegue para `/actuator/health` y endpoints vitales |
| Media | Pipeline | Agregar etapa `docker build` y SonarQube en `.gitlab-ci.yml` |
| Media | Docs | Crear diagrama C4 nivel 2 y diagrama de secuencia del flujo principal |
| Media | Docs | Crear diccionario de datos formal para tablas `students` y `guardians` |
| Media | Validación | Agregar `@Pattern` en `CreateStudentRequest.cui` para validar formato |
| Media | Reportes | Implementar paginación en `findAll()` y `findByClassroomId()` |
| Baja | Docs | Crear manual de usuario para SECRETARÍA y APODERADO |
