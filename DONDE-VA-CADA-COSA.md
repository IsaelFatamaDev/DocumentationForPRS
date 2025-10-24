# 🎯 DÓNDE VA CADA COSA - ARQUITECTURA DE AUTENTICACIÓN

> **Guía definitiva: ¿Dónde manejo qué?**

---

## 📍 RESUMEN EJECUTIVO

Basado en tu respuesta actual de login:

```
POST https://lab.vallegrande.edu.pe/jass/ms-gateway/auth/login
→ Respuesta: {accessToken, refreshToken, userInfo}
```

**Flujo completo:**

```
1. 🌐 Cliente
   ↓ POST /auth/login {username, password}

2. 🛡️ vg-ms-gateway (Puerto 9090)
   ↓ Ruta pública → No valida JWT
   ↓ Proxy a vg-ms-authentication

3. 🔐 vg-ms-authentication (Puerto 9092)
   ↓ A. Llama a Keycloak → Obtiene JWT
   ↓ B. Llama a vg-ms-users → Obtiene userInfo
   ↓ C. Combina ambos → Respuesta completa

4. 🛡️ vg-ms-gateway
   ↓ Devuelve respuesta al cliente

5. 🌐 Cliente
   ✅ Recibe: {accessToken, refreshToken, userInfo}
```

---

## 🔐 vg-ms-authentication (Puerto 9092)

### ✅ RESPONSABILIDADES (Lo que SÍ hace)

#### 1. **CONECTA A KEYCLOAK** ← AQUÍ MANEJAS KEYCLOAK

```java
// infrastructure/repository/KeycloakRepository.java
@Repository
@RequiredArgsConstructor
public class KeycloakRepository {

    @Value("${keycloak.url}")
    private String keycloakUrl;  // https://lab.vallegrande.edu.pe/jass/keycloak

    @Value("${keycloak.realm}")
    private String realm;  // sistema-jass

    private final Keycloak keycloakClient;  // Cliente admin de Keycloak

    /**
     * 🔑 LOGIN: Obtiene JWT de Keycloak
     */
    public Mono<AuthResponse> authenticate(String username, String password) {
        return Mono.fromCallable(() -> {
            // 1. Llamar a Keycloak Token Endpoint
            Form form = new Form()
                .param("grant_type", "password")
                .param("client_id", "jass-users-service")
                .param("username", username)
                .param("password", password);

            Response response = keycloakClient
                .realm(realm)
                .getTokenEndpoint()
                .requestToken(form);

            // 2. Parsear respuesta de Keycloak
            AccessTokenResponse tokenResponse = response.readEntity(AccessTokenResponse.class);

            // 3. Retornar JWT
            return AuthResponse.builder()
                .accessToken(tokenResponse.getToken())         // ← JWT access_token
                .refreshToken(tokenResponse.getRefreshToken()) // ← JWT refresh_token
                .expiresIn(tokenResponse.getExpiresIn())
                .tokenType("Bearer")
                .build();
        });
    }

    /**
     * 🔄 REFRESH TOKEN: Renueva JWT
     */
    public Mono<AuthResponse> refreshToken(String refreshToken) {
        return Mono.fromCallable(() -> {
            Form form = new Form()
                .param("grant_type", "refresh_token")
                .param("client_id", "jass-users-service")
                .param("refresh_token", refreshToken);

            Response response = keycloakClient
                .realm(realm)
                .getTokenEndpoint()
                .requestToken(form);

            AccessTokenResponse tokenResponse = response.readEntity(AccessTokenResponse.class);

            return AuthResponse.builder()
                .accessToken(tokenResponse.getToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .expiresIn(tokenResponse.getExpiresIn())
                .tokenType("Bearer")
                .build();
        });
    }

    /**
     * 🆕 CREAR USUARIO en Keycloak
     */
    public Mono<String> createUserInKeycloak(RegisterUserRequest request) {
        return Mono.fromCallable(() -> {
            UserRepresentation user = new UserRepresentation();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setEnabled(true);
            user.setEmailVerified(true);

            // Credencial
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(request.getPassword());
            credential.setTemporary(false);
            user.setCredentials(List.of(credential));

            // Crear en Keycloak
            Response response = keycloakClient
                .realm(realm)
                .users()
                .create(user);

            String keycloakUserId = extractUserIdFromResponse(response);

            // Asignar roles
            assignRoles(keycloakUserId, request.getRoles());

            return keycloakUserId;
        });
    }

    /**
     * 🔒 CAMBIAR CONTRASEÑA en Keycloak
     */
    public Mono<Void> changePassword(String keycloakUserId, String newPassword) {
        return Mono.fromRunnable(() -> {
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(newPassword);
            credential.setTemporary(false);

            keycloakClient
                .realm(realm)
                .users()
                .get(keycloakUserId)
                .resetPassword(credential);
        });
    }

    /**
     * 🚪 LOGOUT: Invalida sesión en Keycloak
     */
    public Mono<Void> logout(String keycloakUserId) {
        return Mono.fromRunnable(() -> {
            keycloakClient
                .realm(realm)
                .users()
                .get(keycloakUserId)
                .logout();
        });
    }
}
```

