# Arquitectura hexagonal, DDD y modularidad de Vallegrande Big Data

## 1. Propósito del documento

Este documento describe la arquitectura que realmente está implementada en
`vallegrande-bigdata-nuevo`, identifica los elementos de arquitectura hexagonal y
Domain-Driven Design (DDD), y diferencia claramente entre:

- elementos que ya existen en el código;
- elementos que existen de manera parcial;
- conceptos que todavía deberían incorporarse;
- decisiones recomendadas para evolucionar la plataforma.

La clasificación general del sistema es:

> Plataforma Big Data organizada como monorepo, con una API Spring Boot de tipo
> monolito modular que aplica arquitectura hexagonal, DDD táctico ligero, jobs
> Spark independientes y procesamiento de datos mediante capas Bronze, Silver,
> Quarantine y Gold.

No es actualmente una arquitectura de microservicios.

---

## 2. Resumen de evaluación

| Característica | Evaluación | Explicación breve |
|---|---|---|
| Monorepo | Sí | Frontend, backend, contratos, Spark, datos y documentación están en un repositorio. |
| Modularidad Maven | Sí | Existen módulos Maven independientes con sus propios `pom.xml`. |
| Monolito modular | Sí | Los módulos del backend se empaquetan y despliegan dentro de una aplicación Spring Boot. |
| Arquitectura hexagonal | Sí, principalmente en el backend | Se distinguen dominio, aplicación, puertos y adaptadores. |
| DDD táctico | Parcial | Hay entidades, reglas, excepciones y lenguaje de dominio, pero faltan varios patrones explícitos. |
| DDD estratégico | Inicial | Se pueden reconocer contextos, pero todavía no están formalizados como bounded contexts. |
| Microservicios | No | Los módulos de la API no se despliegan ni escalan independientemente. |
| Workers independientes | Sí | Los jobs Spark tienen puntos de entrada propios y se ejecutan en procesos separados. |
| Medallion Architecture | Sí | El pipeline maneja Bronze, Silver y Gold, además de Quarantine y Quality Gate. |
| Procesamiento batch | Sí | Spark procesa conjuntos de datos por ejecuciones o jobs. |

---

## 3. Vista general de la solución

```text
Usuario
  |
  v
Frontend React
  |
  | HTTP / SSE
  v
Backend Spring Boot: monolito modular
  |-- dataset-module
  |-- job-module
  |-- report-module
  |-- shared-kernel
  `-- backend-app
          |
          | puerto JobRunner
          v
     SparkProcessRunner
          |
          | nuevo proceso Java / spark-submit
          v
Workers Spark
  |-- job-rendimiento-academico
  `-- job-alerta-asistencia
          |
          v
Bronze -> Validation -> Quarantine/Silver -> Quality Gate -> Gold -> Export
```

La aplicación combina dos estilos:

1. La API utiliza monolito modular y arquitectura hexagonal.
2. Spark utiliza una arquitectura de pipeline por etapas y jobs especializados.

Estos estilos no se contradicen. Cada uno resuelve una necesidad distinta.

---

## 4. Modularidad del repositorio

### 4.1 Módulos principales

El `pom.xml` raíz declara:

```text
bigdata-platform
|-- contracts
|-- spark
`-- backend
```

El backend se divide en:

```text
backend
|-- shared-kernel
|-- dataset-module
|-- job-module
|-- report-module
`-- backend-app
```

Spark se divide en:

```text
spark
|-- spark-core
|-- spark-academico
|-- job-rendimiento-academico
`-- job-alerta-asistencia
```

El frontend se organiza por funcionalidades:

```text
frontend/src
|-- app
|-- features
|   |-- datasets
|   |-- jobs
|   |-- reports
|   `-- system
`-- shared
```

### 4.2 Responsabilidad de cada módulo

| Módulo | Responsabilidad |
|---|---|
| `contracts` | Contratos compartidos entre API y workers: tipos de job, etapas, estados y reglas. |
| `shared-kernel` | Elementos técnicos compartidos por la API: errores, archivos, tablas y configuración. |
| `dataset-module` | Registro, carga, almacenamiento Bronze y consulta de datasets. |
| `job-module` | Creación, cola, ejecución, seguimiento y cancelación de jobs. |
| `report-module` | Consulta de capas, calidad, dashboard y exportación. |
| `backend-app` | Ensambla los módulos y arranca la única aplicación Spring Boot. |
| `spark-core` | Motor reutilizable de pipelines, publicación, seguimiento y quality gate. |
| `spark-academico` | Extracción y validaciones específicas del dominio académico. |
| `job-rendimiento-academico` | Generación de indicadores académicos Gold. |
| `job-alerta-asistencia` | Generación de alertas de asistencia. |

### 4.3 Dirección de dependencias

La dependencia Maven principal del backend es:

```text
backend-app
    -> report-module
        -> job-module
            -> dataset-module
                -> shared-kernel
