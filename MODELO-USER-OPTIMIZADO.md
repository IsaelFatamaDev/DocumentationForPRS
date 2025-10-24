# 🎯 MODELO DE DATOS OPTIMIZADO PARA KEYCLOAK + MULTI-TENANT

> **User + AuthCredential según DDD y arquitectura con Keycloak**

---

## 📋 RESUMEN DE CAMBIOS

### ❌ **LO QUE QUITAMOS:**

De **AuthCredential**:

- ❌ `passwordHash` → Keycloak gestiona contraseñas
- ❌ `failedLoginAttempts` → Keycloak gestiona bloqueos
- ❌ `lockedUntil` → Keycloak gestiona bloqueos
- ❌ `passwordChangedAt` → Keycloak gestiona esto
- ❌ `mustChangePassword` → Keycloak gestiona esto
- ❌ Métodos de dominio para gestión de contraseñas

De **User**:

- ❌ `userCode` → No es necesario si tenemos `id` y `keycloakUserId`
- ❌ `deletedAt` / `deletedBy` → Soft delete es anti-patrón en microservicios
- ❌ `createdBy` / `updatedBy` → Puede ir en un `AuditMetadata` si es necesario

### ✅ **LO QUE AGREGAMOS:**

A **User**:

- ✅ `keycloakUserId` (sub del JWT) → Vínculo con Keycloak
- ✅ `organizationId` → Multi-tenant (CRÍTICO)
- ✅ Value Objects limpios (`PersonalInfo`, `Contact`, `AddressUsers`)

---

## 🏗️ OPCIÓN RECOMENDADA: **FUSIONAR EN USER**

### ¿Por qué fusionar User + AuthCredential?

**Según DDD:**

- ✅ **User** es el **Aggregate Root** (raíz del agregado)
- ✅ **AuthCredential** no tiene razón de negocio para existir por separado
- ✅ `keycloakUserId` es solo un **atributo** de User
- ✅ Evita duplicación de datos (`roles` está en ambos)
- ✅ Simplifica queries (no necesitas JOIN)

**Resultado:**

```java
User {
    id: "68c0a4ab07fa2d47448b530a"           ← MongoDB ID
    keycloakUserId: "ab97f6ed-66e3-4484-..."  ← Keycloak sub
    username: "javier.fatama@jass.gob.pe"
    organizationId: "6896b2ecf3e398570ffd99d3"
    personalInfo: {...}
    contact: {...}
    address: {...}
    roles: [ADMIN]
    status: ACTIVE
}
```

---

## 📦 MODELO OPTIMIZADO (OPCIÓN RECOMENDADA)

### **User.java** (Entidad única)

