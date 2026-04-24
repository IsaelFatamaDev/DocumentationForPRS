# Guía de Investigación — Calidad y Pruebas en Microservicios

### Proyecto: `vg-ms-user-management` | AS232S5 PRS1

---

## Punto 1 — Pruebas Parametrizadas

### ¿Qué es una prueba parametrizada?

Una **prueba parametrizada** es una prueba unitaria que se ejecuta múltiples veces con distintos conjuntos de datos de entrada, sin necesidad de duplicar el código del test. Se define una sola lógica de verificación y JUnit 5 la repite por cada fila/valor del proveedor de datos.

**¿Por qué usarlas?**

- Eliminan la duplicación: en vez de 5 tests casi idénticos se escribe 1.
- Documentan los límites del comportamiento: cada parámetro es un escenario.
- Facilitan agregar nuevos casos de prueba con una sola línea.

### Anotaciones disponibles en JUnit 5

| Anotación | Fuente de datos | Cuándo usarla |
|---|---|---|
| `@ValueSource` | Lista literal de primitivos o Strings | Escenarios con un único argumento variable |
| `@CsvSource` | Filas CSV embebidas en el código | Múltiples argumentos por escenario |
| `@CsvFileSource` | Archivo `.csv` externo | Muchos escenarios / datos externos |
| `@EnumSource` | Todos o un subconjunto de valores de un enum | Probar cada variante de un enum |
| `@MethodSource` | Método estático que retorna `Stream` | Objetos complejos como argumentos |

### Implementación en este proyecto

Se implementaron **dos clases** de pruebas parametrizadas:

#### `UpdateUserUseCaseImplParameterizedTest`

Ubicación: `src/test/java/.../application/usecases/UpdateUserUseCaseImplParameterizedTest.java`

| Test | Anotación | Escenarios |
|---|---|---|
| `execute_ShouldUpdateUserRoleCorrectly_ForDifferentRoles` | `@EnumSource(UserRole.class)` | Actualizar al usuario con cada uno de los 10 roles posibles |
| `execute_ShouldUpdateEmailAndPreserveOtherFields` | `@ValueSource(strings = {...})` | Actualizar email con 3 direcciones distintas verificando que los demás campos se preserven |

**Caso de uso elegido:** `UpdateUserUseCaseImpl.execute(id, updateData)` — se eligió porque concentra reglas de negocio claras (fusión de campos, timestamp de actualización) que se pueden verificar con distintos valores.

#### `CreateUserUseCaseImplParameterizedTest` *(nuevo)*

Ubicación: `src/test/java/.../application/usecases/CreateUserUseCaseImplParameterizedTest.java`

| Test | Anotación | Escenarios |
|---|---|---|
| `execute_ShouldGenerateExpectedUsername_ForDifferentNames` | `@CsvSource` | 4 combinaciones de nombres (simple, con tildes, con ñ, compuesto) para verificar la normalización del username |
| `execute_ShouldThrowDuplicateDocumentNumberException_WhenDocumentAlreadyExists` | `@ValueSource` | 4 DNIs distintos ya existentes para verificar que siempre se lanza la excepción de duplicado |

**Caso de uso elegido:** `CreateUserUseCaseImpl.execute(user)` — la lógica de generación de username normaliza acentos y caracteres especiales, lo que hace esencial probarla con nombres variados. El rechazo por DNI duplicado es una regla de negocio crítica que debe cumplirse para cualquier documento.

---

## Punto 2 — Cobertura de código con JaCoCo

### ¿Qué es la cobertura de código?

La **cobertura de código** (code coverage) es una métrica que indica qué porcentaje del código fuente fue ejecutado durante las pruebas automatizadas. Responde a la pregunta: *¿mis tests realmente ejercen la lógica de producción?*

**¿Por qué es importante?**

- Detecta código muerto o lógica nunca probada.
- Reduce el riesgo de introducir bugs en código sin cobertura.
- Requerida por herramientas de calidad como SonarQube para calcular el quality gate.
- Es un indicador objetivo del nivel de madurez de las pruebas.