```

Esto produce modularidad real de compilación. Sin embargo, los módulos todavía no
son desplegables de forma independiente.

---

## 5. Por qué es un monolito modular

`backend-app` contiene el único punto de arranque Spring Boot:

```java
@SpringBootApplication
public class BigDataApplication {
    public static void main(String[] args) {
        SpringApplication.run(BigDataApplication.class, args);
    }
}
```

Los módulos `dataset`, `job` y `report`:

- funcionan dentro de la misma JVM;
- se empaquetan conjuntamente;
- se despliegan como una unidad;
- se comunican con llamadas Java internas;
- comparten el ciclo de vida de la aplicación;
- no necesitan comunicación HTTP entre ellos.

Por ello, la clasificación precisa es **monolito modular**.

Un monolito modular no significa que todo esté mezclado. Significa que existen
límites internos claros, aunque el despliegue continúe siendo único.

---

## 6. Por qué no es una arquitectura de microservicios

Para considerar `dataset`, `job` y `report` como microservicios deberían poseer,
entre otras cosas:

- puntos de arranque independientes;
- artefactos desplegables independientes;
- comunicación remota mediante API, mensajería o eventos;
- capacidad de escalar individualmente;
- ciclo de versiones propio;
- propiedad clara de sus datos;
- tolerancia a fallos de red y consistencia distribuida.

El proyecto no implementa actualmente esas características para los módulos de la
API. Por tanto, no deben presentarse como microservicios.

Los ejecutables Spark sí son procesos independientes, pero se clasifican mejor
como **workers batch** o **aplicaciones Spark**, no como microservicios. Que un
componente se ejecute en otro proceso no basta para convertirlo en microservicio.

La arquitectura completa puede describirse como un sistema distribuido pequeño:

```text
API modular + workers Spark + almacenamiento de datos
```

---

## 7. Arquitectura hexagonal

La arquitectura hexagonal busca que el negocio no dependa de HTTP, bases de datos,
archivos, Spark ni frameworks. Las tecnologías externas se conectan mediante
puertos y adaptadores.

### 7.1 Capas implementadas

Cada módulo funcional del backend utiliza esta estructura:

```text
modulo
|-- domain
|   |-- model
|   `-- exception
|-- application
|   |-- port
|   |   |-- in
|   |   `-- out
|   `-- usecase
`-- infrastructure
    |-- adapter
    |   |-- in
    |   `-- out
    `-- config
```

### 7.2 Dominio

El dominio contiene conceptos y reglas independientes de la tecnología.

Ejemplos actuales:

- `Dataset`
- `DatasetOrigin`
- `Job`
- `JobStatus`
- `Layer`
- `TableLocation`
- `ExportFormat`
- excepciones específicas del dominio

`Job` contiene comportamiento relevante:

```java
public Job start(String detail)
public Job finish(JobStatus result, String detail)
public void requireActive()
public boolean queued()
public boolean finished()
public boolean published()
```

Estas operaciones protegen reglas como:

- solo un job en cola puede iniciar;
- un job solo termina con un estado terminal;
- un job finalizado no puede volver a modificarse como si estuviera activo;
- solo `SUCCEEDED` representa resultados publicados.

### 7.3 Puertos de entrada

Los puertos de entrada expresan las operaciones que el sistema ofrece:

```text
DatasetRegistrationUseCase
DatasetQueryUseCase
FindDatasetUseCase

SubmitJobUseCase
JobQueryUseCase
JobControlUseCase
FindJobUseCase
PlatformStatusUseCase

DashboardUseCase
TableQueryUseCase
QualityReportUseCase
ExportUseCase
```

Un controlador REST depende de estas interfaces y no de la persistencia concreta.

### 7.4 Adaptadores de entrada

Los adaptadores de entrada traducen una petición externa a una llamada de la
aplicación:

| Adaptador | Función |
|---|---|
| `DatasetRest` | Recibe cargas y consultas HTTP de datasets. |
| `JobRest` | Recibe solicitudes de ejecución, consulta y cancelación. |
| `SystemRest` | Expone el estado de la plataforma. |
| `ReportRest` | Expone tablas, calidad, dashboard y exportaciones. |

REST es una tecnología de entrada, no el centro del dominio.

### 7.5 Casos de uso

Los servicios de aplicación coordinan el flujo sin conocer los detalles de
infraestructura.

Ejemplo: `SubmitJobService` realiza conceptualmente:

```text
Validar tolerancia y tiempo de UI
  -> localizar Dataset
  -> comprobar disponibilidad del worker
  -> crear Job en estado QUEUED
  -> colocarlo en la cola
