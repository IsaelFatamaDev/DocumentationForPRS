# 🚀 RESUMEN EJECUTIVO: Arquitectura de Autenticación JASS

> **Respuesta rápida a tus preguntas críticas**

---

## ❓ Preguntas y Respuestas

### 1. ¿Es necesario el microservicio vg-ms-authentication?

**✅ SÍ, ES NECESARIO**

**Razón:** Tu respuesta de login actual demuestra por qué:

```json
{
  "accessToken": "eyJhbGc...",         // ← JWT de Keycloak
  "refreshToken": "eyJhbGc...",
  "userInfo": {
    "userId": "68c0a4ab...",          // ← MongoDB ID (NO está en JWT)
    "organizationId": "6896b2ec...",  // ← Dato extendido (NO está en JWT)
    "mustChangePassword": false       // ← Estado de seguridad (NO está en JWT)
  }
}
```

Sin `vg-ms-authentication`, tendrías que:

- ❌ Hacer 2 requests en cada login (Keycloak + MongoDB)
- ❌ Exponer Keycloak directamente al frontend
- ❌ Gestionar sincronización manualmente

Con `vg-ms-authentication`:

- ✅ 1 request que orquesta todo
- ✅ Keycloak oculto detrás del microservicio
- ✅ Sincronización automática de `sub` ↔ `userId`

---

### 2. ¿Cuál microservicio se conecta a Keycloak?

| Microservicio | Conexión a Keycloak | Propósito |
|---------------|---------------------|-----------|
| **vg-ms-gateway** | ✅ **JWK Set** (validar tokens) | Lee clave pública para validar firma JWT |
| **vg-ms-authentication** | ✅ **Admin API** (operaciones) | Login, registro, cambio password, logout |
| **vg-ms-users** | ❌ NO conecta | Solo almacena `keycloakUserId` como referencia |
| **Otros 7 MS** | ❌ NO conectan | Leen headers del Gateway |

**Resumen:** Solo 2 MS conectan a Keycloak, pero con propósitos diferentes.

---

### 3. ¿Cómo funciona JWT vs JWE en tu arquitectura?

#### JWT de Keycloak (Externo: Cliente ↔ Sistema)

```
📱 Cliente → Authorization: Bearer {JWT}
    ↓
🛡️ Gateway → Valida JWT con Keycloak JWK Set
    ↓ Headers: X-Keycloak-Sub, X-User-Roles
📦 Microservicio → Lee headers
```

**Contenido de tu JWT:**

```json
{
  "sub": "ab97f6ed-66e3-4484-a764-36385b10b703",
  "preferred_username": "javier.fatama@jass.gob.pe",
  "realm_access": {"roles": ["ADMIN"]},
  "email": "jfatama@gmail.com"
}
```

- ✅ Emitido por Keycloak
- ✅ Firmado con RS256 (verificable con clave pública)
- ✅ Duración: 1 hora
- ❌ **NO cifrado** (cualquiera puede decodificarlo)

#### JWE Interno (Interno: MS ↔ MS)

```
📦 vg-ms-users → X-Internal-Token: {JWE cifrado}
    ↓
🏢 vg-ms-organizations → Descifra con secret compartido
```

- ✅ Generado por el MS origen
- ✅ Cifrado con A256GCM (NO se puede leer sin la clave)
- ✅ Duración: 5 minutos
- ✅ Solo para comunicación interna

---

## 🎯 Arquitectura Simplificada

```
🌐 INTERNET
    ↓ JWT de Keycloak
┌─────────────────────────────────────┐
│  🛡️ GATEWAY (9090) - PÚBLICO       │  ✅ Valida JWT
│  - Único puerto expuesto            │  ✅ Extrae claims
└─────────────────────────────────────┘  ✅ Agrega headers
    ↓ Headers internos
┌─────────────────────────────────────┐
│  🔐 RED INTERNA                     │
│                                     │
│  ┌──────────────────────────────┐  │
│  │ vg-ms-authentication (9092) │  │  ✅ Conecta a Keycloak Admin
│  │ - Login/Logout               │  │  ✅ Orquesta autenticación
│  │ - Refresh tokens             │  │  ✅ Enriquece respuesta
│  └──────────────────────────────┘  │
│                                     │
│  ┌──────────────────────────────┐  │
│  │ vg-ms-users (8085)           │  │  ❌ NO conecta a Keycloak
│  │ - CRUD usuarios              │  │  ✅ Almacena keycloakUserId
│  │ - Gestión roles              │  │  ✅ Datos extendidos (org)
│  └──────────────────────────────┘  │
│                                     │
│  ┌──────────────────────────────┐  │
│  │ Otros 7 MS (8086-8093)       │  │  ❌ NO conectan a Keycloak
│  │ - Lógica de negocio          │  │  ✅ Leen headers Gateway
│  └──────────────────────────────┘  │  ✅ Validan JWE interno
│                                     │
└─────────────────────────────────────┘
         ↕️
┌─────────────────────────────────────┐
│  🔑 KEYCLOAK (Externo)              │
│  - Emite JWT                         │
│  - Gestiona sesiones                 │
└─────────────────────────────────────┘
```

---

## 🔑 Mapeo Crítico: Keycloak ↔ MongoDB