#### 2. **LLAMA A vg-ms-users** para obtener userInfo

```java
// infrastructure/client/UsersClient.java
@Component
@RequiredArgsConstructor
public class UsersClientImpl implements UsersClient {

    private final WebClient webClient;

    @Value("${app.services.users.url}")
    private String usersServiceUrl;  // https://lab.vallegrande.edu.pe/jass/ms-users/api

    /**
     * 👤 Obtiene userInfo de vg-ms-users
     */
    @Override
    public Mono<UserCompleteDto> getUserByKeycloakId(String keycloakUserId) {
        return webClient.get()
            .uri(usersServiceUrl + "/internal/users/by-keycloak-id/{keycloakUserId}", keycloakUserId)
            .retrieve()
            .bodyToMono(UserCompleteDto.class);
    }
}
```

#### 3. **ORQUESTA TODO** en el login

```java
// application/service/impl/AuthApplicationServiceImpl.java
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthApplicationServiceImpl implements AuthApplicationService {

    private final KeycloakRepository keycloakRepository;
    private final UsersClient usersClient;

    /**
     * 🎯 LOGIN COMPLETO
     * Este es el método que orquesta todo
     */
    @Override
    public Mono<ApiResponse<AuthResponse>> login(LoginRequest request) {
        log.info("Iniciando login para usuario: {}", request.getUsername());

        // 1️⃣ Autenticar en Keycloak
        return keycloakRepository.authenticate(request.getUsername(), request.getPassword())
            .flatMap(authResponse -> {
                log.info("✅ Autenticación exitosa en Keycloak");

                // 2️⃣ Decodificar JWT para obtener el 'sub' (Keycloak User ID)
                String keycloakUserId = extractSubFromJwt(authResponse.getAccessToken());
                log.info("Keycloak User ID (sub): {}", keycloakUserId);

                // 3️⃣ Obtener userInfo de vg-ms-users
                return usersClient.getUserByKeycloakId(keycloakUserId)
                    .map(userInfo -> {
                        log.info("✅ UserInfo obtenido de vg-ms-users");
                        log.info("   - userId (MongoDB): {}", userInfo.getUserId());
                        log.info("   - organizationId: {}", userInfo.getOrganizationId());

                        // 4️⃣ Combinar JWT + userInfo
                        authResponse.setUserInfo(userInfo);

                        return ApiResponse.<AuthResponse>builder()
                            .success(true)
                            .message("Login exitoso")
                            .data(authResponse)
                            .timestamp(LocalDateTime.now())
                            .build();
                    });
            })
            .onErrorResume(error -> {
                log.error("❌ Error en login: {}", error.getMessage());
                return Mono.just(ApiResponse.<AuthResponse>builder()
                    .success(false)
                    .message("Credenciales inválidas")
                    .timestamp(LocalDateTime.now())
                    .build());
            });
    }

    /**
     * Extrae el 'sub' del JWT sin validar la firma
     */
    private String extractSubFromJwt(String accessToken) {
        String[] parts = accessToken.split("\\.");
        String payload = new String(Base64.getDecoder().decode(parts[1]));

        // Parsear JSON
        JSONObject json = new JSONObject(payload);
        return json.getString("sub");
    }
}
```

#### 4. **ENDPOINT DE LOGIN**

