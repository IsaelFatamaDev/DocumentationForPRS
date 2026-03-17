# PSW - Informe de Pruebas de Software

**Integrantes:** [Completar]

**Microservicio maestro:** USERS (`vg-ms-users-management`)

**Microservicio transaccional:** STUDENTS (`vg-ms-students`)

## 1) Caso de uso (minimo 2 por microservicio)

### USERS (maestro)

1. **Registrar usuario institucional**
   - Actor: Administrador institucional.
   - Flujo principal: registra datos personales y documento del usuario; el sistema valida duplicidad de documento; genera nombre de usuario unico; guarda y publica evento de creacion.
   - Resultado: usuario creado en estado activo y disponible para autenticacion/gestion.

2. **Eliminar logicamente usuario**
   - Actor: Administrador institucional.
   - Flujo principal: solicita desactivacion por id; el sistema valida existencia; cambia estado a `INACTIVE`; actualiza fecha de modificacion; publica evento de desactivacion.
   - Resultado: usuario no se elimina fisicamente, queda inactivo para trazabilidad.

### STUDENTS (transaccional)

1. **Registrar estudiante**
   - Actor: Personal academico.
   - Flujo principal: registra CUI y datos personales/desarrollo/salud; el sistema valida duplicidad de CUI; guarda estudiante; publica evento de creacion.
   - Resultado: estudiante registrado para procesos academicos y administrativos.

2. **Eliminar logicamente estudiante**
   - Actor: Personal academico.
   - Flujo principal: solicita baja logica por id; el sistema valida existencia; cambia estado a `INACTIVE`; registra fecha de actualizacion; publica evento de eliminacion.
   - Resultado: estudiante queda inactivo sin perder historico.

## 2) Gestion de datos (descripcion y justificacion)

### USERS

- **Datos gestionados:** id, datos de identidad (tipo/numero de documento), nombres y apellidos, contacto, rol, estado, username institucional, timestamps.
- **Justificacion:**
  - El documento de identidad asegura unicidad legal del usuario.
  - El username institucional estandariza acceso y administracion de cuentas.
  - El estado activo/inactivo soporta eliminacion logica, auditoria y cumplimiento.
  - Los timestamps permiten trazabilidad operativa.

### STUDENTS

- **Datos gestionados:** id, CUI, datos personales, informacion academica (institucion/aula), datos de desarrollo, y datos de salud/emergencia, estado, timestamps.
- **Justificacion:**
  - El CUI es identificador unico del estudiante para evitar duplicidad.
  - Datos de desarrollo/salud son necesarios para atencion pedagogica y protocolos de seguridad.
  - Relacion con institucion/aula permite operaciones transaccionales del ciclo academico.
  - El estado activo/inactivo mantiene historico sin borrado fisico.

## 3) Funcionalidades que debe satisfacer cada microservicio

### USERS

- Crear usuario validando documento unico.
- Generar username institucional unico segun reglas de negocio.
- Listar/consultar usuarios por filtros (estado, rol, institucion).
- Actualizar usuario sin romper reglas de unicidad.
- Desactivar y restaurar usuario con publicacion de eventos.

### STUDENTS

- Crear estudiante validando CUI unico.
- Consultar estudiantes por id, estado, aula e institucion.
- Actualizar datos personales, academicos y de seguimiento.
- Eliminar logicamente y restaurar estudiantes.
- Publicar eventos de ciclo de vida (creado/actualizado/eliminado/restaurado).

## 4) Requisitos de calidad

- **Confiabilidad:** no permitir duplicidad de documento (USERS) ni CUI (STUDENTS).
- **Consistencia:** operaciones de cambio de estado deben persistir y publicar evento asociado.
- **Mantenibilidad:** arquitectura hexagonal con puertos/adaptadores, casos de uso desacoplados y testeables.
- **Trazabilidad:** uso de timestamps y eliminacion logica.
- **Testabilidad:** pruebas unitarias aisladas con mocks de repositorios/publicadores.
- **Manejo de errores:** excepciones de dominio claras (`Duplicate*`, `*NotFound`).

