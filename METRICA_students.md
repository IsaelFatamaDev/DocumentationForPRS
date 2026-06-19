# Evaluación de Métricas — vg-ms-students

**Puerto:** 9081 | **BD:** sigei_students | **Arch:** Hexagonal · WebFlux · R2DBC · RabbitMQ
**Calificación Global: 81%**

---

## SECCIÓN 1: Funcionalidad de Reportes (20%) — Promedio: 4.3/5

| N° | MÉTRICA | PUNTAJE | OBSERVACIÓN |
|----|---------|:-------:|-------------|
| M01 | Generación PDF | 4 | Backend expone datos JSON de estudiante + guardians. PDF se genera en frontend con jsPDF. |
| M02 | Optimización | 4 | Respuestas JSON incluyen detalle completo del alumno. Sin paginación en listados grandes. |
| M03 | Precisión | 5 | Mapeo bidireccional verificado en StudentPersistenceMapperTest. 0 discrepancias BD vs API. |
| M04 | Performance | 5 | WebFlux + R2DBC no bloqueante. findById encadena reactivamente con guardians sin bloqueo. |
| M05 | Identidad | 5 | Campo photo_url con URL S3 disponible en StudentResponse y GuardianResponse. |
| M06 | Filtros | 5 | Filtros por estado, CUI, aula, institución, hijos por apoderado. Endpoints dedicados. |
| M07 | Robustez | 5 | Sin datos → lista vacía HTTP 200. Excepciones manejadas sin 500 expuesto. |
| M08 | Exportación Alt. | — | No aplica en este microservicio. |
| M09 | Consistencia | 5 | Datos consistentes. Bean Validation en DTOs de entrada previene datos malformados. |
| M10 | Descarga | 5 | API JSON compatible con cualquier cliente. CORS configurado en Gateway. |

---

## SECCIÓN 2: Backend & Lógica de Negocio (20%) — Promedio: 4.6/5

| N° | MÉTRICA | PUNTAJE | OBSERVACIÓN |
|----|---------|:-------:|-------------|
| M01 | Microservicios | 5 | Hexagonal completa con 2 entidades de dominio: Student y Guardian. Ports/in+out separados por entidad. |
| M02 | CRUDs | 5 | CRUD Student + Guardian completo + /exists/{cui,document} + /my-children/{doc} + soft delete + restore + foto. |
| M03 | Validaciones | 5 | @NotBlank, @Size, @NotNull en CreateStudentRequest. @Size en UpdateStudentRequest. @Valid en POST/PUT. |
| M04 | Manejo de Errores | 5 | GlobalExceptionHandler: NotFound(404), Conflict(409), Domain(422), Validation(400), NoResourceFound(404), Generic(500). |
| M05 | Rendimiento | 4 | WebFlux + R2DBC no bloqueante. findById encadena con guardians reactivamente. Sin medición formal. |
| M06 | Integración | 5 | Publica StudentCreated, StudentUpdated, GuardianAdded vía RabbitMQ. WebClientConfig para consumo externo. |
| M07 | Escalabilidad | 5 | Toda configuración por ${VAR}. Perfiles dev/prod/test. Sin valores hardcodeados. |
| M08 | Auditoría | 5 | created_at, updated_at + created_by, updated_by agregados (migración V6) en students y guardians. |
| M09 | Seguridad API | 5 | OAuth2 + JWT. JwtAuthorizationHelper.isOwnDocument() protege /my-children para apoderados. |
| M10 | Transaccionalidad | 4 | Flujo reactivo: existsByCui → save → publishEvent. Errores con Mono.error(DuplicateCuiException). |

---

## SECCIÓN 3: Frontend & Experiencia de Usuario (20%) — Promedio: 4.0/5