```java
// application/rest/AuthenticationController.java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación")
public class AuthenticationController {

    private final AuthApplicationService authApplicationService;

    /**
     * 🔑 LOGIN
     * POST /api/auth/login
     */
    @PostMapping("/login")
    @Operation(summary = "Login de usuario")
    public Mono<ResponseEntity<ApiResponse<AuthResponse>>> login(
        @Valid @RequestBody LoginRequest request
    ) {
        return authApplicationService.login(request)
            .map(response -> response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response)
            );
    }

    /**
     * 🔄 REFRESH TOKEN
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public Mono<ResponseEntity<ApiResponse<AuthResponse>>> refresh(
        @Valid @RequestBody RefreshTokenRequest request
    ) {
        return authApplicationService.refreshToken(request.getRefreshToken())
            .map(ResponseEntity::ok);
    }

    /**
     * 🔒 CAMBIAR CONTRASEÑA
     * POST /api/auth/change-password
     */
    @PostMapping("/change-password")
    public Mono<ResponseEntity<ApiResponse<String>>> changePassword(
        @Valid @RequestBody ChangePasswordRequest request,
        @RequestHeader("X-Keycloak-Sub") String keycloakUserId
    ) {
        return authApplicationService.changePassword(keycloakUserId, request)
            .map(ResponseEntity::ok);
    }

    /**
     * 🚪 LOGOUT
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public Mono<ResponseEntity<ApiResponse<String>>> logout(
        @RequestHeader("X-Keycloak-Sub") String keycloakUserId
    ) {
        return authApplicationService.logout(keycloakUserId)
            .map(ResponseEntity::ok);
    }
}
```

### ❌ LO QUE NO HACE vg-ms-authentication

- ❌ NO valida JWT en cada request (eso lo hace el Gateway)
- ❌ NO gestiona usuarios (CRUD) - eso lo hace vg-ms-users
- ❌ NO tiene base de datos propia
- ❌ NO conoce organizaciones más allá de lo que vg-ms-users le devuelve

---

## 👥 vg-ms-users (Puerto 8085)

### ✅ RESPONSABILIDADES

#### 1. **ENDPOINT INTERNO** para obtener userInfo

```java
// infrastructure/rest/InternalUserController.java
@RestController
@RequestMapping("/api/internal/users")
@RequiredArgsConstructor
@Tag(name = "Internal Users API")
public class InternalUserController {

    private final UserService userService;

    /**
     * 🔍 Obtiene usuario por Keycloak ID
     * Este endpoint es llamado por vg-ms-authentication
     */
    @GetMapping("/by-keycloak-id/{keycloakUserId}")
    public Mono<ResponseEntity<UserCompleteDto>> getUserByKeycloakId(
        @PathVariable String keycloakUserId
    ) {
        return userService.getUserByKeycloakId(keycloakUserId)
            .map(user -> ResponseEntity.ok(UserCompleteDto.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .organizationId(user.getOrganizationId())  // ← CRÍTICO
                .roles(user.getRoles())
                .mustChangePassword(user.isMustChangePassword())
                .lastLogin(LocalDateTime.now())
                .build()))
            .switchIfEmpty(Mono.error(new NotFoundException("Usuario no encontrado")));
    }
}
```

#### 2. **BUSCA EN MONGODB** por keycloakUserId

```java
// application/service/impl/UserServiceImpl.java
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final AuthCredentialRepository authCredentialRepository;
    private final UserRepository userRepository;

    @Override
    public Mono<User> getUserByKeycloakId(String keycloakUserId) {
        // 1. Buscar AuthCredential por keycloakUserId
        return authCredentialRepository.findByKeycloakUserId(keycloakUserId)
            .switchIfEmpty(Mono.error(new NotFoundException(
                "No se encontró usuario con keycloakUserId: " + keycloakUserId
            )))
            .flatMap(authCredential -> {
                // 2. Actualizar lastLogin
                authCredential.recordSuccessfulLogin();

                // 3. Obtener User completo
                return authCredentialRepository.save(authCredential)
                    .then(userRepository.findById(authCredential.getUserId()));
            });
    }
}
```

#### 3. **MODELO DE DATOS**