## 5) Escenarios de pruebas unitarias (4 por microservicio)

### USERS

1. Crear usuario exitosamente cuando documento y username base estan disponibles.
2. Lanzar `DuplicateDocumentNumberException` cuando el documento ya existe.
3. Eliminar logicamente usuario exitosamente (cambio a `INACTIVE` + evento).
4. Lanzar `UserNotFoundException` cuando se elimina un id inexistente.

### STUDENTS

1. Crear estudiante exitosamente cuando CUI no existe.
2. Lanzar `DuplicateCuiException` cuando el CUI ya existe.
3. Eliminar logicamente estudiante exitosamente (cambio a `INACTIVE` + evento).
4. Lanzar `StudentNotFoundException` cuando se elimina un id inexistente.

## 6) Implementacion (JUnit + Mockito)

Se implementaron pruebas unitarias para casos de uso de aplicacion (arquitectura hexagonal), usando:

- **JUnit 5** para estructura de pruebas.
- **Mockito** para simulacion de repositorios y publicadores de eventos.
- **StepVerifier** de Reactor para validar flujos reactivos (`Mono`) en resultados y errores.

## 7) Validacion de pruebas (`mvn test`)

### USERS

Comando ejecutado:

- `sh mvnw test`

Resultado:

- **BUILD SUCCESS**
- Tests run: 5
- Failures: 0
- Errors: 0
- Skipped: 1 (test de contexto deshabilitado)

### STUDENTS

Comando ejecutado:

- `sh mvnw test`

Resultado:

- **BUILD SUCCESS**
- Tests run: 5
- Failures: 0
- Errors: 0
- Skipped: 1 (test de contexto deshabilitado)

## 8) Enlace de repositorio

- Repositorio: [Completar URL]
- Rama trabajada: [Completar]
- Commit de evidencias de pruebas: [Completar SHA]

## 9) Guion base para video (10 a 15 min)

1. Presentacion del equipo y alcance (USERS maestro, STUDENTS transaccional).
2. Explicacion rapida de arquitectura y por que se prueban use cases.
3. Mostrar escenarios de prueba definidos (4 por cada microservicio).
4. Demostracion en vivo ejecutando `mvn test` en ambos servicios.
5. Revisar salida de consola y explicar que todos los tests pasan.
6. Responder preguntas de reflexion:

### 9.1 Que conceptos nuevos sobre pruebas unitarias y Mockito aprendimos

- Aislar reglas de negocio mediante mocks mejora velocidad y precision de pruebas.
- Validar flujos reactivos con `StepVerifier` permite comprobar tanto datos como errores.
- Nombres de pruebas descriptivos mejoran mantenimiento y comunicacion.

### 9.2 Que parte fue mas facil y por que

- Simular repositorios con Mockito, porque los puertos de salida estan bien definidos.
- Verificar excepciones de dominio, por tener mensajes y tipos claros.

### 9.3 Dificultades tecnicas o de coordinacion

- Los tests de contexto Spring levantaban dependencias externas (BD/Flyway), ralentizando o bloqueando ejecucion.
- Coordinacion para repartir escenarios sin duplicar esfuerzos.

### 9.4 Como se resolvio

- Se enfocaron pruebas unitarias puras sobre casos de uso.
- Se deshabilito test de contexto para esta entrega de unit tests.
- Se definio una matriz de casos por integrante y checklist de verificacion.

### 9.5 Que pruebas adicionales incluir

- Pruebas de actualizacion (documento/CUI duplicado en update).
- Pruebas de restauracion de estado.
- Pruebas de resiliencia ante fallo del publicador de eventos.
- Pruebas de adaptadores (persistencia/REST) y pruebas de integracion con Testcontainers.

### 9.6 Beneficio en entorno empresarial

- Reduce defectos en produccion.
- Permite cambios rapidos con menor riesgo (regresiones controladas).
- Mejora confianza para CI/CD y despliegues frecuentes.
- Facilita auditoria tecnica de reglas de negocio.
