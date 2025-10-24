# 💼 Descripción General de los PRS

## 📑 PROYECTO PRS01: SISTEMA JASS

**Juntas Administradoras de Servicios de Saneamiento**

### 📋 Descripción del Proyecto

El **Sistema JASS Digital** es una plataforma integral diseñada para modernizar y optimizar la gestión de las Juntas Administradoras de Servicios de Saneamiento (JASS) en el Perú. Este sistema permite la administración eficiente de recursos hídricos, facturación, control de calidad del agua, inventarios y infraestructura de saneamiento rural.

## 📑 PROYECTO PRS02: SISTEMA SCHOOL

**Gestión Integral de Instituciones Educativas**

### 📋 Descripción del Proyecto

El Sistema School es una plataforma integral diseñada para digitalizar y optimizar la gestión de instituciones educativas en distintos niveles (inicial, primaria, secundaria y superior). Este sistema permite registrar y administrar múltiples colegios, gestionando sus sedes, aulas y docentes.

### 🎯 Objetivos del Sistema

- **Digitalización**: Transformar los procesos manuales en digitales
- **Transparencia**: Proporcionar información clara y accesible sobre servicios
- **Eficiencia**: Optimizar la gestión de recursos y servicios


### 🌍 Alcance y Cobertura

- **Ámbito**: Nacional (Perú) - Zonas rurales y periurbanas
- **Usuarios**: JASS, administradores locales, usuarios finales
- **Servicios**: Agua potable, saneamiento, control de calidad
- **Gestión**: Multiempresa con contexto organizacional

### 🏗️ Arquitectura de Microservicios Backend

Sistema distribuido basado en microservicios con arquitectura hexagonal, comunicación reactiva y seguridad avanzada.

---

## 📋 Tabla de Contenidos