```

Otros casos de uso son:

- `DatasetRegistrationService`
- `DatasetQueryService`
- `JobQueryService`
- `JobControlService`
- `PlatformStatusService`
- `DashboardService`
- `QualityReportService`
- `TableQueryService`
- `ExportService`

### 7.6 Puertos de salida

Los puertos de salida declaran necesidades del núcleo:

| Puerto | Necesidad expresada |
|---|---|
| `DatasetRepository` | Guardar y recuperar datasets. |
| `BronzeStorage` | Guardar archivos de entrada. |
| `SampleCatalog` | Acceder a conjuntos de ejemplo. |
| `JobRepository` | Guardar y recuperar jobs. |
| `JobRunner` | Ejecutar y controlar un worker. |
| `JobOutputs` | Leer eventos y resultados de una ejecución. |
| `SparkHistory` | Administrar el servidor de historial Spark. |
| `LayerStorage` | Consultar tablas de las capas del pipeline. |

### 7.7 Adaptadores de salida

Implementaciones actuales:

```text
DatasetRepository <- FileDatasetRepository
BronzeStorage     <- FileBronzeStorage
SampleCatalog     <- FileSampleCatalog

JobRepository     <- FileJobRepository
JobRunner         <- SparkProcessRunner
JobOutputs        <- FileJobOutputs
SparkHistory      <- SparkHistoryServer