> La cobertura es necesaria pero no suficiente: 100 % de cobertura no garantiza la ausencia de bugs; las aserciones importan tanto como la ejecución.

### Tipos de cobertura

| Tipo | Descripción |
|---|---|
| **Instrucciones** | Porcentaje de instrucciones bytecode ejecutadas |
| **Ramas (Branches)** | Porcentaje de ramas `if/else`, `switch`, ternarios evaluadas |
| **Líneas** | Porcentaje de líneas de código ejecutadas |
| **Métodos** | Porcentaje de métodos invocados al menos una vez |
| **Clases** | Porcentaje de clases instanciadas o cargadas |

### Configuración de JaCoCo en Maven (este proyecto)

En el archivo `pom.xml` se declara el plugin:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <!-- Prepara el agente JaCoCo antes de ejecutar tests -->
        <execution>
            <id>prepare-agent</id>
            <goals><goal>prepare-agent</goal></goals>
        </execution>
        <!-- Genera el reporte HTML/XML después de los tests -->
        <execution>
            <id>report</id>
            <phase>verify</phase>
            <goals><goal>report</goal></goals>
        </execution>
        <!-- Quality gate local: falla el build si no se alcanza el mínimo -->
        <execution>
            <id>check</id>
            <goals><goal>check</goal></goals>
            <configuration>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### Ejecución paso a paso

```bash
# 1. Compilar y ejecutar todos los tests generando el reporte
mvn clean verify

# 2. Abrir el reporte HTML generado
# Ruta: target/site/jacoco/index.html
```

### Análisis del reporte generado

#### Resultados globales (Overall Code)

| Métrica | Resultado |
|---|---|
| Cobertura de instrucciones | **81 %** (1.777 de 2.179 cubiertas, 402 no cubiertas) |
| Cobertura de ramas | **77 %** (54 de 70 cubiertas, 16 no cubiertas) |
| Líneas cubiertas | **418 / 495** |
| Métodos cubiertos | **117 / 130** (13 no cubiertos) |
| Clases cubiertas | **29 / 31** |

#### Desglose por paquete

| Paquete | Cob. instrucciones | Cob. ramas | Líneas no cubiertas | Observación |
|---|---|---|---|---|
| `pe.edu.vallegrande.sigei.vgmsusermanagement.infrastructure.config` | 27 % | 100 % | 52 | Baja cobertura de instrucciones en clases de configuración de Spring |
| `pe.edu.vallegrande.sigei.vgmsusermanagement.application.usecases` | 93 % | 75 % | 7 | Buen nivel general; faltan ramas puntuales de lógica de negocio |
| `pe.edu.vallegrande.sigei.vgmsusermanagement.application.mappers` | 88 % | 65 % | 7 | Faltan escenarios de campos opcionales (`null`) |
| `pe.edu.vallegrande.sigei.vgmsusermanagement.infrastructure.adapters.out.messaging` | 87 % | 100 % | 8 | Cobertura alta; quedan pocas líneas sin ejecutar |
| `pe.edu.vallegrande.sigei.vgmsusermanagement` | 0 % | n/a | 3 | Corresponde principalmente a clase de arranque (`Application`) |
| `pe.edu.vallegrande.sigei.vgmsusermanagement.infrastructure.adapters.in.rest` | 100 % | 100 % | 0 | Cobertura total |
| `pe.edu.vallegrande.sigei.vgmsusermanagement.infrastructure.persistence.mappers` | 100 % | n/a | 0 | Cobertura total |
| `pe.edu.vallegrande.sigei.vgmsusermanagement.infrastructure.adapters.out.persistence` | 100 % | n/a | 0 | Cobertura total |
| `pe.edu.vallegrande.sigei.vgmsusermanagement.domain.models.vo` | 100 % | n/a | 0 | Cobertura total |
| `pe.edu.vallegrande.sigei.vgmsusermanagement.application.events` | 100 % | n/a | 0 | Cobertura total |
| `pe.edu.vallegrande.sigei.vgmsusermanagement.application.dto.common` | 100 % | n/a | 0 | Cobertura total |
| `pe.edu.vallegrande.sigei.vgmsusermanagement.domain.exceptions` | 100 % | n/a | 0 | Cobertura total |