```java
package pe.edu.vallegrande.vgmsusers.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import pe.edu.vallegrande.vgmsusers.domain.enums.RolesUsers;
import pe.edu.vallegrande.vgmsusers.domain.enums.UserStatus;
import pe.edu.vallegrande.vgmsusers.domain.model.vo.AddressUsers;
import pe.edu.vallegrande.vgmsusers.domain.model.vo.Contact;
import pe.edu.vallegrande.vgmsusers.domain.model.vo.PersonalInfo;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Agregado raíz: User
 * Representa un usuario del sistema JASS Digital
 *
 * Bounded Context: Identity Management
 * Aggregate Root: User
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "users")
@CompoundIndex(name = "org_username_idx", def = "{'organizationId': 1, 'username': 1}", unique = true)
@CompoundIndex(name = "org_status_idx", def = "{'organizationId': 1, 'status': 1}")
public class User {

    // ==================== IDENTIFICADORES ====================

    @Id
    private String id;  // MongoDB ID

    @Indexed(unique = true)
    private String keycloakUserId;  // Keycloak sub (ab97f6ed-66e3-4484-...)

    @Indexed
    private String username;  // javier.fatama@jass.gob.pe

    // ==================== MULTI-TENANT ====================

    /**
     * ID de la organización (JASS) a la que pertenece el usuario
     * CRÍTICO para Row-Level Security
     */
    @Indexed
    private String organizationId;  // 6896b2ecf3e398570ffd99d3

    // ==================== VALUE OBJECTS ====================

    private PersonalInfo personalInfo;  // Información personal (nombre, documento)
    private Contact contact;            // Contacto (email, teléfono)
    private AddressUsers address;       // Dirección

    // ==================== ROLES Y ESTADO ====================

    private Set<RolesUsers> roles;      // [ADMIN, CLIENT, OPERATOR, SUPER_ADMIN]
    private UserStatus status;          // ACTIVE, INACTIVE, SUSPENDED, PENDING

    // ==================== FECHAS ====================

    private LocalDateTime registrationDate;  // Fecha de registro
    private LocalDateTime lastLogin;         // Último login

    // ==================== AUDITORÍA ====================

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ==================== MÉTODOS DE DOMINIO ====================

    /**
     * Verifica si el usuario está activo
     */
    public boolean isActive() {
        return UserStatus.ACTIVE.equals(this.status);
    }

    /**
     * Verifica si el usuario puede hacer login
     */
    public boolean canLogin() {
        return UserStatus.ACTIVE.equals(this.status) ||
               UserStatus.PENDING.equals(this.status);
    }

    /**
     * Verifica si el usuario tiene un rol específico
     */
    public boolean hasRole(RolesUsers role) {
        return this.roles != null && this.roles.contains(role);
    }

    /**
     * Verifica si el usuario tiene alguno de los roles especificados
     */
    public boolean hasAnyRole(RolesUsers... roles) {
        if (this.roles == null || this.roles.isEmpty()) {
            return false;
        }
        for (RolesUsers role : roles) {
            if (this.roles.contains(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica si el usuario es administrador
     */
    public boolean isAdmin() {
        return hasAnyRole(RolesUsers.ADMIN, RolesUsers.SUPER_ADMIN);
    }

    /**
     * Verifica si el usuario es super administrador
     */
    public boolean isSuperAdmin() {
        return hasRole(RolesUsers.SUPER_ADMIN);
    }

    /**
     * Verifica si el usuario es cliente
     */
    public boolean isClient() {
        return hasRole(RolesUsers.CLIENT);
    }

    /**
     * Verifica si el usuario es operario
     */
    public boolean isOperator() {
        return hasRole(RolesUsers.OPERATOR);
    }

    /**
     * Verifica si el usuario pertenece a la organización especificada
     * CRÍTICO para validación de multi-tenant
     */
    public boolean belongsToOrganization(String organizationId) {
        return this.organizationId != null &&
               this.organizationId.equals(organizationId);
    }

    /**
     * Registra un login exitoso
     */
    public void recordLogin() {
        this.lastLogin = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Actualiza el estado del usuario
     */
    public void updateStatus(UserStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Agrega un rol al usuario
     */
    public void addRole(RolesUsers role) {
        if (this.roles == null) {
            this.roles = Set.of(role);
        } else {
            this.roles.add(role);
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Remueve un rol del usuario
     */
    public void removeRole(RolesUsers role) {
        if (this.roles != null) {
            this.roles.remove(role);
            this.updatedAt = LocalDateTime.now();
        }
    }
}
```

---

## 📦 VALUE OBJECTS

### **PersonalInfo.java**

```java
package pe.edu.vallegrande.vgmsusers.domain.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.vgmsusers.domain.enums.DocumentType;

/**
 * Value Object: Información Personal
 * Inmutable, sin identidad propia
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonalInfo {

    private DocumentType documentType;  // DNI, CE, PASSPORT
    private String documentNumber;      // 12345678
    private String firstName;           // Javier
    private String lastName;            // Fatama

    /**
     * Nombre completo calculado
     */
    public String getFullName() {
        return (firstName != null ? firstName : "") + " " +
               (lastName != null ? lastName : "");
    }

    /**
     * Valida si la información personal es completa
     */
    public boolean isComplete() {
        return documentType != null &&
               documentNumber != null && !documentNumber.isEmpty() &&
               firstName != null && !firstName.isEmpty() &&
               lastName != null && !lastName.isEmpty();
    }
}
```

### **Contact.java**

```java
package pe.edu.vallegrande.vgmsusers.domain.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Value Object: Contacto
 * Puede ser email, teléfono, etc.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contact {

    private String email;       // jfatama@gmail.com
    private String phone;       // +51 987654321
    private String phoneAlt;    // Teléfono alternativo

    /**
     * Verifica si tiene email válido
     */
    public boolean hasEmail() {
        return email != null && email.contains("@");
    }

    /**
     * Verifica si tiene teléfono válido
     */
    public boolean hasPhone() {
        return phone != null && !phone.isEmpty();
    }
}
```

### **AddressUsers.java**