LayerStorage      <- FileLayerStorage
```

Por esta separación podría agregarse, por ejemplo:

```text
JobRepository     <- PostgresJobRepository
DatasetRepository <- PostgresDatasetRepository
BronzeStorage     <- S3BronzeStorage
LayerStorage      <- S3LayerStorage
JobRunner         <- RemoteSparkSubmitRunner
```

sin introducir PostgreSQL, S3 o la ejecución remota dentro del dominio.

### 7.8 Regla de dependencias

La dirección deseada es:

```text
Infrastructure -> Application -> Domain
```

El dominio no debe conocer infraestructura. La aplicación conoce puertos, no sus
implementaciones. La infraestructura conoce las interfaces que implementa.

El proyecto incluye `ArchitectureTests`, que verifica, entre otras reglas, que:

- el dominio no dependa de aplicación o infraestructura;
- el dominio no importe Spring, Jackson o Reactor;
- la aplicación no dependa de infraestructura;
- la aplicación no acceda directamente a archivos.

Esto aporta una verificación automática y hace que la arquitectura hexagonal no
sea únicamente una convención de nombres.

### 7.9 Alcance real de la arquitectura hexagonal

El backend aplica claramente el patrón. Spark utiliza abstracciones y contratos,
pero su diseño principal es un pipeline. No es necesario forzar que cada clase
Spark replique las mismas carpetas del backend.

---

## 8. Domain-Driven Design

DDD no es una estructura de carpetas ni un sinónimo de arquitectura hexagonal.

- La arquitectura hexagonal controla dependencias y tecnologías externas.
- DDD busca modelar el conocimiento, lenguaje y reglas del negocio.

Ambos enfoques pueden utilizarse juntos, como ocurre parcialmente en este proyecto.

### 8.1 Evaluación general

El sistema contiene **DDD táctico ligero** porque existen:

- modelos con identidad;
- comportamiento de dominio;
- estados y políticas;
- excepciones específicas;
- repositorios expresados como interfaces;
- servicios de aplicación;
- vocabulario consistente en código y API.

El DDD estratégico todavía es inicial porque no se han formalizado completamente:

- core domain;
- subdominios;
- bounded contexts;
- context map;
- equipos propietarios;
- contratos de integración entre todos los dominios futuros.

---

## 9. Core Domain, Supporting Subdomains y Generic Subdomains

### 9.1 Core Domain actual

El **Core Domain** es la capacidad que aporta el valor diferencial principal del
sistema. No tiene que coincidir con el componente más grande ni con el más técnico.

Para la demostración académica actual, el Core Domain puede definirse como:

> Procesamiento confiable de información académica para producir indicadores de
> rendimiento y alertas de asistencia, aplicando validaciones y reglas de calidad.

Sus componentes principales son:

- validación de estudiantes, cursos, notas, semestres y asistencia;
- reglas de consistencia académica;
- cálculo de rendimiento;
- clasificación de aprobación, riesgo e inhabilitación;
- generación de alertas de asistencia;
- publicación de tablas analíticas Gold.

Código relacionado:

```text
spark/spark-academico
spark/job-rendimiento-academico
spark/job-alerta-asistencia
contracts/QualityRule
contracts/GradingPolicy
```

### 9.2 Supporting Subdomains

Los subdominios de soporte hacen posible el Core Domain, pero no representan la
principal diferenciación del producto:

| Supporting Subdomain | Responsabilidad |
|---|---|
| Gestión de datasets | Registrar, identificar y almacenar entradas. |
| Orquestación de jobs | Encolar, ejecutar, cancelar y observar procesos. |
| Calidad de datos | Medir rechazo, bloquear publicaciones y producir reportes. |
| Publicación y consulta | Exponer Silver, Quarantine y Gold. |
| Reportes y exportación | Consultar y exportar CSV, JSON y Parquet. |

### 9.3 Generic Subdomains

Son capacidades necesarias, pero genéricas y reemplazables:

- API HTTP;
- manejo de archivos;
- serialización JSON;
- configuración de Spring;
- historial de Spark;
- almacenamiento local;
- paginación;
- logging;
- CORS y configuración web.

### 9.4 Evolución hacia Amankay, SUSALUD e IPD

Cuando la plataforma soporte los tres proyectos, no debería existir un único Core
Domain universal. Cada producto tendrá el suyo:

| Producto | Posible Core Domain |
|---|---|
| Amankay | Detección y priorización territorial de emergencias. |
| SUSALUD | Identificación explicable y auditable de señales de riesgo sanitario. |
| IPD | Priorización de infraestructura y recursos deportivos basada en evidencia. |

La orquestación Spark, ingesta, almacenamiento y observabilidad serían capacidades
compartidas de plataforma, no el Core Domain de cada producto.

---

## 10. Lenguaje ubicuo

El lenguaje ubicuo es un vocabulario común utilizado por desarrolladores, usuarios
y expertos del negocio. Los términos deben significar lo mismo en conversaciones,
documentos, código, endpoints y reportes.

### 10.1 Glosario actual propuesto

| Término | Significado dentro del sistema |
|---|---|
| Dataset | Lote identificado de tablas de entrada que será procesado. |
| Dataset de muestra | Datos incluidos en el proyecto para demostración o pruebas. |
| Dataset cargado | Datos proporcionados mediante la API por un usuario. |
| Job | Solicitud identificada de procesamiento de un dataset. |
| Tipo de job | Análisis que se ejecutará, por ejemplo rendimiento o alerta de asistencia. |
| Worker | Proceso encargado de ejecutar un job Spark. |
| Pipeline | Secuencia ordenada de etapas que transforma un dataset. |
| Etapa | Unidad del pipeline: Bronze, Validation, Quarantine, Silver, Quality Gate, Gold o Export. |
| Bronze | Copia de entrada conservada antes de las transformaciones analíticas. |
| Validation | Aplicación de reglas estructurales, de validez, integridad y consistencia. |
| Quarantine | Registros rechazados acompañados de la causa de rechazo. |
| Silver | Registros aceptados, limpios y normalizados. |
| Quality Gate | Decisión que permite o bloquea la publicación según la calidad del lote. |
| Gold | Datos agregados y productos analíticos listos para consumo. |
| Export | Publicación en formatos consumibles, como CSV, JSON o Parquet. |
| Regla de calidad | Restricción con dimensión y descripción que determina la validez de un registro. |
| Ratio de rechazo | Proporción de registros rechazados frente a los recibidos. |
| Tolerancia de rechazo | Máximo ratio aceptado para permitir que el pipeline publique Gold. |
| Resultado publicado | Resultado de un job exitoso disponible para consulta. |
| Alerta de asistencia | Resultado analítico que identifica riesgo por inasistencias. |
| Rendimiento académico | Resultado analítico derivado de notas, asistencia y evaluaciones. |

### 10.2 Estados del lenguaje

Estados de un job:

```text
QUEUED
RUNNING
SUCCEEDED
QUALITY_FAILED
FAILED
TIMED_OUT
CANCELLED
INTERRUPTED
```

Estados de una etapa:

```text
RUNNING
COMPLETED
BLOCKED
SKIPPED
FAILED
```

Clasificaciones académicas:

```text
APROBADO
DESAPROBADO
INHABILITADO
PENDIENTE
EN_RIESGO
NORMAL
```

### 10.3 Recomendación de consistencia

Debe evitarse usar distintas palabras para el mismo concepto. Por ejemplo:

- no alternar indiscriminadamente entre `job`, proceso, tarea y ejecución;
- usar `Dataset` para el lote registrado y `tabla` para una estructura interna;
- reservar `rechazado` para registros y `QUALITY_FAILED` para el resultado del job;
- diferenciar `estado del job` de `estado de una etapa`;
- utilizar `publicado` solamente cuando los resultados Gold sean consumibles.

---

## 11. Bounded Contexts

Un bounded context es un límite dentro del cual un modelo y sus términos mantienen
un significado consistente.

El proyecto no los declara formalmente, pero pueden reconocerse estos candidatos:

### 11.1 Dataset Catalog and Ingestion Context

Responsable de:

- registrar datasets;
- recibir archivos CSV;
- calcular metadatos;
- evitar duplicados mediante `sha256`;
- almacenar la capa Bronze;
- localizar datasets.

Modelo principal: `Dataset`.

### 11.2 Job Orchestration Context

Responsable de:

- crear jobs;
- gestionar estados;
- manejar la cola;
- ejecutar Spark;
- cancelar ejecuciones;
- proporcionar progreso y logs.

Modelo principal: `Job`.

### 11.3 Reporting and Publication Context

Responsable de:

- localizar tablas procesadas;
- consultar capas;
- comprobar disponibilidad de Gold;
- construir dashboards;
- exponer reportes de calidad;
- exportar resultados.

Modelos principales: `Layer`, `TableLocation` y `ExportFormat`.

### 11.4 Academic Analytics Context

Responsable de:

- validar entidades académicas;
- calcular indicadores;
- evaluar políticas de calificación;
- generar alertas de asistencia;
- producir tablas Gold.

Es el contexto más cercano al Core Domain de la demostración actual.

### 11.5 Shared Kernel y contratos

`contracts` funciona como un contrato compartido entre la API y Spark. Contiene:

- `JobType`;
- `PipelineStage`;
- `StageStatus`;
- `StageEvent`;
- `QualityRule`;
- `GradingPolicy`;
- `WorkerProtocol`.

Debe mantenerse pequeño. Introducir demasiadas reglas específicas en un shared
kernel aumenta el acoplamiento entre contextos.

### 11.6 Context map actual

```text
Dataset Context
      |
      | Dataset identificado
      v