#### Métodos/clases no cubiertos identificados

- **`VgMsUserManagementApplication`** — clase main de Spring Boot; se excluye deliberadamente (0 %).
- Ramas `else` de `generateUniqueUserName` cuando el apellido materno es `null` o está en blanco (en `CreateUserUseCaseImpl`).
- Ramas de validación de campos opcionales en `UserMapper`.

#### Propuestas de mejora

1. Agregar test para el flujo `generateUniqueUserName` con apellido materno vacío.
2. Agregar test para el flujo de fallback completo (username base ocupado + inicial materna ocupada).
3. Excluir la clase `Application` en la configuración JaCoCo para no distorsionar el porcentaje.
4. Cubrir ramas de `null` en `UserMapper` con parámetros `null` en las pruebas parametrizadas.

---

## Punto 3 — Análisis de calidad con SonarQube

### ¿Qué es SonarQube?

**SonarQube** es una plataforma de análisis estático de código que inspecciona el código fuente en busca de:

- **Bugs**: errores que pueden causar comportamiento incorrecto en producción.
- **Vulnerabilidades**: problemas de seguridad explotables (OWASP).
- **Code Smells**: código técnicamente correcto pero difícil de mantener.
- **Duplicaciones**: bloques de código repetidos.
- **Cobertura**: integra reportes de JaCoCo para el quality gate.

SonarQube asigna una nota de **A a E** por categoría y define un **Quality Gate** que puede bloquear merges o deploys si no se cumplen umbrales mínimos.

### Configuración del proyecto

#### 1. Propiedades en `pom.xml`

```xml
<properties>
    <sonar.projectKey>AS232S5_PRS1_vg-ms-users-management</sonar.projectKey>
    <sonar.host.url>https://sonarcloud.io</sonar.host.url>
    <sonar.organization>as232s5-prs1</sonar.organization>
    <sonar.coverage.jacoco.xmlReportPaths>
        target/site/jacoco/jacoco.xml
    </sonar.coverage.jacoco.xmlReportPaths>
</properties>
```

#### 2. Ejecución del análisis

```bash
# Con token en variable de entorno
mvn clean verify sonar:sonar -Dsonar.token=$SONAR_TOKEN

# O directamente
mvn sonar:sonar \
  -Dsonar.token=TU_TOKEN \
  -Dsonar.host.url=https://sonarcloud.io
```

### Resultados del análisis (rama `main`)

#### New Code (últimas 2 horas al momento del análisis)

| Indicador | Resultado | Umbral |
|---|---|---|
| Cobertura | **47.37 %** | ≥ 80 % |
| Issues nuevos | 2 | — |
| Duplicaciones | 0.0 % | ≤ 3 % |
| **Quality Gate** | **FAILED** | — |

El quality gate **falló** porque la cobertura del código nuevo no alcanzó el 80 % requerido (solo 15 nuevas líneas a cubrir, 47 % cubiertas).

#### Overall Code

| Categoría | Resultado | Nota |
|---|---|---|
| Security | 0 issues | A |
| Reliability | 0 issues | A |
| Maintainability | 2 issues | A |
| Coverage | 83.5 % (495 líneas) | — |
| Duplications | 0.0 % (1.8k líneas) | — |

### Corrección de un hallazgo

#### Problema detectado: Code Smell — "Reduce inheritance depth"

**Descripción:** SonarQube marcó que la jerarquía de herencia `DomainException → ConflictException → DuplicateDocumentNumberException` superaba la profundidad recomendada.

**Antes (3 niveles):**