```java
package pe.edu.vallegrande.vgmsusers.domain.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Value Object: Dirección
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressUsers {

    private String streetAddress;   // Jr. Los Olivos 123
    private String streetId;        // ID de la calle (si aplica)
    private String zoneId;          // ID de la zona (si aplica)
    private String district;        // Distrito
    private String province;        // Provincia
    private String department;      // Departamento
    private String postalCode;      // Código postal

    /**
     * Dirección completa formateada
     */
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();

        if (streetAddress != null) sb.append(streetAddress);
        if (district != null) sb.append(", ").append(district);
        if (province != null) sb.append(", ").append(province);
        if (department != null) sb.append(", ").append(department);

        return sb.toString();
    }
}
```

---

## 🗄️ REPOSITORIO

### **UserRepository.java**

```java
package pe.edu.vallegrande.vgmsusers.infrastructure.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.vgmsusers.domain.model.User;
import pe.edu.vallegrande.vgmsusers.domain.enums.UserStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveMongoRepository<User, String> {

    /**
     * Busca usuario por Keycloak User ID (sub)
     * Usado en login para obtener userInfo
     */
    Mono<User> findByKeycloakUserId(String keycloakUserId);

    /**
     * Busca usuario por username
     */
    Mono<User> findByUsername(String username);

    /**
     * Busca usuarios por organización (multi-tenant)
     */
    Flux<User> findByOrganizationId(String organizationId);

    /**
     * Busca usuarios activos por organización
     */
    Flux<User> findByOrganizationIdAndStatus(String organizationId, UserStatus status);

    /**
     * Busca usuario por username y organización
     * Garantiza unicidad dentro de cada organización
     */
    Mono<User> findByOrganizationIdAndUsername(String organizationId, String username);

    /**
     * Verifica si existe un usuario con ese username en esa organización
     */
    Mono<Boolean> existsByOrganizationIdAndUsername(String organizationId, String username);

    /**
     * Verifica si existe un usuario con ese Keycloak ID
     */
    Mono<Boolean> existsByKeycloakUserId(String keycloakUserId);
}
```

---

## 🎯 ESCENARIOS DE USO

### 1️⃣ **Login** (vg-ms-authentication llama a vg-ms-users)

```java
// vg-ms-authentication
public Mono<AuthResponse> login(LoginRequest request) {
    // 1. Autenticar en Keycloak
    return keycloakPort.authenticate(request.getUsername(), request.getPassword())
        .flatMap(authToken -> {
            // 2. Extraer sub del JWT
            String keycloakUserId = extractSub(authToken.getAccessToken());

            // 3. Obtener userInfo de vg-ms-users
            return usersClient.getUserByKeycloakId(keycloakUserId)
                .doOnNext(user -> user.recordLogin())  // Registrar login
                .flatMap(user -> userRepository.save(user)  // Actualizar lastLogin
                    .map(savedUser -> {
                        authToken.setUserInfo(UserInfoDto.builder()
                            .userId(savedUser.getId())
                            .keycloakUserId(savedUser.getKeycloakUserId())
                            .username(savedUser.getUsername())
                            .organizationId(savedUser.getOrganizationId())
                            .roles(savedUser.getRoles())
                            .personalInfo(savedUser.getPersonalInfo())
                            .contact(savedUser.getContact())
                            .lastLogin(savedUser.getLastLogin())
                            .build());

                        return authToken;
                    })
                );
        });
}
```

### 2️⃣ **Registro de Usuario** (vg-ms-authentication orquesta)

```java
// vg-ms-authentication
public Mono<User> registerUser(RegisterRequest request) {
    return Mono.defer(() -> {
        // 1. Crear usuario en Keycloak
        return keycloakPort.createUser(request)
            .flatMap(keycloakUserId -> {
                // 2. Crear usuario en MongoDB
                User user = User.builder()
                    .keycloakUserId(keycloakUserId)
                    .username(request.getUsername())
                    .organizationId(request.getOrganizationId())
                    .personalInfo(PersonalInfo.builder()
                        .firstName(request.getFirstName())
                        .lastName(request.getLastName())
                        .documentType(request.getDocumentType())
                        .documentNumber(request.getDocumentNumber())
                        .build())
                    .contact(Contact.builder()
                        .email(request.getEmail())
                        .phone(request.getPhone())
                        .build())
                    .roles(Set.of(RolesUsers.CLIENT))
                    .status(UserStatus.ACTIVE)
                    .registrationDate(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .build();

                return usersClient.createUser(user);
            })
            .onErrorResume(error -> {
                // Compensación: eliminar de Keycloak si falla MongoDB
                log.error("Error creando usuario en MongoDB, eliminando de Keycloak");
                return keycloakPort.deleteUser(request.getUsername())
                    .then(Mono.error(error));
            });
    });
}
```