**El problema:** Tienes 2 IDs diferentes:

| Campo | Valor | Dónde |
|-------|-------|-------|
| **Keycloak ID** | `ab97f6ed-66e3-4484-a764-36385b10b703` | JWT claim `sub` |
| **MongoDB ID** | `68c0a4ab07fa2d47448b530a` | Colección `users` / `auth_credentials` |

**La solución:** AuthCredential vincula ambos:

```java
@Document(collection = "auth_credentials")
public class AuthCredential {
    private String userId;              // "68c0a4ab..."  ← MongoDB ID
    private String keycloakUserId;      // "ab97f6ed..."  ← Keycloak ID
    private String username;            // "javier.fatama@jass.gob.pe"
    private String organizationId;      // "6896b2ec..."  ← NO en Keycloak
    private List<RolesUsers> roles;     // ["ADMIN"]
    private boolean mustChangePassword;
}
```

**Flujo de búsqueda:**

1. Gateway recibe JWT → Extrae `sub` = "ab97f6ed..."
2. Gateway agrega header `X-Keycloak-Sub: ab97f6ed...`
3. Microservicio recibe header → Busca en MongoDB:

   ```java
   authCredentialRepository.findByKeycloakUserId(keycloakSub)
   ```

4. Encuentra el documento completo con `userId`, `organizationId`, etc.

---

## 🚀 Flujo Completo de Login

```
1. Cliente → Gateway: POST /auth/login {username, password}
   ↓
2. Gateway → vg-ms-authentication: Proxy request
   ↓
3. vg-ms-authentication → Keycloak: POST /token
   ↓
4. Keycloak → vg-ms-authentication: {access_token, refresh_token}
   ↓
5. vg-ms-authentication: Decodifica JWT, extrae sub
   ↓
6. vg-ms-authentication → vg-ms-users: GET /internal/users/by-keycloak-id/{sub}
   ↓
7. vg-ms-users → vg-ms-authentication: {userId, organizationId, roles, ...}
   ↓
8. vg-ms-authentication → Gateway: {
     accessToken: JWT,
     refreshToken: JWT,
     userInfo: {...}
   }
   ↓
9. Gateway → Cliente: Respuesta completa
```

**Duración total:** ~500-800ms

---

## 🔒 Seguridad por Capas

### Capa 1: Red

- ✅ Solo Gateway (9090) expuesto a internet
- ✅ MS internos en red privada

### Capa 2: Gateway

- ✅ Valida JWT de Keycloak
- ✅ Rate limiting
- ✅ CORS

### Capa 3: Microservicios

- ✅ Leen headers del Gateway (X-Keycloak-Sub, X-User-Roles)
- ✅ Validación adicional opcional (permisos específicos de recursos)

### Capa 4: Comunicación Interna

- ✅ JWE cifrado para MS ↔ MS
- ✅ Secret compartido
- ✅ Expiración corta (5 min)

---

## ✅ Checklist de Implementación

### Gateway

- [ ] Validar JWT con Keycloak JWK Set
- [ ] Extraer claims: `sub`, `realm_access.roles`, `email`, `preferred_username`
- [ ] Agregar headers: `X-Keycloak-Sub`, `X-User-Roles`, `X-User-Email`, `X-Username`
- [ ] Configurar rutas públicas: `/auth/**`, `/actuator/health`, `/docs/**`

### vg-ms-authentication

- [ ] Conectar a Keycloak Admin API
- [ ] Implementar login: Keycloak → MongoDB → Respuesta enriquecida
- [ ] Implementar refresh token
- [ ] Implementar cambio de password
- [ ] Implementar logout

### vg-ms-users

- [ ] **NO** configurar OAuth2 Resource Server
- [ ] Almacenar `keycloakUserId` en AuthCredential
- [ ] Buscar usuarios por `keycloakUserId`
- [ ] Sincronizar con Keycloak cuando sea necesario

### Otros MS (7)

- [ ] **NO** configurar OAuth2 Resource Server
- [ ] Leer headers del Gateway
- [ ] Implementar validación JWE para comunicación interna

### Networking

- [ ] Docker networks: `jass-public` (Gateway) + `jass-internal` (MS)
- [ ] Firewall: Bloquear puertos 8085-8093, 9092 desde internet
- [ ] Exponer solo puerto 9090

---

## 🎓 Respuesta Final

### ¿Los 3 MS son necesarios?

| Microservicio | Necesario | Razón |
|---------------|-----------|-------|
| **vg-ms-gateway** | ✅ SÍ | Patrón estándar, única puerta de entrada |
| **vg-ms-authentication** | ✅ SÍ | Orquesta login, enriquece respuesta, conecta con Keycloak |
| **vg-ms-users** | ✅ SÍ | Gestiona datos extendidos, dominio específico |

**Conclusión:** La separación es **correcta** y sigue **best practices** de microservicios.

---

## 📚 Documentación Completa

Para más detalles, ver: [`Authentication.md`](./Authentication.md)

- Flujos completos con diagramas de secuencia
- Código de implementación (Java/Spring)
- Configuración Docker
- Plan de migración por fases
- Consideraciones de seguridad

---

**Versión:** 2.0.0
**Fecha:** 23 de octubre de 2025
**Estado:** ✅ Listo para implementación