```
Exception
  └─ DomainException
       └─ ConflictException
            └─ DuplicateDocumentNumberException
```

**Solución aplicada:** Se mantuvieron `DomainException` y las excepciones concretas, pero se documentó que la jerarquía existe por propósito semántico (el handler global captura por tipo). SonarQube fue configurado para aceptar este patrón mediante la anotación `@SuppressWarnings("java:S110")` en `DomainException`, justificando que la profundidad es intencional para el manejo diferenciado de errores HTTP.

**Beneficio:** 0 code smells de herencia en el reporte actual; la arquitectura de excepciones sigue siendo expresiva.

### Interpretación del Quality Gate

El quality gate **New Code 47 % < 80 %** se explica por:

- Los nuevos archivos de configuración de tests (`R2dbcConfigTest`, `RabbitMQConfigTest`, `WebClientConfigTest`) cubren pocas ramas internas de las clases de config de Spring.
- **Acción correctiva:** agregar los tests de las 8 líneas nuevas no cubiertas para superar el umbral.

---

## Punto 4 — Integración Continua con GitLab CI/CD

### ¿Qué es CI/CD?

**Integración Continua (CI)** es la práctica de ejecutar automáticamente compilación, pruebas y análisis de calidad cada vez que se sube código al repositorio. El objetivo es detectar problemas lo antes posible, antes de que lleguen a producción.

Este proyecto usa **GitLab CI/CD** (no GitHub Actions), cuya configuración reside en el archivo `.gitlab-ci.yml` en la raíz del repositorio. GitLab ejecuta los jobs automáticamente mediante sus runners al detectar eventos en el repositorio.

### Pipeline configurado

Archivo: `.gitlab-ci.yml`

```yaml
stages:
  - test      # ✅ Pruebas unitarias, parametrizadas y cobertura JaCoCo
  - sonar     # 🔍 Análisis de calidad SonarCloud
  - docker    # 🐳 Build & Push imagen Docker
  - deploy    # 🚀 Despliegue en VPS

# ✅ PRUEBAS + COBERTURA JACOCO
unit-tests:
  stage: test
  image: maven:3.9.9-eclipse-temurin-21
  cache:
    key: "${CI_JOB_NAME}"
    paths:
      - .m2/repository
  script:
    # Compila, ejecuta todos los tests (unitarios + parametrizados) y genera reporte JaCoCo
    - mvn clean verify -B
  artifacts:
    when: always
    paths:
      - target/site/jacoco/          # Reporte HTML de cobertura
      - target/surefire-reports/     # Resultados de tests en XML
    reports:
      junit: target/surefire-reports/*.xml  # GitLab muestra los tests en la UI
    expire_in: 7 days
  rules:
    - if: '$CI_PIPELINE_SOURCE == "merge_request_event"'
    - if: '$CI_COMMIT_BRANCH == "main" || $CI_COMMIT_BRANCH == "develop"'

# 🔍 ANÁLISIS DE CÓDIGO
sonarcloud-check:
  stage: sonar
  image: maven:3.9.9-eclipse-temurin-21
  needs:
    - unit-tests
  cache:
    key: "${CI_JOB_NAME}"
    paths:
      - .sonar/cache
      - .m2/repository
  script:
    - mvn sonar:sonar
        -Dsonar.projectKey=as232s5-prs1_vg-ms-users-management
        -Dsonar.organization=as232s5-prs1
        -Dsonar.host.url=https://sonarcloud.io
        -Dsonar.token=$SONAR_TOKEN
  rules:
    - if: '$CI_PIPELINE_SOURCE == "merge_request_event"'
    - if: '$CI_COMMIT_BRANCH == "main" || $CI_COMMIT_BRANCH == "develop"'
```

> **Nota:** El stage `sonar` ahora depende de `unit-tests` (`needs: unit-tests`), por lo que Sonar usa el reporte JaCoCo ya generado en el paso anterior.

### ¿Cuándo se ejecuta el pipeline?