### 3️⃣ **Buscar Usuario por Keycloak ID** (endpoint interno)

```java
// vg-ms-users
@GetMapping("/internal/users/by-keycloak-id/{keycloakUserId}")
public Mono<ResponseEntity<UserInfoDto>> getUserByKeycloakId(
    @PathVariable String keycloakUserId
) {
    return userRepository.findByKeycloakUserId(keycloakUserId)
        .map(user -> ResponseEntity.ok(UserInfoDto.from(user)))
        .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
}
```

### 4️⃣ **Listar Usuarios de una Organización** (multi-tenant)

```java
// vg-ms-users
@GetMapping("/api/users")
public Mono<ResponseEntity<List<UserDto>>> getUsers(
    @RequestHeader("X-Organization-Id") String organizationId,
    @RequestHeader("X-User-Roles") String roles
) {
    // Validar que el usuario tiene permiso
    if (!roles.contains("ADMIN") && !roles.contains("SUPER_ADMIN")) {
        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }

    // Listar solo usuarios de su organización (Row-Level Security)
    return userRepository.findByOrganizationIdAndStatus(
            organizationId,
            UserStatus.ACTIVE
        )
        .map(UserDto::from)
        .collectList()
        .map(ResponseEntity::ok);
}
```

---

## 📊 ÍNDICES DE MONGODB

```javascript
// Crear índices en MongoDB
db.users.createIndex({ "keycloakUserId": 1 }, { unique: true });
db.users.createIndex({ "username": 1 });
db.users.createIndex({ "organizationId": 1 });
db.users.createIndex({ "organizationId": 1, "username": 1 }, { unique: true });
db.users.createIndex({ "organizationId": 1, "status": 1 });
db.users.createIndex({ "organizationId": 1, "roles": 1 });
```

---

## ❓ ¿ELIMINAR AuthCredential COMPLETAMENTE?

### ✅ **SÍ, ELIMINARLO**

**Razones:**

1. ✅ **Keycloak gestiona autenticación** → No necesitas `passwordHash`, `failedLoginAttempts`, etc.
2. ✅ **User es el agregado raíz** → `keycloakUserId` es solo un atributo
3. ✅ **Evita duplicación** → `roles` está en ambos
4. ✅ **Simplifica queries** → No necesitas JOIN entre colecciones
5. ✅ **Sigue DDD** → Un solo agregado para Usuario

### ⚠️ **ALTERNATIVA: Mantener AuthCredential SOLO SI...**

Solo mantén `AuthCredential` separado si:

- Necesitas **auditoría detallada** de autenticación independiente de User
- Quieres **historial de cambios de roles** sin tocar User
- Tienes **requisitos de compliance** que exigen separar autenticación de identidad

En ese caso, simplifica `AuthCredential` a:

```java
@Document(collection = "auth_credentials")
public class AuthCredential {
    @Id
    private String id;

    private String userId;              // FK a User
    private String keycloakUserId;      // Keycloak sub
    private String organizationId;      // Multi-tenant

    private LocalDateTime lastLoginAt;  // Solo auditoría
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

Pero **en general, NO es necesario**.

---

## 🎯 RECOMENDACIÓN FINAL

### ✅ **FUSIONAR EN USER**

```java
User {
    id
    keycloakUserId       ← ¡Agregar esto!
    username
    organizationId       ← Multi-tenant
    personalInfo         ← Value Object
    contact              ← Value Object
    address              ← Value Object
    roles
    status
    registrationDate
    lastLogin
    createdAt
    updatedAt
}
```

### ❌ **ELIMINAR AuthCredential**

Keycloak ya gestiona:

- ❌ Contraseñas
- ❌ Bloqueos
- ❌ Intentos fallidos
- ❌ Sesiones

**Tu MongoDB solo necesita:**

- ✅ Vínculo `keycloakUserId` → MongoDB `id`
- ✅ Datos de perfil (personalInfo, contact, address)
- ✅ Multi-tenant (`organizationId`)
- ✅ Roles de negocio

---

**¿Necesitas ayuda implementando estos cambios?** 🚀