1. [Resumen Ejecutivo](#-resumen-ejecutivo)
2. [Tecnologías y Frameworks](#-tecnologías-y-frameworks)
3. [Arquitectura del Sistema](#-arquitectura-del-sistema)
4. [Estructura del Proyecto](#-estructura-del-proyecto)
5. [Estándares de Codificacion](#estándares-de-codificación)
6. [Seguridad y Autenticación](#-seguridad-y-autenticación)
7. [Comunicación entre Microservicios](#-comunicación-entre-microservicios)
8. [Gestión de Datos](#-gestión-de-datos)
9. [Infraestructura y Despliegue](#-infraestructura-y-despliegue)
10. [Control de Versionamiento](#-control-de-versionamiento)
11. [Monitoreo y Observabilidad](#-monitoreo-y-observabilidad)
12. [Documentación y APIs](#-documentación-y-apis)
13. [Mejores Prácticas](#-mejores-prácticas)

---

## 🎯 Resumen Ejecutivo

### Arquitectura General Backend

- **Patrón**: Microservicios con API Gateway
- **Comunicación**: HTTP/REST + JWT + JWE para comunicación interna
- **Seguridad**: OAuth2 + JWT + Keycloak
- **Base de Datos**: MongoDB (NoSQL) + PostgreSQL (SQL)
- **Lenguaje**: Java 17 con Spring Boot
- **Infraestructura**: Docker + Docker Compose

---

## ⚙️ Tecnologías y Frameworks

### Backend Technologies Stack

#### **Core Framework**

- **Spring Boot**: `3.4.5` - `3.5.5` (Diferentes versiones por MS)
- **Java**: `17` (LTS) - Estándar en todos los microservicios
- **Maven**: `3.9.6` - Gestor de dependencias y builds

#### **Framework Web**

- **Spring WebFlux**: Framework reactivo para la mayoría de MS
  - Usado en: `vg-ms-users`, `vg-ms-organizations`, `vg-ms-water-quality`, `vg-ms-authentication`, `vg-ms-gateway`
- **Spring Web MVC**: Framework tradicional para algunos MS
  - Usado en: `vg-ms-infrastructure`

#### **Seguridad**

```xml
<!-- OAuth2 Resource Server -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>

<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- Keycloak Admin Client -->
<dependency>
    <groupId>org.keycloak</groupId>
    <artifactId>keycloak-admin-client</artifactId>
    <version>25.0.6</version>
</dependency>

<!-- JWE/JWS Support -->
<dependency>
    <groupId>com.nimbusds</groupId>
    <artifactId>nimbus-jose-jwt</artifactId>
    <version>9.37.3</version>
</dependency>
```

#### **Base de Datos**

```xml
<!-- MongoDB Reactive -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb-reactive</artifactId>
</dependency>

<!-- PostgreSQL + JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
</dependency>
```

#### **Gateway y Cloud**

```xml
<!-- Spring Cloud Gateway -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
<!-- Version: 2023.0.3 -->
```

#### **Documentación API**

```xml
<!-- OpenAPI 3 / Swagger -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webflux-ui</artifactId>
    <version>2.8.8</version>
</dependency>
```

#### **Monitoreo**

```xml
<!-- Actuator -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<!-- Prometheus -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

#### **Herramientas de Desarrollo**

```xml
<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>

<!-- DevTools -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
</dependency>

<!-- Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

---

## 📁 Estructura del Proyecto

## 📂 **Estructura Estándar Unificada de Microservicios para Mongo DB**

```
vg-ms-{service}/
├── 📄 pom.xml                            # Configuración Maven con MongoDB y JWE
├── 📄 Dockerfile                         # Imagen Docker multi-stage optimizada
├── 📄 README.md                          # Documentación específica del microservicio
├── 📄 mvnw, mvnw.cmd                     # Maven Wrapper para builds independientes
├── 📄 docker-compose.yml                 # Orquestación local con MongoDB
├── 📄 {service}-specific-docs.md         # Documentación técnica específica
├── 📄 .gitlab-ci.yml                     # CI/CD específico del servicio
├── 📁 .mvn/wrapper/                      # Configuración Maven Wrapper
└── 📁 src/
    ├── 📁 main/
    │   ├── 📁 java/pe/edu/vallegrande/{package}/
    │   │   ├── 📄 {Service}Application.java           # Clase principal Spring Boot
    │   │   │
    │   │   ├── 📁 application/                        # ⚙️ CAPA DE APLICACIÓN
    │   │   │   ├── 📁 services/                       # Casos de uso y servicios
    │   │   │   │   ├── 📄 {Entity}Service.java        # Interface de servicio (Puerto)
    │   │   │   │   └── 📁 impl/                       # Implementaciones
    │   │   │   │       └── 📄 {Entity}ServiceImpl.java
    │   │   │
    │   │   ├── 📁 domain/                             # 🎯 CAPA DE DOMINIO (Core)
    │   │   │   ├── 📁 models/                         # Entidades de dominio
    │   │   │   │   ├── 📄 {Entity}.java               # Entidades principales (Aggregates)
    │   │   │   │   ├── 📄 {ValueObject}.java          # Value Objects inmutables
    │   │   │   │   └── 📄 {DomainEntity}.java         # Entidades del dominio
    │   │   │   ├── 📁 enums/                          # Enumeraciones del dominio
    │   │   │   │   ├── 📄 {Status}.java               # Estados/Status
    │   │   │   │   ├── 📄 {Type}.java                 # Tipos de dominio
    │   │   │   │   └── 📄 Constants.java              # Constantes del dominio
    │   │   │
    │   │   └── 📁 infrastructure/                     # 🔧 CAPA DE INFRAESTRUCTURA
    │   │       │
    │   │       ├── 📁 document/                       # Documentos MongoDB (Modelos de Persistencia)
    │   │       │   ├── 📄 {Entity}Document.java       # Documento principal
    │   │       │   ├── 📄 {Embedded}Document.java     # Documentos embebidos
    │   │       │   └── 📄 BaseDocument.java           # Documento base con auditoría
    │   │       │
    │   │       ├── 📁 dto/                            # Data Transfer Objects
    │   │       │   ├── 📁 request/                    # DTOs de entrada
    │   │       │   │   ├── 📄 {Entity}Request.java    # Request principal
    │   │       │   │   ├── 📄 Create{Entity}Request.java
    │   │       │   │   ├── 📄 Update{Entity}Request.java
    │   │       │   │   └── 📄 Filter{Entity}Request.java
    │   │       │   ├── 📁 response/                   # DTOs de salida
    │   │       │   │   ├── 📄 {Entity}Response.java   # Response principal
    │   │       │   │   ├── 📄 {Entity}DetailResponse.java
    │   │       │   │   └── 📄 {Entity}SummaryResponse.java
    │   │       │   └── 📁 common/                     # DTOs comunes
    │   │       │       ├── 📄 ResponseDto.java        # Wrapper de respuesta estándar
    │   │       │       ├── 📄 ErrorMessage.java       # Mensajes de error
    │   │       │       └── 📄 ValidationError.java    # Errores de validación
    │   │       │
    │   │       ├── 📁 repository/                     # Repositorios MongoDB
    │   │       │   ├── 📄 {Entity}Repository.java     # Interface MongoRepository
    │   │       │
    │   │       ├── 📁 mapper/                         # Mappers entre capas
    │   │       │   ├── 📄 {Entity}Mapper.java         # Mapper Document <-> Domain
    │   │       │   ├── 📄 {Entity}DtoMapper.java      # Mapper Domain <-> DTO
    │   │       │   └── 📄 BaseMapper.java             # Mapper base con métodos comunes
    │   │       │
    │   │       ├── 📁 rest/                           # Controladores REST (Adaptadores de entrada)
    │   │       │   ├── 📁 admin/                      # Endpoints administrativos
    │   │       │   │   ├── 📄 Admin{Entity}Rest.java
    │   │       │   │   └── 📄 Admin{Management}Rest.java
    │   │       │   └── 📁 client/                     # Endpoints públicos/clientes
    │   │       │       ├── 📄 {Entity}Rest.java
    │   │       │       └── 📄 {Public}Rest.java
    │   │       │
    │   │       ├── 📁 client/                        # 📡 CLIENTES EXTERNOS (Opcional a los microservciios que necesiten consumir)
    │   │       │   │
    │   │       │   ├── 📁 external/                  # Clientes a sistemas externos
    │   │       │   │   ├── 📄 {External}Client.java
    │   │       │   │   │   # - @Component
    │   │       │   │   │   # - implements {External}ClientPort
    │   │       │   │   │   # - RestTemplate o WebClient
    │   │       │   │   │   # - Ejemplo: NotificationClient, PaymentClient
    │   │       │   │   │
    │   │       │   │   └── 📁 dto/                   # DTOs de cliente externo
    │   │       │   │       ├── 📄 {External}Request.java
    │   │       │   │       └── 📄 {External}Response.java
    │   │       │   │
    │   │       │   ├── 📁 internal/                  # Clientes a otros microservicios
    │   │       │   │   ├── 📄 {Service}InternalClient.java
    │   │       │   │   │   # - Comunicación entre microservicios
    │   │       │   │   │   # - Con JWE para datos sensibles
    │   │       │   │   │
    │   │       │   │
    │   │       │   └── 📁 validator/                 # Validadores de clientes
    │   │       │       ├── 📄 ExternalClientValidator.java
    │   │       │       │   # - Valida responses externos
    │   │       │       │   # - Timeout, formato, business rules
    │   │       │       │
    │   │       │       └── 📄 InternalClientValidator.java
    │   │       │           # - Valida comunicación interna
    │   │       │           # - JWE signature, roles, permissions
    │   │       │
    │   │       ├── 📁 security/                       # Seguridad y JWE
    │   │       │   ├── 📄 JweService.java             # Interface JWE
    │   │       │   ├── 📄 InternalJweService.java     # Implementación JWE interna
    │   │       │   ├── 📄 JweEncryptionService.java   # Encriptación JWE
    │   │       │   ├── 📄 JweDecryptionService.java   # Desencriptación JWE
    │   │       │   ├── 📄 SecurityConfig.java         # Configuración Spring Security
    │   │       │   ├── 📄 JwtAuthenticationFilter.java # Filtro JWT/JWE
    │   │       │
    │   │       ├── 📁 validation/                     # Validaciones de infraestructura
    │   │       │   ├── 📄 RequestValidator.java       # Validador de requests
    │   │       │   ├── 📄 JweTokenValidator.java      # Validador de tokens JWE
    │   │       │   ├── 📄 ExternalServiceValidator.java # Validador servicios externos
    │   │       │
    │   │       ├── 📁 exception/                      # Manejo de excepciones
    │   │       │   ├── 📄 GlobalExceptionHandler.java # Handler global
    │   │       │   ├── 📄 RestExceptionHandler.java   # Handler REST específico
    │   │       │   ├── 📄 ValidationExceptionHandler.java # Handler validaciones
    │   │       │   └── 📁 custom/                     # Excepciones personalizadas
    │   │       │       ├── 📄 ResourceNotFoundException.java
    │   │       │       ├── 📄 InvalidTokenException.java
    │   │       │       ├── 📄 ExternalServiceException.java
    │   │       │       └── 📄 {Custom}Exception.java
    │   │       │
    │   │       └── 📁 config/                         # Configuraciones generales
    │   │           ├── 📄 {GENERALES_DE_MS}.java      # Configuración general
    │   │           ├── 📄 WebClientConfig.java        # Configuración general
    │   │
    │   └── 📁 resources/
    │       ├── 📄 application.yml                     # Configuración principal
    │       ├── 📄 application-dev.yml                 # Perfil desarrollo
    │       ├── 📄 application-prod.yml                # Perfil producción
    │       └── 📁 doc/                                # Documentación
    │           ├── 📄 API_DOCUMENTATION.md            # Documentación API
    │           ├── 📄 ARCHITECTURE.md                 # Arquitectura detallada
    │           ├── 📄 JWE_GUIDE.md                    # Guía implementación JWE
    │           └── 📄 {service}-collection.json       # Colección Postman
    │
```
## 📂 **Estructura Estándar Unificada de Microservicios para  PostgreSQL**

```
vg-ms-{service}/
├── 📄 pom.xml                            # Configuración Maven con PostgreSQL y JWE
├── 📄 Dockerfile                         # Imagen Docker multi-stage optimizada
├── 📄 README.md                          # Documentación específica del microservicio
├── 📄 mvnw, mvnw.cmd                     # Maven Wrapper para builds independientes
├── 📄 docker-compose.yml                 # Orquestación local con PostgreSQL
├── 📄 {service}-specific-docs.md         # Documentación técnica específica
├── 📄 .gitlab-ci.yml                     # CI/CD específico del servicio
├── 📁 .mvn/wrapper/                      # Configuración Maven Wrapper
└── 📁 src/
    ├── 📁 main/
    │   ├── 📁 java/pe/edu/vallegrande/{package}/
    │   │   ├── 📄 {Service}Application.java           # Clase principal Spring Boot
    │   │   │
    │   │   ├── 📁 application/                        # ⚙️ CAPA DE APLICACIÓN
    │   │   │   ├── 📁 services/                       # Casos de uso y servicios
    │   │   │   │   ├── 📄 {Entity}Service.java        # Interface de servicio (Puerto)
    │   │   │   │   └── 📁 impl/                       # Implementaciones
    │   │   │   │       └── 📄 {Entity}ServiceImpl.java
    │   │   │
    │   │   ├── 📁 domain/                             # 🎯 CAPA DE DOMINIO (Core)
    │   │   │   ├── 📁 models/                         # Entidades de dominio
    │   │   │   │   ├── 📄 {Entity}.java               # Entidades principales (Aggregates)
    │   │   │   │   ├── 📄 {ValueObject}.java          # Value Objects inmutables
    │   │   │   │   └── 📄 {DomainEntity}.java         # Entidades del dominio
    │   │   │   ├── 📁 enums/                          # Enumeraciones del dominio
    │   │   │   │   ├── 📄 {Status}.java               # Estados/Status
    │   │   │   │   ├── 📄 {Type}.java                 # Tipos de dominio
    │   │   │   │   └── 📄 Constants.java              # Constantes del dominio
    │   │   │
    │   │   └── 📁 infrastructure/                     # 🔧 CAPA DE INFRAESTRUCTURA
    │   │       │
    │   │       ├── 📁 Entity/                       # Entidad PostgreSQL (Modelos de Persistencia)
    │   │       │   ├── 📄 {Entity}Entity.java       # Entidad principal
    │   │       │   ├── 📄 {Embedded}Entity.java     # Entidades embebidas
    │   │       │   └── 📄 BaseEntity.java           # Entidad base con auditoría
    │   │       │
    │   │       ├── 📁 dto/                            # Data Transfer Objects
    │   │       │   ├── 📁 request/                    # DTOs de entrada
    │   │       │   │   ├── 📄 {Entity}Request.java    # Request principal
    │   │       │   │   ├── 📄 Create{Entity}Request.java
    │   │       │   │   ├── 📄 Update{Entity}Request.java
    │   │       │   │   └── 📄 Filter{Entity}Request.java
    │   │       │   ├── 📁 response/                   # DTOs de salida
    │   │       │   │   ├── 📄 {Entity}Response.java   # Response principal
    │   │       │   │   ├── 📄 {Entity}DetailResponse.java
    │   │       │   │   └── 📄 {Entity}SummaryResponse.java
    │   │       │   └── 📁 common/                     # DTOs comunes
    │   │       │       ├── 📄 ResponseDto.java        # Wrapper de respuesta estándar
    │   │       │       ├── 📄 ErrorMessage.java       # Mensajes de error
    │   │       │       └── 📄 ValidationError.java    # Errores de validación
    │   │       │
    │   │       ├── 📁 repository/                     # Repositorios PostgreSQL
    │   │       │   ├── 📄 {Entity}Repository.java     # Interface ReactiveCrudRepository
    │   │       │
    │   │       ├── 📁 mapper/                         # Mappers entre capas
    │   │       │   ├── 📄 {Entity}Mapper.java         # Mapper Document <-> Domain
    │   │       │   ├── 📄 {Entity}DtoMapper.java      # Mapper Domain <-> DTO
    │   │       │   └── 📄 BaseMapper.java             # Mapper base con métodos comunes
    │   │       │
    │   │       ├── 📁 rest/                           # Controladores REST (Adaptadores de entrada)
    │   │       │   ├── 📁 admin/                      # Endpoints administrativos
    │   │       │   │   ├── 📄 Admin{Entity}Rest.java
    │   │       │   │   └── 📄 Admin{Management}Rest.java
    │   │       │   └── 📁 client/                     # Endpoints públicos/clientes
    │   │       │       ├── 📄 {Entity}Rest.java
    │   │       │       └── 📄 {Public}Rest.java
    │   │       │
    │   │       ├── 📁 client/                        # 📡 CLIENTES EXTERNOS (Opcional a los microservciios que necesiten consumir)
    │   │       │   │
    │   │       │   ├── 📁 external/                  # Clientes a sistemas externos
    │   │       │   │   ├── 📄 {External}Client.java
    │   │       │   │   │   # - @Component
    │   │       │   │   │   # - implements {External}ClientPort
    │   │       │   │   │   # - RestTemplate o WebClient
    │   │       │   │   │   # - Ejemplo: NotificationClient, PaymentClient
    │   │       │   │   │
    │   │       │   │   └── 📁 dto/                   # DTOs de cliente externo
    │   │       │   │       ├── 📄 {External}Request.java
    │   │       │   │       └── 📄 {External}Response.java
    │   │       │   │
    │   │       │   ├── 📁 internal/                  # Clientes a otros microservicios
    │   │       │   │   ├── 📄 {Service}InternalClient.java
    │   │       │   │   │   # - Comunicación entre microservicios
    │   │       │   │   │   # - Con JWE para datos sensibles
    │   │       │   │   │
    │   │       │   │
    │   │       │   └── 📁 validator/                 # Validadores de clientes
    │   │       │       ├── 📄 ExternalClientValidator.java
    │   │       │       │   # - Valida responses externos
    │   │       │       │   # - Timeout, formato, business rules
    │   │       │       │
    │   │       │       └── 📄 InternalClientValidator.java
    │   │       │           # - Valida comunicación interna
    │   │       │           # - JWE signature, roles, permissions
    │   │       │
    │   │       ├── 📁 security/                       # Seguridad y JWE
    │   │       │   ├── 📄 JweService.java             # Interface JWE
    │   │       │   ├── 📄 InternalJweService.java     # Implementación JWE interna
    │   │       │   ├── 📄 JweEncryptionService.java   # Encriptación JWE
    │   │       │   ├── 📄 JweDecryptionService.java   # Desencriptación JWE
    │   │       │   ├── 📄 SecurityConfig.java         # Configuración Spring Security
    │   │       │   ├── 📄 JwtAuthenticationFilter.java # Filtro JWT/JWE
    │   │       │
    │   │       ├── 📁 validation/                     # Validaciones de infraestructura
    │   │       │   ├── 📄 RequestValidator.java       # Validador de requests
    │   │       │   ├── 📄 JweTokenValidator.java      # Validador de tokens JWE
    │   │       │   ├── 📄 ExternalServiceValidator.java # Validador servicios externos
    │   │       │
    │   │       ├── 📁 exception/                      # Manejo de excepciones
    │   │       │   ├── 📄 GlobalExceptionHandler.java # Handler global
    │   │       │   ├── 📄 RestExceptionHandler.java   # Handler REST específico
    │   │       │   ├── 📄 ValidationExceptionHandler.java # Handler validaciones
    │   │       │   └── 📁 custom/                     # Excepciones personalizadas
    │   │       │       ├── 📄 ResourceNotFoundException.java
    │   │       │       ├── 📄 InvalidTokenException.java
    │   │       │       ├── 📄 ExternalServiceException.java
    │   │       │       └── 📄 {Custom}Exception.java
    │   │       │
    │   │       └── 📁 config/                         # Configuraciones generales
    │   │           ├── 📄 {GENERALES_DE_MS}.java      # Configuración general
    │   │           ├── 📄 WebClientConfig.java        # Configuración general
    │   │
    │   └── 📁 resources/
    │       ├── 📄 application.yml                     # Configuración principal
    │       ├── 📄 application-dev.yml                 # Perfil desarrollo
    │       ├── 📄 application-prod.yml                # Perfil producción
    │       └── 📁 database/                                # Base de Datos
    │           ├── 📄 schema.sql            # Esquema de base de datos
    │       └── 📁 doc/                                # Documentación
    │           ├── 📄 API_DOCUMENTATION.md            # Documentación API
    │           ├── 📄 ARCHITECTURE.md                 # Arquitectura detallada
    │           ├── 📄 JWE_GUIDE.md                    # Guía implementación JWE
    │           └── 📄 {service}-collection.json       # Colección Postman
    │
```

### Estándares de Codificación

#### **Anotaciones Lombok**

```java
@Data                    // Getter, Setter, ToString, EqualsAndHashCode
@Builder                 // Patrón Builder
@NoArgsConstructor      // Constructor sin argumentos
@AllArgsConstructor     // Constructor con todos los argumentos
@Slf4j                  // Logger
```

#### **Validaciones**

```java
@NotNull
@NotBlank
@NotEmpty
@Size(min = 1, max = 50)
@Email
@Pattern(regexp = "^[A-Z0-9]+$")
```

#### **Estructura de Controller**

```java
@RestController
@RequestMapping("/api/v1/{context}")
@Validated
@Slf4j
@Tag(name = "Context API", description = "Operations for Context management")
public class ContextController {

    @PostMapping("/{action}")
    @PreAuthorize("hasPermission('ACTION_PERMISSION')")
    public Mono<ResponseEntity<ApiResponse<ContextResponse>>> action(
            @Valid @RequestBody ContextRequest request,
            @RequestHeader("X-User-Id") String userId) {
        // Implementation
    }
}
```

#### **Estructura de Service**

```java
@Service
@Slf4j
public class ContextServiceImpl implements ContextService {

    private final ContextRepository repository;

    public ContextServiceImpl(ContextRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Context> create(Context context) {
        return repository.save(context)
            .doOnSuccess(saved -> log.info("Context created: {}", saved.getId()))
            .doOnError(error -> log.error("Error creating context", error));
    }
}
```

### Configuración de Aplicación Estándar

#### **application.yml Base**

```yaml
server:
  port: ${SERVER_PORT:808X}

spring:
  application:
    name: vg-ms-{service}

  # Configuración de base de datos
  data:
    mongodb:
      uri: ${MONGODB_URI:mongodb://localhost:27017/{service}}

  # Configuración de seguridad
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: ${KEYCLOAK_JWK_URI}
          issuer-uri: ${KEYCLOAK_ISSUER_URI}

# Configuración de documentación
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html

# Configuración de monitoreo
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always

# Logging
logging:
  level:
    pe.edu.vallegrande: DEBUG
    org.springframework.security: INFO
```

---

## 🔐 Seguridad y Autenticación

### Modelo de Seguridad Implementado

#### **1. Autenticación Centralizada**

- **Keycloak**: Provider de identidad principal
- **MS-Authentication**: Microservicio de autenticación
- **JWT Tokens**: Tokens de acceso y refresh

#### **2. Autorización Distribuida**

- **Gateway**: Validación inicial de tokens
- **Cada MS**: Validación específica de permisos
- **Headers Contextuales**: X-User-*, información del usuario

#### **3. Comunicación Segura MS-to-MS**

- **JWE (JSON Web Encryption)**: Para endpoints `/internal/*`
- **Service Tokens**: Autenticación entre servicios
- **Filtros de Seguridad**: Validación de servicios internos

### Configuración de Seguridad por Microservicio

#### **Gateway (vg-ms-gateway)**

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: https://lab.vallegrande.edu.pe/{NAME-PRS}/keycloak/realms/sistema-{NAME-PRS}/protocol/openid-connect/certs
          issuer-uri: https://lab.vallegrande.edu.pe/{NAME-PRS}/keycloak/realms/sistema-{NAME-PRS}
```

#### **Microservicio Estándar**

```java
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) {
        return http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/actuator/**").permitAll()
                .pathMatchers("/swagger-ui.html", "/v3/api-docs/**").permitAll()
                .pathMatchers("/internal/**").hasRole("INTERNAL_SERVICE")
                .anyExchange().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt())
            .build();
    }
}
```

### JWE Implementation (MS-to-MS)

#### **Generación de Token JWE**

```java
@Service
@Slf4j
public class InternalJweService {

    public String generateInternalToken(String serviceId, String targetService) {
        JWEHeader header = new JWEHeader.Builder(JWEAlgorithm.A256GCM, EncryptionMethod.A256GCM)
                .build();

        JWEObject jweObject = new JWEObject(header, new Payload(claims.toJSONString()));
        jweObject.encrypt(new AESEncrypter(derivedKey));

        return jweObject.serialize();
    }
}
```

---

## 🔄 Comunicación entre Microservicios

### Patrones de Comunicación

#### **1. Synchronous Communication**

- **HTTP/REST**: Comunicación principal
- **WebClient**: Cliente HTTP reactivo
- **JSON**: Formato de intercambio de datos

#### **2. Gateway Routing**

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: ms-users
          uri: ${USERS_SERVICE_URL:http://localhost:8085}
          predicates:
            - Path=/api/v1/users/**,/api/v1/admin/users/**
          filters:
            - JwtAuthenticationFilter
```

#### **3. Service Discovery**

- **Configuration-based**: URLs configurables por ambiente
- **Load Balancing**: Spring Cloud Gateway

## 📊 **Patrones de Respuesta HTTP**

### **ResponseDto Estándar**

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDto<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public static <T> ResponseDto<T> success(T data) {
        return ResponseDto.<T>builder()
            .success(true)
            .message("Operación exitosa")
            .data(data)
            .timestamp(LocalDateTime.now())
            .build();
    }

    public static <T> ResponseDto<T> error(String message) {
        return ResponseDto.<T>builder()
            .success(false)
            .message(message)
            .data(null)
            .timestamp(LocalDateTime.now())
            .build();
    }
}
```

### **Códigos de Estado HTTP**

- **200 OK**: Operación exitosa
- **201 Created**: Recurso creado exitosamente
- **204 No Content**: Operación exitosa sin contenido
- **400 Bad Request**: Error en la solicitud del cliente
- **401 Unauthorized**: No autenticado
- **403 Forbidden**: No autorizado
- **404 Not Found**: Recurso no encontrado
- **409 Conflict**: Conflicto de recursos
- **500 Internal Server Error**: Error interno del servidor

---

### Endpoints Internos

#### **Patrón para Comunicación MS-to-MS**

```java
@RestController
@RequestMapping("/internal")
@Validated
public class InternalController {

    @GetMapping("/users/organization/{orgId}")
    @PreAuthorize("hasRole('INTERNAL_SERVICE')")
    public Mono<ResponseEntity<List<UserResponse>>> getUsersByOrganization(
            @PathVariable String orgId,
            @RequestHeader("X-Internal-Service") String sourceService) {
        // Implementation
    }
}
```

### Configuración de WebClient

```java
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader("X-Internal-Service", "ms-users")
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
            .build();
    }
}
```

---

## 🚀 Infraestructura y Despliegue

### Docker Configuration

#### **Dockerfile Estándar (Multi-stage)**

```dockerfile
# Build stage
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /build
COPY pom.xml ./
COPY mvnw ./
COPY .mvn ./.mvn
RUN ./mvnw dependency:go-offline -B -q
COPY src ./src
RUN ./mvnw clean package -DskipTests -B -q

# Runtime stage
FROM eclipse-temurin:17-jre-alpine AS runtime
RUN apk add --no-cache dumb-init curl
RUN addgroup -S jassgroup && adduser -S -D -H -s /sbin/nologin -G jassgroup jassuser

WORKDIR /app
COPY --from=builder --chown=jassuser:jassgroup /build/target/*.jar app.jar

USER jassuser

ENV JAVA_OPTS="-server \
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=70.0 \
    -XX:+UseG1GC \
    -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["dumb-init", "--"]
CMD ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
```

### Scripts de Despliegue

#### **PowerShell Script (start-microservices.ps1)**

```powershell
# Inicio secuencial de microservicios
$services = @(
    @{ Name = "Authentication"; Directory = "vg-ms-authentication"; Port = 8081 },
    @{ Name = "Users"; Directory = "vg-ms-users"; Port = 8085 },
    @{ Name = "Gateway"; Directory = "vg-ms-gateway"; Port = 9090 }
)

foreach ($service in $services) {
    Write-Host "🚀 Iniciando $($service.Name)..." -ForegroundColor Green
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$($service.Directory)'; mvn spring-boot:run"
    Start-Sleep -Seconds 15
}
```

### Configuración de Profiles

#### **Development Profile**

```yaml
# application-dev.yml
server:
  port: 8085

logging:
  level:
    pe.edu.vallegrande: DEBUG
    org.springframework.web: DEBUG

spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/ms_users_dev
```

#### **Production Profile**

```yaml
# application-prod.yml
server:
  port: ${SERVER_PORT}

logging:
  level:
    pe.edu.vallegrande: INFO

spring:
  data:
    mongodb:
      uri: ${MONGODB_URI}

management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
```

### Variables de Entorno

```bash
# Database
MONGODB_URI=mongodb://mongo:27017/ms_users
POSTGRES_URL=jdbc:postgresql://postgres:5432/ms_infrastructure

# Security
JWT_SECRET=your-256-bit-secret-key
KEYCLOAK_URL=https://keycloak.domain.com
INTERNAL_JWE_SECRET=your-jwe-secret-key

# Services URLs
GATEWAY_URL=https://api.domain.com
USERS_SERVICE_URL=http://ms-users:8085
AUTH_SERVICE_URL=http://ms-auth:8081

# Monitoring
SPRING_PROFILES_ACTIVE=prod
JAVA_OPTS=-Xmx512m -Xms256m
```

---

## 📝 Control de Versionamiento

### Git Workflow (GitLab)

#### **Branch Strategy**

```
main/
├── develop/              # Desarrollo activo
├── feature/              # Nuevas funcionalidades
│   ├── feature/user-management
│   ├── feature/jwe-implementation
│   └── feature/payment-system
├── release/              # Preparación de releases
│   └── release/v1.2.0
├── hotfix/              # Correcciones urgentes
│   └── hotfix/security-patch
└── docs/                # Documentación
```

#### **Commit Message Convention**

```
<type>(<scope>): <description>

Types:
- feat:     Nueva funcionalidad
- fix:      Corrección de bug
- docs:     Documentación
- style:    Formateo (no cambios de código)
- refactor: Refactoring
- test:     Tests
- chore:    Tareas de mantenimiento

Examples:
feat(users): implement JWE encryption for internal communication
fix(gateway): resolve JWT validation issue
docs(security): update authentication documentation
```

#### **GitLab CI/CD Pipeline**

```yaml
# .gitlab-ci.yml
stages:
  - test
  - build
  - deploy

variables:
  MAVEN_OPTS: "-Dmaven.repo.local=$CI_PROJECT_DIR/.m2/repository"

cache:
  paths:
    - .m2/repository/

test:
  stage: test
  script:
    - mvn clean test
  artifacts:
    reports:
      junit:
        - target/surefire-reports/TEST-*.xml
    paths:
      - target/

build:
  stage: build
  script:
    - mvn clean package -DskipTests
    - docker build -t $CI_REGISTRY_IMAGE:$CI_COMMIT_SHA .
    - docker push $CI_REGISTRY_IMAGE:$CI_COMMIT_SHA

deploy:
  stage: deploy
  script:
    - kubectl apply -f k8s/
    - kubectl set image deployment/app app=$CI_REGISTRY_IMAGE:$CI_COMMIT_SHA
  only:
    - main
```

### Versionado de APIs

#### **Semantic Versioning**

```
v{MAJOR}.{MINOR}.{PATCH}

MAJOR: Cambios incompatibles
MINOR: Funcionalidades compatibles hacia atrás
PATCH: Bug fixes compatibles

Examples:
v1.0.0 - Release inicial
v1.1.0 - Nueva funcionalidad (JWE)
v1.1.1 - Bug fix en autenticación
v2.0.0 - Cambio mayor en API
```

#### **API Versioning Strategy**

```java
// URL Versioning
@RequestMapping("/api/v1/users")
@RequestMapping("/api/v2/users")

// Header Versioning
@RequestMapping(value = "/api/users", headers = "API-Version=1")
@RequestMapping(value = "/api/users", headers = "API-Version=2")
```

---

## 📊 Monitoreo y Observabilidad

### Health Checks y Actuator

#### **Configuración de Actuator**

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,loggers
      base-path: /actuator
  endpoint:
    health:
      show-details: always
      show-components: always
  health:
    mongo:
      enabled: true
    redis:
      enabled: true
```

#### **Custom Health Indicators**

```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private final UserRepository userRepository;

    @Override
    public Health health() {
        try {
            userRepository.count().block(Duration.ofSeconds(3));
            return Health.up()
                .withDetail("database", "MongoDB is accessible")
                .build();
        } catch (Exception e) {
            return Health.down(e)
                .withDetail("database", "MongoDB is not accessible")
                .build();
        }
    }
}
```

### Logging Estándar

#### **Configuración de Logs**

```yaml
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId},%X{spanId}] %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId},%X{spanId}] %logger{36} - %msg%n"
  level:
    pe.edu.vallegrande: INFO
    org.springframework.security: WARN
  file:
    path: ./logs
    name: ${spring.application.name}.log
```

#### **Structured Logging**

```java
@Slf4j
@Service
public class UserService {

    public Mono<User> createUser(CreateUserRequest request) {
        return userRepository.save(user)
            .doOnSuccess(savedUser ->
                log.info("User created successfully: userId={}, email={}, organizationId={}",
                    savedUser.getId(), savedUser.getEmail(), savedUser.getOrganizationId()))
            .doOnError(error ->
                log.error("Failed to create user: email={}, error={}",
                    request.getEmail(), error.getMessage(), error));
    }
}
```

### Métricas con Prometheus

#### **Custom Metrics**

```java
@Component
public class UserMetrics {

    private final Counter userCreationCounter;
    private final Timer userCreationTimer;

    public UserMetrics(MeterRegistry meterRegistry) {
        this.userCreationCounter = Counter.builder("users.created.total")
            .description("Total number of users created")
            .tag("service", "ms-users")
            .register(meterRegistry);

        this.userCreationTimer = Timer.builder("users.creation.duration")
            .description("Time taken to create a user")
            .register(meterRegistry);
    }

    public void recordUserCreation(String organizationId) {
        userCreationCounter.increment(Tags.of("organization", organizationId));
    }
}
```

---

## 📚 Documentación y APIs

### OpenAPI/Swagger Configuration

#### **Swagger Configuration**

```java
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("MS-Users API")
                .version("v1.0")
                .description("Microservicio de gestión de usuarios para Sistema PRS")
                .contact(new Contact()
                    .name("Valle Grande - Sistema PRS")
                    .email("soporte@vallegrande.edu.pe")))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .components(new Components()
                .addSecuritySchemes("bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
```

#### **API Documentation Tags**

```java
@RestController
@RequestMapping("/api/v1/admin/users")
@Tag(name = "Admin User Management", description = "Operations for user management by administrators")
@Validated
public class AdminUserController {

    @Operation(
        summary = "Create a new client user",
        description = "Creates a new client user in the system. Requires ADMIN role.",
        responses = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
        }
    )
    @PostMapping("/clients")
    public Mono<ResponseEntity<ApiResponse<UserResponse>>> createClient(
            @Valid @RequestBody CreateUserRequest request) {
        // Implementation
    }
}
```

### URLs de Documentación

```yaml
# URLs de Swagger por servicio (a través del Gateway)
Gateway Swagger UI:     http://localhost:9090/swagger-ui.html
MS-Users Swagger:       http://localhost:9090/docs/users/swagger-ui.html
MS-Auth Swagger:        http://localhost:9090/docs/auth/swagger-ui.html
MS-Organizations:       http://localhost:9090/docs/organizations/swagger-ui.html
```

---

## ✅ Mejores Prácticas

### Código Limpio

#### **1. Principios SOLID**

```java
// Single Responsibility
@Service
public class UserRegistrationService {
    public Mono<User> registerUser(RegisterUserRequest request) { ... }
}

@Service
public class UserValidationService {
    public Mono<Boolean> validateUser(User user) { ... }
}

// Dependency Injection
@Service
public class UserService {
    private final UserRepository repository;
    private final UserValidationService validationService;

    public UserService(UserRepository repository, UserValidationService validationService) {
        this.repository = repository;
        this.validationService = validationService;
    }
}
```

#### **2. Error Handling**

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error("User not found", ex.getMessage()));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(ValidationException ex) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("Validation failed", ex.getErrors()));
    }
}
```

#### **3. Reactive Programming Best Practices**

```java
@Service
public class UserService {

    public Mono<User> createUser(CreateUserRequest request) {
        return validateRequest(request)
            .flatMap(this::convertToEntity)
            .flatMap(userRepository::save)
            .flatMap(this::sendNotification)
            .doOnSuccess(user -> log.info("User created: {}", user.getId()))
            .doOnError(error -> log.error("Error creating user", error))
            .onErrorMap(DataAccessException.class,
                ex -> new UserCreationException("Failed to create user", ex));
    }
}
```

### Testing Standards

#### **Unit Tests**

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldCreateUser_WhenValidRequest() {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
            .email("test@example.com")
            .build();

        User expectedUser = User.builder().id("123").build();
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(expectedUser));

        // When
        StepVerifier.create(userService.createUser(request))
            // Then
            .expectNext(expectedUser)
            .verifyComplete();

        verify(userRepository).save(any(User.class));
    }
}
```

#### **Integration Tests**

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.data.mongodb.uri=mongodb://localhost:27017/test_users"
})
class UserControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void shouldCreateUser_WhenValidRequest() {
        CreateUserRequest request = CreateUserRequest.builder()
            .email("test@example.com")
            .firstName("John")
            .lastName("Doe")
            .build();

        webTestClient.post()
            .uri("/api/v1/admin/users/clients")
            .header("Authorization", "Bearer " + validJwtToken)
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated()
            .expectBody()
            .jsonPath("$.success").isEqualTo(true)
            .jsonPath("$.data.email").isEqualTo("test@example.com");
    }
}
```

### Performance Optimization

#### **1. Database Optimization**

```java
// MongoDB Indexes
@Document(collection = "users")
@CompoundIndex(def = "{'organizationId': 1, 'email': 1}")
public class User {
    @Indexed(unique = true)
    private String email;

    @Indexed
    private String organizationId;
}

// Reactive Queries
public Flux<User> findUsersByOrganization(String organizationId) {
    return userRepository.findByOrganizationId(organizationId)
        .take(100)  // Limit results
        .timeout(Duration.ofSeconds(5));  // Timeout
}
```

#### **2. Caching Strategy**

```java
@Service
@Slf4j
public class UserService {

    @Cacheable(value = "users", key = "#id")
    public Mono<User> findById(String id) {
        return userRepository.findById(id);
    }

    @CacheEvict(value = "users", key = "#user.id")
    public Mono<User> updateUser(User user) {
        return userRepository.save(user);
    }
}
```

### Security Best Practices

#### **1. Input Validation**

```java
@PostMapping("/users")
public Mono<ResponseEntity<ApiResponse<UserResponse>>> createUser(
        @Valid @RequestBody CreateUserRequest request,
        @Pattern(regexp = "^[A-Z0-9]+$") @RequestHeader("X-User-Organization") String orgId) {

    return userService.createUser(request, orgId)
        .map(user -> ResponseEntity.ok(ApiResponse.success(user)));
}
```

#### **2. Secure Headers**

```java
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) {
        return http
            .headers(headers -> headers
                .frameOptions().deny()
                .contentTypeOptions().and()
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)
                    .includeSubdomains(true)))
            .build();
    }
}
```

---

## 🎯 Conclusiones y Próximos Pasos

### Estado Actual del Sistema

✅ **Implementado:**

- Arquitectura de microservicios completa
- Autenticación JWT + JWE para comunicación interna
- API Gateway con enrutamiento inteligente
- Base de datos distribuida (MongoDB + PostgreSQL)
- Documentación OpenAPI/Swagger completa
- Containerización con Docker
- Scripts de despliegue automatizados

### Características Destacadas

🔒 **Seguridad Avanzada:**

- Autenticación con Keycloak
- JWE para comunicación segura entre microservicios
- Autorización granular por roles y contexto organizacional

⚡ **Performance:**

- Programación reactiva con Spring WebFlux
- Base de datos optimizada por dominio
- Caching y optimizaciones

🏗️ **Arquitectura Sólida:**

- Principios SOLID y Clean Architecture
- Separación clara de responsabilidades
- Patrones probados (Gateway, Database per Service)

### Próximos Pasos Recomendados

#### **Corto Plazo (1-2 meses)**

1. **Monitoreo Avanzado:**
   - Implementar distributed tracing (Zipkin/Jaeger)
   - Dashboards de Grafana + Prometheus
   - Alertas automáticas

2. **Testing:**
   - Cobertura de tests > 80%
   - Tests de integración automatizados
   - Performance testing

#### **Mediano Plazo (3-6 meses)**

1. **CI/CD Completo:**
   - Pipeline automatizado GitLab CI
   - Despliegue automático a staging/producción
   - Blue-green deployments

2. **Observabilidad:**
   - Logging centralizado (ELK Stack)
   - Métricas de negocio
   - Health checks avanzados

#### **Largo Plazo (6-12 meses)**

1. **Event-Driven Architecture:**
   - Implementar messaging con RabbitMQ/Kafka
   - Event sourcing para auditoría
   - CQRS para optimización de consultas

2. **Escalabilidad:**
   - Kubernetes para orquestación
   - Auto-scaling horizontal
   - Service mesh (Istio)

---