| Evento | Cuándo ocurre |
|---|---|
| `push` a `main` o `develop` | Cada vez que se sube código a esas ramas |
| `merge_request` | Cuando se abre o actualiza un Merge Request en GitLab |
| Tag de Git (`$CI_COMMIT_TAG`) | Al crear un tag se ejecutan los stages docker y deploy |

### ¿Qué validaciones realiza?

1. **Compilación limpia**: `mvn clean verify` compila todo el proyecto desde cero.
2. **Pruebas unitarias**: todas las clases `*Test.java` bajo `src/test/` se ejecutan automáticamente.
3. **Pruebas parametrizadas**: incluidas automáticamente al estar anotadas con `@ParameterizedTest` — JUnit 5 las corre dentro del mismo `mvn verify`.
4. **Generación de cobertura JaCoCo**: produce `target/site/jacoco/` (HTML) y `jacoco.xml` que sube como artefacto del pipeline.
5. **Reporte de tests en GitLab UI**: el campo `reports.junit` permite ver qué tests pasaron/fallaron directamente en la interfaz de GitLab.
6. **Análisis SonarCloud**: el stage `sonar` lee el reporte JaCoCo generado y verifica bugs, code smells, vulnerabilidades y cobertura.

### Beneficios de automatizar estas tareas

| Beneficio | Descripción |
|---|---|
| **Detección temprana** | Los errores se detectan en minutos tras cada push, no al final del sprint |
| **Consistencia** | El entorno del runner es siempre el mismo (imagen `maven:3.9.9-eclipse-temurin-21`), elimina el "en mi máquina funciona" |
| **Historial trazable** | Cada pipeline queda registrado con su resultado de cobertura y calidad en GitLab |
| **Bloqueo de merges defectuosos** | El Merge Request no puede aprobarse si el job `unit-tests` falla |
| **Artefactos descargables** | El reporte HTML de JaCoCo queda disponible 7 días para revisión sin ejecutar nada localmente |
| **Integración con SonarCloud** | El quality gate de Sonar valida automáticamente cada cambio antes de que llegue a `main` |

---

## Punto 5 — Reflexión Final

### ¿Cómo estas prácticas mejoran el proyecto?

| Práctica | Impacto en `vg-ms-user-management` |
|---|---|
| Pruebas unitarias | Verifican que cada caso de uso (crear, actualizar, eliminar, restaurar usuario) funcione de forma aislada antes de integrar |
| Pruebas parametrizadas | Garantizan que la lógica de generación de username resiste distintos caracteres especiales y que el rechazo por DNI duplicado es consistente en cualquier documento |
| Cobertura JaCoCo | Expone ramas sin probar en la lógica de negocio (fallbacks del username) que podrían esconder bugs silenciosos |
| SonarQube | Mantiene la deuda técnica bajo control y asegura que no se introduzcan vulnerabilidades ni código difícil de mantener |
| GitHub Actions CI | Automatiza todo lo anterior: ningún código llega a `main` sin haber pasado por las pruebas y el quality gate |

### ¿Qué valor aportan al enfoque de responsabilidad social del sistema?

El sistema SIGEI gestiona datos sensibles de personas (docentes, apoderados, estudiantes) en instituciones educativas públicas. En este contexto:

- **Confiabilidad**: las pruebas automatizadas reducen la probabilidad de errores que afecten registros reales de usuarios.
- **Transparencia**: los reportes de cobertura y SonarQube son evidencia auditable de que el software fue construido con estándares de calidad.
- **Seguridad**: SonarQube detecta vulnerabilidades (ej. inyección, exposición de datos) antes de que lleguen a producción, protegiendo la privacidad de los usuarios del sistema educativo.
- **Sostenibilidad**: el código bien testeado y libre de deuda técnica puede ser mantenido y evolucionado por cualquier integrante del equipo, lo que garantiza la continuidad del servicio a la comunidad educativa.

---

*Elaborado por el equipo AS232S5 PRS1 — vg-ms-user-management | 2026*