```java
// domain/model/AuthCredential.java
@Document(collection = "auth_credentials")
public class AuthCredential {
    @Id
    private String authCredentialId;

    private String userId;              // "68c0a4ab07fa2d47448b530a" ← MongoDB ID
    private String keycloakUserId;      // "ab97f6ed-66e3-4484..." ← Keycloak sub
    private String username;            // "javier.fatama@jass.gob.pe"
    private String organizationId;      // "6896b2ecf3e398570ffd99d3"
    private List<RolesUsers> roles;     // ["ADMIN"]
    private LocalDateTime lastLoginAt;

    public void recordSuccessfulLogin() {
        this.lastLoginAt = LocalDateTime.now();
        this.failedLoginAttempts = 0;
        this.updatedAt = LocalDateTime.now();
    }
}
```

### ❌ LO QUE NO HACE vg-ms-users

- ❌ NO conecta a Keycloak directamente
- ❌ NO autentica usuarios (solo gestiona sus datos)
- ❌ NO emite tokens

---

## 🛡️ vg-ms-gateway (Puerto 9090)

### ✅ RESPONSABILIDADES

#### 1. **ROUTING** - Envía /auth/* a vg-ms-authentication

```java
// infrastructure/route/AuthenticationServiceRoute.java
@Configuration
public class AuthenticationServiceRoute {

    @Value("${authentication.service.url}")
    private String authenticationServiceUrl;

    @Bean
    public RouteLocator authenticationServiceRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            // ✅ Login (PÚBLICO - no requiere JWT)
            .route("auth-login", route -> route
                .path("/auth/login")
                .filters(f -> f.rewritePath(
                    "/auth/(?<segment>.*)",
                    "/jass/ms-authentication/api/auth/${segment}"
                ))
                .uri(authenticationServiceUrl))

            // ✅ Refresh (PÚBLICO)
            .route("auth-refresh", route -> route
                .path("/auth/refresh")
                .filters(f -> f.rewritePath(
                    "/auth/(?<segment>.*)",
                    "/jass/ms-authentication/api/auth/${segment}"
                ))
                .uri(authenticationServiceUrl))

            // ... más rutas
            .build();
    }
}
```

#### 2. **SEGURIDAD** - Permite /auth/* sin validación

```java
// application/config/SecurityConfig.java
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .authorizeExchange(exchanges -> exchanges
                // ✅ PÚBLICO: Login y refresh no requieren JWT
                .pathMatchers("/auth/**").permitAll()
                .pathMatchers("/actuator/health").permitAll()
                .pathMatchers("/docs/**").permitAll()

                // ✅ PROTEGIDO: Todo lo demás requiere JWT válido
                .pathMatchers("/api/**").authenticated()
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt())
            .build();
    }
}
```

#### 3. **VALIDACIÓN JWT** (para requests autenticados)

```java
// infrastructure/filter/CustomAuthenticationFilter.java
@Component
public class CustomAuthenticationFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();

        // Si es ruta pública, no validar JWT
        if (path.startsWith("/auth/") || path.startsWith("/actuator/")) {
            return chain.filter(exchange);
        }

        // Para rutas protegidas, extraer claims del JWT
        return exchange.getPrincipal()
            .cast(JwtAuthenticationToken.class)
            .flatMap(authentication -> {
                Jwt jwt = (Jwt) authentication.getPrincipal();

                // Extraer claims
                String keycloakSub = jwt.getClaimAsString("sub");
                String email = jwt.getClaimAsString("email");
                String username = jwt.getClaimAsString("preferred_username");
                List<String> roles = extractRoles(jwt);

                // Agregar headers para microservicios internos
                ServerHttpRequest request = exchange.getRequest().mutate()
                    .header("X-Keycloak-Sub", keycloakSub)
                    .header("X-User-Email", email)
                    .header("X-Username", username)
                    .header("X-User-Roles", String.join(",", roles))
                    .header("X-Authenticated", "true")
                    .build();

                return chain.filter(exchange.mutate().request(request).build());
            });
    }

    private List<String> extractRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            List<String> allRoles = (List<String>) realmAccess.get("roles");
            // Filtrar solo roles de negocio
            return allRoles.stream()
                .filter(role -> role.equals("ADMIN") ||
                               role.equals("CLIENT") ||
                               role.equals("SUPER_ADMIN"))
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
```