| N° | MÉTRICA | PUNTAJE | OBSERVACIÓN |
|----|---------|:-------:|-------------|
| M01 | Integración API | 5 | Axios con interceptor JWT. Sin errores CORS en módulo students. |
| M02 | Responsive | 3 | Formulario de alumno (20+ campos) requiere scroll en móvil 360px. Funcional en escritorio. |
| M03 | Feedback UX | 4 | Toast react-hot-toast en CRUD. Loader visible durante peticiones. |
| M04 | Validación UI | 4 | Formulario valida campos requeridos antes de enviar. Mensajes en español. |
| M05 | Navegación | 4 | Rutas protegidas con guard. Redirección al login si token expira. |
| M06 | Usabilidad | 4 | Registro de alumno accesible en ≤3 clics. Vista detalle con apoderados integrada. |
| M07 | Consistencia UI | 4 | TailwindCSS uniforme. Componentes coherentes en vistas de alumnos y apoderados. |
| M08 | Soft Delete | 5 | Registros inactivos diferenciados visualmente. Restaurar funcional. |
| M09 | Accesibilidad | 3 | Contraste adecuado. Faltan aria-label en algunos inputs del formulario extenso. |
| M10 | Gestión de Rol | 4 | UI oculta opciones según rol. /my-children protegido por documento del apoderado. |

---

## SECCIÓN 4: Calidad de Software y Pruebas (20%) — Promedio: 3.4/5

| N° | MÉTRICA | PUNTAJE | OBSERVACIÓN |
|----|---------|:-------:|-------------|
| M01 | Unit Testing | 4 | 5 test files: Create, Delete, Update Student + PersistenceMapper + AppTests. Faltan tests de Guardian. |
| M02 | Integración | 2 | Sin colección Postman ni flujo encadenado multi-servicio documentado. |
| M03 | Smoke Tests | 2 | Sin script de smoke tests post-despliegue. Solo ApplicationTests verifica arranque. |
| M04 | Bug Rate | 5 | Bug (NoResourceFoundException sin handler) identificado y corregido. <2 bugs críticos al cierre. |
| M05 | Performance Test | 2 | Sin archivo JMeter ni Postman Runner. Sin prueba de carga documentada. |
| M06 | Seguridad | 5 | R2DBC parametrizado. Sin stacktrace en prod. Secrets en env vars. |
| M07 | Manejo de Logs | 4 | @Slf4j en use cases y controller. Logs en handlers. Pendiente log.info en create exitoso de Student. |
| M08 | Limpieza Código | 5 | Código bien organizado. Mappers separados Student/Guardian. Sin código muerto. Sin System.out. |
| M09 | Compatibilidad | 5 | API REST pura compatible con cualquier cliente. Headers JSON correctos. |
| M10 | Documentación QA | 2 | Sin plan de pruebas formal con PASS/FAIL. Tests JUnit como único artefacto QA. |

---

## SECCIÓN 5: Infraestructura, Seguridad y Docs (20%) — Promedio: 3.9/5

| N° | MÉTRICA | PUNTAJE | OBSERVACIÓN |
|----|---------|:-------:|-------------|
| M01 | Contenedores | 5 | Dockerfile multistage (Maven builder + JRE 17 Alpine). |
| M02 | Keycloak | 5 | SecurityConfig OAuth2 Resource Server + JWK. Realm sigei con roles mapeados. |
| M03 | Base de Datos | 5 | BD propia sigei_students (students + guardians). Flyway V1–V6. |
| M04 | HTTPS / SSL | 4 | TLS en Traefik. Microservicio en red interna Docker. |
| M05 | Monitoreo | 4 | Spring Actuator /actuator/health. Sin métricas Prometheus documentadas. |
| M06 | API Doc | 5 | springdoc-openapi-starter-webflux-ui. Swagger UI funcional. |
| M07 | Diagramas | 3 | README.md descriptivo. Sin diagrama C4 ni secuencia. |
| M08 | Diccionario BD | 3 | Migraciones Flyway documentan schema implícitamente. Sin diccionario formal. |
| M09 | Manual Usuario | 2 | Sin guía de usuario por rol documentada. |
| M10 | Pipeline CI/CD | 3 | .gitlab-ci.yml presente. Sin Jenkinsfile ni sonar-project.properties. Sin etapa docker build. |