Job Orchestration Context
      |
      | JobType + protocolo + eventos
      v
Academic Analytics Context / Spark
      |
      | Silver + Quarantine + Gold + quality.json
      v
Reporting and Publication Context
```

Las integraciones actuales son principalmente llamadas Java y contratos de
archivos. En una evolución futura podrían convertirse en eventos o APIs explícitas.

---

## 12. Entidades

Una entidad se distingue por su identidad y continuidad en el tiempo, aunque
cambien algunos de sus atributos.

### 12.1 Entidad `Dataset`

`Dataset` posee un `id` generado mediante UUID y metadatos asociados:

```text
id
name
origin
rows
totalRows
sizeBytes
sha256
createdAt
```

Se considera una entidad porque dos datasets con nombres o contenidos parecidos
continúan siendo registros diferentes si poseen identidades diferentes.

Además, su método `register` garantiza que `totalRows` se derive de las filas por
tabla.

### 12.2 Entidad `Job`

`Job` posee identidad y ciclo de vida:

```text
QUEUED -> RUNNING -> SUCCEEDED
                  -> QUALITY_FAILED
                  -> FAILED
                  -> TIMED_OUT
                  -> CANCELLED
                  -> INTERRUPTED
```

Es la entidad con comportamiento de dominio más claro del backend.

---

## 13. Agregados y Aggregate Roots

El código no marca explícitamente agregados. Sin embargo, existen dos candidatos
razonables.

### 13.1 `Job` como Aggregate Root candidato

`Job` controla su transición de estados y protege invariantes. Podría formalizarse
como raíz del agregado `JobExecution`.

Invariantes actuales:

- un job nuevo comienza en `QUEUED`;
- solo un job en cola puede comenzar;
- un resultado de finalización debe ser terminal;
- un job terminado no puede tratarse como activo;
- la publicación corresponde a `SUCCEEDED`.

Posibles componentes futuros del agregado:

- `JobId`;
- `DatasetId`;
- `JobStatus`;
- `RejectionTolerance`;
- `JobExecutionTime`;
- resumen de etapas.

Los logs completos y archivos de salida no deberían cargarse dentro del agregado;
son datos voluminosos administrados por almacenamiento especializado.

### 13.2 `Dataset` como Aggregate Root candidato

`Dataset` podría ser la raíz del agregado de catálogo. Sus invariantes podrían
incluir:

- identidad obligatoria;
- nombre válido;
- origen permitido;
- hash válido;
- tamaño no negativo;
- filas totales iguales a la suma por tabla;
- conjunto mínimo de tablas según el tipo de procesamiento.

Actualmente algunas de estas validaciones viven en casos de uso o servicios de
ingesta, por lo que el agregado todavía no está formalizado completamente.

---

## 14. Value Objects

Un Value Object:

- se identifica por su valor, no por un ID;
- normalmente es inmutable;
- valida su propio contenido;
- representa un concepto del dominio;
- evita que reglas importantes queden representadas como `String`, `double` o
  `int` sin protección.

### 14.1 Value Objects o aproximaciones existentes

| Tipo actual | Evaluación |
|---|---|
| `JobStatus` | Value Object enumerado que representa el estado permitido. |
| `DatasetOrigin` | Value Object enumerado para el origen del dataset. |
| `Layer` | Value Object enumerado con parseo y comportamiento de dominio. |
| `ExportFormat` | Value Object enumerado con extensión y tipo de contenido. |
| `JobType` | Value Object enumerado y contrato de ejecución. |
| `PipelineStage` | Value Object enumerado para las etapas permitidas. |
| `StageStatus` | Value Object enumerado para el estado de una etapa. |
| `QualityRule` | Value Object enumerado con dimensión y descripción. |
| `TableLocation` | Buen candidato ya representado como `record` inmutable. |

`Layer` es un buen ejemplo porque, además de enumerar valores, sabe:

- convertir un código externo en una capa válida;
- indicar si es una capa fuente;
- indicar si requiere un job publicado.

`ExportFormat` encapsula:

- extensión;
- content type;
- validación del formato solicitado.

### 14.2 Elementos que no son Value Objects de dominio

Que una clase sea un `record` no la convierte automáticamente en Value Object.

Por ejemplo:

- `Job` es una entidad porque tiene identidad y ciclo de vida;
- `Dataset` es una entidad porque tiene identidad;
- `JobDetail` es un DTO de aplicación;
- `TableQuery` es un DTO de consulta;
- `ErrorResponse` es un DTO de infraestructura;
- `WorkerConfig` es configuración técnica;
- `BronzeTables` y `GoldTables` son estructuras del pipeline.

### 14.3 Value Objects recomendados

El modelo utiliza todavía varios tipos primitivos. Se recomienda incorporar:

#### `JobId`

```java
public record JobId(UUID value) {
    public JobId {
        Objects.requireNonNull(value);
    }
}
```

Evitaría confundir un ID de job con un ID de dataset.

#### `DatasetId`

```java
public record DatasetId(UUID value) {
    public DatasetId {
        Objects.requireNonNull(value);
    }
}
```

#### `RejectionRatio`

```java
public record RejectionRatio(double value) {
    public RejectionRatio {
        if (!Double.isFinite(value) || value < 0 || value > 1) {
            throw new IllegalArgumentException("El ratio debe estar entre 0 y 1");
        }
    }
}
```

Actualmente esta regla aparece como validación de un `double`. El Value Object
evitaría crear ratios inválidos en cualquier parte del sistema.

#### `AcademicScore`

```java
public record AcademicScore(double value) {
    public AcademicScore {
        if (value < GradingPolicy.MIN_SCORE || value > GradingPolicy.MAX_SCORE) {
            throw new IllegalArgumentException("La nota debe estar entre 0 y 20");
        }
    }
}
```

#### `AttendanceRatio`

Representaría y validaría un porcentaje de inasistencia entre `0` y `1`, y podría
contener operaciones como `isRisk()` o `isDisqualified()`.

#### `DatasetName`

Evitaría nombres vacíos, espacios sin contenido o longitudes no permitidas.

#### `ContentHash`

Encapsularía el SHA-256 y garantizaría formato y normalización.

#### `TableName`

Evitaría utilizar nombres arbitrarios al localizar o exportar tablas.

#### `JobTimeout`

Encapsularía una duración positiva y los máximos permitidos.

### 14.4 Prioridad recomendada

No es necesario crear Value Objects para cada dato. La prioridad debería ser:

1. `JobId` y `DatasetId`, para evitar confusión de identificadores.
2. `RejectionRatio`, porque contiene una invariante importante.
3. `AcademicScore` y `AttendanceRatio`, porque pertenecen al Core Domain.
4. `ContentHash` y `DatasetName`, para fortalecer la ingesta.

---

## 15. Servicios de dominio, servicios de aplicación y políticas

### 15.1 Servicios de aplicación existentes

Los siguientes coordinan casos de uso y, por tanto, son servicios de aplicación:

- `DatasetRegistrationService`
- `DatasetQueryService`
- `SubmitJobService`
- `JobQueryService`
- `JobControlService`
- `PlatformStatusService`
- `DashboardService`
- `QualityReportService`
- `TableQueryService`
- `ExportService`

No deberían llamarse servicios de dominio porque coordinan puertos, repositorios,
DTO y operaciones externas.

### 15.2 Políticas de dominio existentes

`GradingPolicy` reúne constantes relacionadas con calificación y riesgo:

```text
nota mínima: 0
nota máxima: 20
nota aprobatoria: 13
inasistencia máxima: 30 %
riesgo de inasistencia: 20 %
rechazo predeterminado: 5 %
```

Actualmente es una clase de constantes. Puede considerarse una política inicial,
pero podría evolucionar hacia objetos configurables por institución o periodo.

`QualityRule` representa reglas nombradas y clasificadas por dimensión:

- completitud;
- validez;
- unicidad;
- integridad;
- consistencia.

### 15.3 Servicios de dominio futuros

Si las reglas aumentan, podrían aparecer servicios como:

```text
AcademicStandingPolicy
AttendanceRiskPolicy
DatasetAcceptancePolicy
QualityGatePolicy
```

Solo deberían crearse cuando una operación de negocio no pertenezca naturalmente
a una entidad o Value Object.

---

## 16. Repositorios DDD

Los repositorios presentan a la aplicación una colección conceptual de agregados,
sin exponer cómo se almacenan.

Ejemplos existentes:

```text
DatasetRepository
JobRepository
```

La implementación actual utiliza archivos:

```text
FileDatasetRepository
FileJobRepository
```

Una migración a PostgreSQL debería agregar adaptadores, no reemplazar el puerto:

```text
DatasetRepository <- PostgresDatasetRepository
JobRepository     <- PostgresJobRepository
```

De esta manera los casos de uso y el dominio permanecen estables.

`BronzeStorage`, `LayerStorage` y `JobOutputs` son puertos de almacenamiento, pero
no todos deben llamarse repositorios DDD: algunos gestionan archivos analíticos y
resultados, no agregados de dominio.

---

## 17. Domain Events

Actualmente se registran `StageEvent`, pero estos eventos funcionan principalmente
como eventos técnicos de progreso del pipeline. No existe todavía un mecanismo
formal de Domain Events dentro del backend.

Eventos de dominio posibles:

```text
DatasetRegistered
JobQueued
JobStarted
JobSucceeded
JobFailed
QualityGateRejected
AcademicRiskDetected
AttendanceAlertGenerated
GoldResultsPublished
```

Un Domain Event debe expresar un hecho relevante que ya ocurrió. No debe nombrarse
como una orden. Por ejemplo, `JobSucceeded` expresa un hecho; `RunJob` sería un
comando.

La incorporación de eventos sería útil si aparecen:

- notificaciones;
- auditoría;
- integración con otros sistemas;
- cola durable;
- varios consumidores de un resultado;
- separación futura en servicios.

No es obligatorio introducir Kafka para utilizar Domain Events. Primero pueden
implementarse dentro del monolito y persistirse mediante un outbox en PostgreSQL.

---

## 18. Invariantes de dominio identificadas

Una invariante es una condición que siempre debe ser verdadera dentro de un modelo.

### 18.1 Jobs

- Un job nuevo se crea en estado `QUEUED`.
- Solo un job `QUEUED` puede pasar a `RUNNING`.
- Un job solo finaliza en un estado terminal.
- Un job finalizado no puede tratarse como activo.
- Solo un job `SUCCEEDED` tiene resultados Gold publicados.
- `maxRejectedRatio` debe estar entre `0` y `1`.
- `sparkUiSeconds` debe estar dentro del máximo permitido.

### 18.2 Datasets

- El total de filas es la suma de filas de sus tablas.
- El dataset tiene una identidad única.
- El origen solo puede ser `SAMPLE` o `UPLOAD`.
- Los archivos CSV deben ser estructuralmente válidos.
- El hash identifica el contenido cargado.

### 18.3 Pipeline y calidad

- La tolerancia debe estar entre `0` y `1`.
- Las etapas se ejecutan en orden.
- Si el Quality Gate bloquea el lote, las etapas restantes se omiten.
- Los registros inválidos se publican en Quarantine.
- Gold solo debe considerarse publicado si la calidad permitió continuar.

### 18.4 Dominio académico

- Una nota válida está entre `0` y `20`.
- La nota aprobatoria es `13` según la política actual.
- Los pesos deben estar entre `0` exclusivo y `1` inclusivo.
- Los estudiantes, cursos y semestres referenciados deben existir.
- Un curso debe pertenecer al semestre correspondiente.
- Una fecha académica debe caer dentro de su periodo.
- Los estados de asistencia deben pertenecer al catálogo permitido.

---

## 19. Arquitectura del pipeline Spark

El pipeline utiliza las etapas:

```text
BRONZE
  -> VALIDATION
  -> QUARANTINE
  -> SILVER
  -> QUALITY_GATE
  -> GOLD
  -> EXPORT