### ❌ LO QUE NO HACE vg-ms-gateway

- ❌ NO conecta a Keycloak Admin API (solo usa JWK Set para validar)
- ❌ NO hace login/logout
- ❌ NO consulta base de datos
- ❌ NO modifica el JWT

---

## 🔑 Keycloak (Servicio Externo)

### ✅ LO QUE HACE

1. **Emite JWT** cuando vg-ms-authentication llama a `/token`
2. **Provee JWK Set** para que el Gateway valide JWT
3. **Gestiona usuarios y contraseñas** (base de datos interna de Keycloak)
4. **Invalida sesiones** cuando se hace logout

### 🔌 CONEXIONES

```
vg-ms-authentication → Keycloak Admin API
   - POST /realms/sistema-jass/protocol/openid-connect/token (login)
   - POST /admin/realms/sistema-jass/users (crear usuario)
   - PUT /admin/realms/sistema-jass/users/{id}/reset-password
   - POST /admin/realms/sistema-jass/users/{id}/logout

vg-ms-gateway → Keycloak JWK Set (solo lectura)
   - GET /realms/sistema-jass/protocol/openid-connect/certs
   (Valida firma del JWT)
```

---

## 📋 CHECKLIST DE CONFIGURACIÓN

### vg-ms-authentication

```yaml
# application.yml
keycloak:
  url: https://lab.vallegrande.edu.pe/jass/keycloak
  realm: sistema-jass
  admin-username: admin
  admin-password: ${KEYCLOAK_ADMIN_PASSWORD}
  admin-client-id: admin-cli

app:
  services:
    users:
      url: https://lab.vallegrande.edu.pe/jass/ms-users/api
```

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.keycloak</groupId>
    <artifactId>keycloak-admin-client</artifactId>
    <version>25.0.6</version>
</dependency>
```

### vg-ms-gateway

```yaml
# application.yml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://lab.vallegrande.edu.pe/jass/keycloak/realms/sistema-jass
          jwk-set-uri: https://lab.vallegrande.edu.pe/jass/keycloak/realms/sistema-jass/protocol/openid-connect/certs

authentication:
  service:
    url: https://lab.vallegrande.edu.pe
```

### vg-ms-users

```yaml
# application.yml
# ❌ NO necesita configuración de Keycloak

spring:
  data:
    mongodb:
      uri: mongodb+srv://...
      database: JASS_DIGITAL
```

---

## 🎯 RESUMEN FINAL

| ¿Dónde? | ¿Qué hace? | Conexión a Keycloak |
|---------|------------|---------------------|
| **vg-ms-authentication** | 🔐 Login, registro, cambio password | ✅ **SÍ** - Admin API |
| **vg-ms-gateway** | 🛡️ Valida JWT, routing | ✅ **SÍ** - JWK Set (solo lectura) |
| **vg-ms-users** | 👥 CRUD usuarios, gestión datos | ❌ **NO** |
| **Otros 7 MS** | 📦 Lógica de negocio | ❌ **NO** |

**LA CONEXIÓN PRINCIPAL A KEYCLOAK ESTÁ EN:**
👉 **`vg-ms-authentication`** 👈

---

## 🚀 TU FLUJO ACTUAL (CORRECTO)

```
1. POST /auth/login {username, password}
   ↓
2. Gateway → No valida (ruta pública)
   ↓
3. vg-ms-authentication:
   A. ✅ Llama a Keycloak → Obtiene JWT
   B. ✅ Llama a vg-ms-users → Obtiene userInfo
   C. ✅ Combina → {JWT + userInfo}
   ↓
4. Gateway → Devuelve respuesta completa
   ↓
5. Cliente recibe:
   {
     accessToken: "eyJhbGc...",
     refreshToken: "eyJhbGc...",
     userInfo: {
       userId: "68c0a4ab...",
       organizationId: "6896b2ec...",
       roles: ["ADMIN"]
     }
   }
```

**✅ ESTO ESTÁ BIEN ASÍ**

---

**Versión:** 2.0.0
**Fecha:** 24 de octubre de 2025
**Estado:** ✅ Documentación completa
