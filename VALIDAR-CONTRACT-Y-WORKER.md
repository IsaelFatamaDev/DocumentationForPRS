# Validar que `contracts` y `spark-worker` funcionan

Una sola prueba y un solo comando. Si pasa en verde, el contrato coincide con los datos y el pipeline funciona de Bronze a Gold.

## Qué comprueba

| # | Prueba | Qué valida |
|---|---|---|
| 1 | `contractMatchesSampleFiles` | Cada CSV de `data/limpio` y `data/con-errores` tiene **exactamente** las columnas que declara el contract |
| 2 | `cleanDataIsPublished` | Con datos limpios el job se publica, no hay rechazos y se genera Gold |
| 3 | `dataWithErrorsGoesToQuarantine` | Con datos con errores las filas malas van a cuarentena y el job se publica con 5 % de tolerancia |
| 4 | `qualityGateBlocksGold` | Con 1 % de tolerancia la puerta de calidad bloquea y **no** se genera Gold |

## Paso 1 · Revisar el `pom.xml` del spark-worker

Debe tener JUnit y el `argLine` para Spark (el proyecto de referencia ya los tiene):

```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>${junit.version}</version>
    <scope>test</scope>
</dependency>
```

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <argLine>${spark.jvm.options}</argLine>
    </configuration>
</plugin>
```

## Paso 2 · Copiar la prueba

Crear `spark-worker/src/test/java/pe/edu/vallegrande/bigdata/worker/PipelineValidationTests.java`:

```java
package pe.edu.vallegrande.bigdata.worker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pe.edu.vallegrande.bigdata.contracts.AcademicSchema;
import pe.edu.vallegrande.bigdata.contracts.WorkerProtocol;
import pe.edu.vallegrande.bigdata.worker.config.WorkerConfig;

class PipelineValidationTests {
    private static final Path CLEAN = Path.of("..", "data", "limpio");
    private static final Path ERRORS = Path.of("..", "data", "con-errores");

    @TempDir
    Path output;

    @Test
    void contractMatchesSampleFiles() throws Exception {
        for (Path folder : List.of(CLEAN, ERRORS)) {
            for (String table : AcademicSchema.TABLES) {
                Path file = folder.resolve(table + ".csv");
                assertTrue(Files.isRegularFile(file), "Falta " + file);
                assertEquals(AcademicSchema.columns(table), header(file), "Encabezado distinto al contract en " + file);
            }
        }
    }

    @Test
    void cleanDataIsPublished() throws Exception {
        int exit = run(CLEAN, 0.05);

        assertEquals(WorkerProtocol.EXIT_SUCCEEDED, exit);
        assertEquals("0", result("rejected"));
        assertTrue(hasFiles(output.resolve(WorkerProtocol.GOLD)), "No se generó Gold");
    }

    @Test
    void dataWithErrorsGoesToQuarantine() throws Exception {
        int exit = run(ERRORS, 0.05);

        assertEquals(WorkerProtocol.EXIT_SUCCEEDED, exit);
        assertTrue(Long.parseLong(result("rejected")) > 0, "No se rechazó ninguna fila");
        assertTrue(hasFiles(output.resolve(WorkerProtocol.QUARANTINE)), "No se generó la cuarentena");
    }

    @Test
    void qualityGateBlocksGold() throws Exception {
        int exit = run(ERRORS, 0.01);

        assertEquals(WorkerProtocol.EXIT_QUALITY_FAILED, exit);
        assertFalse(hasFiles(output.resolve(WorkerProtocol.GOLD)), "Gold no debía generarse");
    }

    private int run(Path input, double tolerance) throws Exception {
        return WorkerMain.run(input, output, tolerance, 0, WorkerConfig.load().withoutUi());
    }

    private String result(String key) throws Exception {
        String json = Files.readString(output.resolve(WorkerProtocol.RESULT_FILE));
        Matcher matcher = Pattern.compile("\"" + key + "\"\\s*:\\s*\"?([^,\"\\n}]+)").matcher(json);
        assertTrue(matcher.find(), "result.json no tiene " + key);
        return matcher.group(1).trim();
    }

    private static boolean hasFiles(Path folder) throws Exception {
        if (!Files.isDirectory(folder)) {
            return false;
        }
        try (var files = Files.list(folder)) {
            return files.findAny().isPresent();
        }
    }

    private static List<String> header(Path file) throws Exception {
        String first = Files.readAllLines(file, StandardCharsets.UTF_8).get(0).replace("﻿", "");
        return Arrays.stream(first.split(",")).map(String::trim).toList();
    }
}
```

### Qué adaptar en cada proyecto

| En la prueba | Cambiar por |
|---|---|
| `AcademicSchema` | La clase de su contract con las tablas y columnas (por ejemplo `AmankaySchema`, `SusaludSchema`, `IpdSchema`) |
| `data/limpio`, `data/con-errores` | Sus carpetas de datos de ejemplo |
| `0.05` y `0.01` | Tolerancias con las que **sus** datos con errores se publican y se bloquean |

Si su `WorkerMain` todavía no tiene el método `run(...)` que devuelve el código de salida, deben separarlo del `main`, igual que en el proyecto de referencia. Así el pipeline se puede probar sin cerrar la JVM con `System.exit`.

## Paso 3 · Ejecutar

Desde la raíz del proyecto:

```bash
mvn -pl spark-worker -am test -Dtest=PipelineValidationTests -Dsurefire.failIfNoSpecifiedTests=false
```

En IntelliJ: abrir `PipelineValidationTests` y presionar ▶ junto al nombre de la clase.

Resultado esperado (tarda cerca de un minuto porque levanta Spark):

```text
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Si algo falla

| Mensaje | Causa |
|---|---|
| `Encabezado distinto al contract en ...` | Las columnas del CSV no coinciden con el contract: nombre, orden o una columna de más o de menos |
| `Falta ... .csv` | Falta un archivo en `data/` o el nombre no coincide con la tabla del contract |
| `expected: <0> but was: <2>` en `cleanDataIsPublished` | Los datos limpios tienen errores o las reglas rechazan filas válidas: revisar `quarantine/` |
| `No se rechazó ninguna fila` | Los datos con errores no tienen errores reales o la validación no los detecta |
| `expected: <2> but was: <0>` en `qualityGateBlocksGold` | La tolerancia de 1 % es mayor que su porcentaje de rechazo: bajarla o agregar más errores a `con-errores` |
| `InaccessibleObjectException` | Falta el `argLine` del Paso 1 |