```

`Pipeline` recibe una lista inmutable de `PipelineStep` y las ejecuta en orden.
Cada paso devuelve un `StepResult` que indica:

- filas procesadas;
- detalle;
- si el pipeline debe continuar.

`PipelineContext` mantiene los resultados intermedios:

- `BronzeTables`;
- `ValidationResult`;
- `QualityReport`;
- `GoldTables`.

Este diseño combina:

- Pipeline Pattern;
- Strategy mediante implementaciones de `PipelineStep`;
- Template/orchestration en `Pipeline`;
- Medallion Architecture para las capas de datos;
- Quality Gate para controlar la publicación.

No debe confundirse la Medallion Architecture con DDD: la primera organiza la
transformación y calidad de datos; DDD organiza el modelo del negocio.

---

## 20. Fortalezas actuales

- Separación clara entre dominio, aplicación e infraestructura.
- Puertos de entrada y salida explícitos.
- Persistencia sustituible mediante adaptadores.
- Dominio sin anotaciones de Spring.
- Pruebas de reglas arquitectónicas.
- Módulos Maven con responsabilidades diferenciadas.
- Workers Spark separados de la API.
- Estados e invariantes en `Job`.
- Reglas académicas y de calidad con nombres del negocio.
- Pipeline reutilizable y jobs especializados.
- Capas Bronze, Silver, Quarantine y Gold explícitas.
- Base apropiada para evolucionar sin iniciar con demasiados microservicios.

---

## 21. Limitaciones actuales

- Los bounded contexts no están declarados formalmente.
- El Core Domain no estaba documentado explícitamente.
- `Job` y `Dataset` utilizan IDs como `String` en lugar de Value Objects.
- Ratios, notas y tiempos utilizan tipos primitivos.
- `GradingPolicy` es una clase de constantes y no una política configurable.
- No existen Domain Events formales en la API.
- No existe persistencia transaccional ni outbox.
- Algunos contratos académicos están compartidos globalmente y podrían acoplar
  futuros dominios.
- La cadena Maven `report -> job -> dataset` puede crear dependencia excesiva si
  los módulos siguen creciendo.
- El almacenamiento local es adecuado para demostración, pero no para múltiples
  instancias concurrentes.
- Los módulos no tienen autonomía de despliegue, por lo que no son microservicios.

---

## 22. Evolución recomendada

### Etapa 1: consolidar el monolito modular

1. Mantener la API como un solo despliegue.
2. Documentar el lenguaje ubicuo con los usuarios del negocio.
3. Formalizar los cuatro bounded contexts propuestos.
4. Añadir `JobId`, `DatasetId` y `RejectionRatio`.
5. Mantener las pruebas arquitectónicas.
6. Evitar dependencias directas nuevas entre infraestructuras de módulos.

### Etapa 2: persistencia productiva

1. Implementar repositorios PostgreSQL como adaptadores.
2. Mantener archivos o S3/MinIO para datos Bronze, Silver y Gold.
3. Agregar migraciones versionadas.
4. Incorporar una cola durable basada inicialmente en PostgreSQL.
5. Agregar auditoría y outbox de eventos.

### Etapa 3: dominios de producto

1. Crear contextos separados para Amankay, SUSALUD e IPD.
2. Definir un lenguaje ubicuo por producto.
3. Mantener la plataforma Spark como capacidad compartida.
4. Evitar colocar reglas específicas de los tres productos en `shared-kernel`.
5. Crear contratos de integración explícitos y versionados.

### Etapa 4: extraer servicios solo cuando exista una razón

Un módulo debería convertirse en servicio independiente únicamente si aparece una
necesidad medible, por ejemplo:

- escalado muy diferente;
- propietario o equipo diferente;
- aislamiento de datos o seguridad;
- disponibilidad independiente;
- ciclo de despliegue independiente;
- carga que perjudica al resto de la API.

No debería dividirse el sistema solamente para poder llamarlo microservicios.

---

## 23. Forma recomendada de presentar el proyecto

### Versión corta

> Vallegrande Big Data es una plataforma batch construida como monorepo. Su API es
> un monolito modular Spring Boot que aplica arquitectura hexagonal y DDD táctico
> ligero. Los procesos analíticos se ejecutan mediante workers Spark independientes
> y emplean una arquitectura Medallion con Bronze, Silver, Quarantine y Gold.

### Versión académica ampliada

> La solución separa dominio, aplicación e infraestructura utilizando puertos y
> adaptadores. Los módulos Dataset, Job y Report representan capacidades del
> negocio y se integran dentro de un único despliegue Spring Boot. El dominio
> académico constituye el Core Domain actual, mientras que la ingesta, la
> orquestación y la publicación actúan como subdominios de soporte. La solución
> emplea DDD táctico de manera parcial mediante entidades, repositorios, políticas,
> excepciones e invariantes, aunque todavía debe formalizar Value Objects, bounded
> contexts y Domain Events. Los jobs Spark son workers batch separados, no
> microservicios.

---

## 24. Conclusión

La definición técnicamente más precisa del proyecto es:

> **Monorepo de una plataforma Big Data con backend de monolito modular y
> arquitectura hexagonal, DDD táctico ligero, workers Spark batch desacoplados y
> arquitectura de datos Medallion.**

El backend sí demuestra arquitectura hexagonal mediante puertos, adaptadores,
casos de uso, dominio puro y pruebas de dependencias. El proyecto utiliza elementos
de DDD, pero todavía no debe afirmarse que aplica DDD completo. Su Core Domain
actual es el análisis académico confiable, mientras que dataset, jobs, calidad y
reportes proporcionan capacidades de soporte.

La modularidad existente permite evolucionar hacia Amankay, SUSALUD e IPD y, si en
el futuro existe una necesidad operativa real, extraer determinados módulos como
servicios independientes sin comenzar prematuramente con una arquitectura de
microservicios.
