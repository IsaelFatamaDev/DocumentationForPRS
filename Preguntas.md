# ❓ Preguntas Frecuentes - Implementación JWE

## 📚 Preguntas Técnicas que Puede Hacer el Profesor

---

## 🔐 1. ¿Qué algoritmo de cifrado se está usando?

### Respuesta

Se está usando **JWE (JSON Web Encryption)** con dos algoritmos:

1. **Algoritmo de Gestión de Claves:** `DIR` (Direct Encryption)
2. **Algoritmo de Cifrado de Contenido:** `A256GCM` (AES-256-GCM)

### Detalles Técnicos

```
DIR = Direct Encryption with symmetric key (clave simétrica compartida)
A256GCM = AES de 256 bits en modo GCM (Galois/Counter Mode)
```

**¿Por qué esta combinación?**

- `DIR`: Usa una clave secreta compartida directamente (no genera clave efímera)
- `A256GCM`: Cifrado autenticado que garantiza confidencialidad + integridad

---

## 📂 2. ¿En qué sección o archivo del código se está cifrando?

### Archivos Clave

#### **MS-USERS (Servidor - Valida):**

1. **`JweDemoService.java`**
   - **Ubicación:** `src/main/java/pe/edu/vallegrande/vgmsusers/infrastructure/security/`
   - **Métodos de Cifrado:**
     - `generateInternalToken()` - Genera token JWE
     - `validateInternalToken()` - Desencripta y valida token

2. **`InternalJweFilter.java`**
   - **Ubicación:** `src/main/java/pe/edu/vallegrande/vgmsusers/infrastructure/security/`
   - **Función:** Intercepta peticiones `/internal/*` y valida JWE

#### **MS-ORGANIZATION (Cliente - Genera):**

3. **`JweTokenService.java`**
   - **Ubicación:** `src/main/java/pe/edu/vallegrande/vgmsorganization/infrastructure/security/`
   - **Métodos de Cifrado:**
     - `generateServiceToken()` - Crea token JWE para autenticarse

4. **`MsUsersClient.java`**
   - **Ubicación:** `src/main/java/pe/edu/vallegrande/vgmsorganization/infrastructure/client/`
   - **Función:** Usa tokens JWE para llamar a MS-USERS

---

## ⚙️ 3. ¿Qué métodos se están usando para cifrar?

### Flujo Completo de Cifrado

#### **Paso 1: Derivación de Clave (SHA-256)**

```java
// JweDemoService.java - Línea ~45
MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
byte[] keyBytes = sha256.digest(jweSecret.getBytes(StandardCharsets.UTF_8));
```

**¿Por qué?** Convierte el secreto compartido (string) en una clave de 256 bits.

#### **Paso 2: Crear Claims (Payload)**

```java
// JweDemoService.java - Línea ~51
JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
    .subject(subject)
    .issuer(issuer)
    .audience(audience)
    .expirationTime(expirationDate)
    .issueTime(now)
    .claim("scope", scope)
    .build();
```

**¿Por qué?** Define qué información va cifrada en el token.

#### **Paso 3: Configurar Header JWE**

```java
// JweDemoService.java - Línea ~60
JWEHeader header = new JWEHeader.Builder(JWEAlgorithm.DIR, EncryptionMethod.A256GCM)
    .contentType("JWT")
    .build();
```

**¿Por qué?** Especifica los algoritmos usados (DIR + A256GCM).

#### **Paso 4: Cifrar con SecretKey**

```java
// JweDemoService.java - Línea ~65
JWEObject jweObject = new JWEObject(header, new Payload(claimsSet.toJSONObject()));
SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
DirectEncrypter encrypter = new DirectEncrypter(secretKey);
jweObject.encrypt(encrypter);
```

**¿Por qué?** Realiza el cifrado AES-256-GCM del payload completo.

#### **Paso 5: Serializar Token**

```java
// JweDemoService.java - Línea ~69
return jweObject.serialize();
```

**¿Por qué?** Convierte el objeto JWE en string Base64 URL-safe para transmisión HTTP.

---

## 🔄 4. ¿Por qué cifrar en lugar de solo firmar (JWT)?

### Comparación: JWT vs JWE

| Aspecto | JWT (Firma) | JWE (Cifrado) |
|---------|-------------|---------------|
| **Confidencialidad** | ❌ Payload visible en Base64 | ✅ Payload completamente cifrado |
| **Integridad** | ✅ Detecta alteraciones | ✅ Detecta alteraciones |
| **Algoritmo** | HMAC-SHA256 / RS256 | AES-256-GCM |
| **Uso** | Autenticación pública | Comunicación privada |
| **Decodificable** | ✅ Con jwt.io | ❌ Necesita clave secreta |

### Ejemplo Real

**JWT Firmado (RS256):**

```
eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJtcy1vcmdhbml6YXRpb24ifQ.signature
```

👆 Puedes decodificar en jwt.io y ver: `{"sub":"ms-organization"}`

**JWE Cifrado (DIR + A256GCM):**

```
eyJhbGciOiJkaXIiLCJlbmMiOiJBMjU2R0NNIn0..zXkw7Q.encrypted_content.tag
```

👆 NO puedes ver el contenido sin la clave secreta

**¿Por qué elegimos JWE?**

- Comunicación **interna** entre microservicios
- Datos sensibles (roles, scopes, organizationId)
- Necesitamos **confidencialidad** + integridad

---

## 🔍 5. ¿Qué otros algoritmos de cifrado hay?

### Alternativas JWE

#### **A) Algoritmos de Gestión de Claves:**

| Algoritmo | Tipo | Descripción |
|-----------|------|-------------|
| **DIR** ✅ | Simétrico | Clave compartida directamente (el que usamos) |
| RSA-OAEP | Asimétrico | Cifrado con clave pública RSA |
| ECDH-ES | Asimétrico | Diffie-Hellman con curvas elípticas |
| A256KW | Simétrico | AES Key Wrap (envuelve otra clave) |
| PBES2-HS256 | Basado en password | Deriva clave de contraseña |

#### **B) Algoritmos de Cifrado de Contenido:**

| Algoritmo | Tamaño Clave | Modo | Autenticado |
|-----------|--------------|------|-------------|
| **A256GCM** ✅ | 256 bits | GCM | ✅ Sí (el que usamos) |
| A128GCM | 128 bits | GCM | ✅ Sí |
| A192GCM | 192 bits | GCM | ✅ Sí |
| A128CBC-HS256 | 128 bits | CBC | ✅ Con HMAC |
| A256CBC-HS512 | 256 bits | CBC | ✅ Con HMAC |

---

## 🤔 6. ¿Por qué NO elegiste otros algoritmos?

### ❌ Por qué NO RSA-OAEP

**Ventajas:**

- No necesita secreto compartido
- Cada servicio tiene par de claves (pública/privada)

**Desventajas:**

- ⚠️ Más complejo de implementar
- ⚠️ Gestión de certificados PKI
- ⚠️ Más lento (operaciones asimétricas)
- ⚠️ Tamaño de token mayor

**Conclusión:** Innecesario para comunicación interna entre servicios de confianza.

---

### ❌ Por qué NO A128GCM (128 bits)

**Ventajas:**

- Más rápido que 256 bits
- Menor consumo de CPU

**Desventajas:**

- ⚠️ Menor seguridad (128 vs 256 bits)
- ⚠️ No cumple estándares de seguridad modernos
- ⚠️ Menos resistente a ataques de fuerza bruta

**Conclusión:** AES-256 es el estándar de seguridad actual (usado por gobiernos).

---

### ❌ Por qué NO A256CBC-HS512

**Ventajas:**

- También provee confidencialidad + integridad

**Desventajas:**

- ⚠️ Modo CBC requiere padding (vulnerable a padding oracle attacks)
- ⚠️ Necesita HMAC separado para autenticación
- ⚠️ GCM es más eficiente (autenticación integrada)

**Conclusión:** GCM es superior: más rápido, más seguro, autenticación incorporada.

---

### ❌ Por qué NO ECDH-ES (Curvas Elípticas)

**Ventajas:**

- Claves más pequeñas que RSA
- Seguridad similar a RSA con menos bits

**Desventajas:**

- ⚠️ Complejidad de implementación
- ⚠️ Requiere acuerdo de clave efímera
- ⚠️ Overhead de generación de claves por cada token

**Conclusión:** Exceso de ingeniería para comunicación interna.

---

## ✅ 7. Justificación de la Elección: DIR + A256GCM

### Criterios de Decisión

| Criterio | DIR + A256GCM | Alternativas |
|----------|---------------|--------------|
| **Seguridad** | ✅ 256 bits AES | RSA (mayor overhead) |
| **Rendimiento** | ✅ Muy rápido | CBC+HMAC (más lento) |
| **Simplicidad** | ✅ Clave compartida | PKI (complejo) |
| **Autenticación** | ✅ Incorporada (GCM) | CBC (necesita HMAC aparte) |
| **Estándar** | ✅ RFC 7516 | Propietario (❌) |
| **Compatibilidad** | ✅ Amplia (Nimbus) | Limitada |

### Ventajas Clave

1. **Seguridad de Grado Militar:** AES-256 usado por NSA para información clasificada
2. **Autenticación Integrada:** GCM detecta alteraciones automáticamente
3. **Rendimiento:** ~10x más rápido que RSA
4. **Simplicidad Operativa:** Solo necesitas sincronizar un secreto
5. **Estándar IETF:** RFC 7516 (JSON Web Encryption)

---

## 🔐 8. ¿Cómo se gestiona el secreto compartido?

### Configuración Actual

```yaml
# application.yml (ambos microservicios)
internal:
  security:
    jwe:
      secret: ${INTERNAL_JWE_SECRET:bWktc2VjcmV0by1zdXBlci1zZWd1cm8...}
```

### Buenas Prácticas

1. **Variables de Entorno:** `INTERNAL_JWE_SECRET` en producción
2. **Gestores de Secretos:**
   - AWS Secrets Manager
   - Azure Key Vault
   - HashiCorp Vault
3. **Rotación de Claves:** Cambiar secreto periódicamente
4. **No hardcodear:** Nunca en código fuente

---

## 🛡️ 9. ¿Qué protege JWE contra qué ataques?

### Protecciones Implementadas

| Ataque | Protección JWE |
|--------|----------------|
| **Man-in-the-Middle** | ✅ Contenido cifrado (no legible) |
| **Token Forgery** | ✅ GCM detecta alteraciones |
| **Replay Attacks** | ✅ Expiración + timestamp |
| **Padding Oracle** | ✅ GCM no usa padding |
| **Brute Force** | ✅ 2^256 combinaciones (imposible) |
| **Sniffing** | ✅ Payload cifrado en tránsito |

### Lo que NO protege

- ❌ Secreto comprometido (si filtran la clave, todo se descifra)
- ❌ Ataques físicos al servidor
- ❌ Insider threats con acceso al secreto

---

## 📊 10. ¿Cuál es el overhead de rendimiento?

### Benchmarks Aproximados

| Operación | Tiempo | Comparación |
|-----------|--------|-------------|
| **Generar token JWE** | ~2-5 ms | JWT: ~1-2 ms |
| **Validar token JWE** | ~2-5 ms | JWT: ~1-2 ms |
| **Overhead total** | ~10 ms | Aceptable para internal APIs |

### ¿Es un problema?

❌ **NO** porque:

- Comunicación interna (no expuesta a internet)
- ~10ms es despreciable vs tiempo de red (~50-100ms)
- Ganancia en seguridad >> pérdida de rendimiento

---

## 🔄 11. ¿Cómo se valida el token en MS-USERS?

### Proceso de Validación

```java
// InternalJweFilter.java - Intercepta petición
1. Extraer header Authorization
2. Verificar formato "Bearer <token>"
3. Llamar a JweDemoService.validateInternalToken()
4. Desencriptar con secreto compartido
5. Validar issuer == "vg-microservices"
6. Validar audience == "vg-ms-users"
7. Validar expiración (< 3600 segundos)
8. Si todo OK → Permitir acceso
9. Si falla → 401 Unauthorized
```

### Código de Validación

```java
// JweDemoService.java - validateInternalToken()
JWEObject jweObject = JWEObject.parse(token);
DirectDecrypter decrypter = new DirectDecrypter(secretKey);
jweObject.decrypt(decrypter);

JWTClaimsSet claimsSet = JWTClaimsSet.parse(jweObject.getPayload().toJSONObject());

// Validaciones
if (!issuer.equals(claimsSet.getIssuer())) {
    throw new RuntimeException("Issuer inválido");
}
if (!audience.equals(claimsSet.getAudience().get(0))) {
    throw new RuntimeException("Audience inválido");
}
if (expirationTime.before(new Date())) {
    throw new RuntimeException("Token expirado");
}
```

---

## 🎯 12. ¿Qué contiene el payload del token JWE?

### Claims Estándar (RFC 7519)

```json
{
  "sub": "ms-organization",        // Subject: quién genera el token
  "iss": "vg-microservices",       // Issuer: emisor
  "aud": "vg-ms-users",            // Audience: destinatario
  "exp": 1730745607,               // Expiration: timestamp Unix
  "iat": 1730742007,               // Issued At: timestamp creación
  "scope": "scope:users.read",     // Custom: permisos
  "service": "ms-organization",    // Custom: identificador servicio
  "environment": "production"      // Custom: entorno
}
```

### ¿Por qué estos claims?

- **sub:** Identifica el servicio que llama
- **iss/aud:** Previene uso del token en otros sistemas
- **exp:** Limita tiempo de validez (ventana de 1 hora)
- **iat:** Auditoría de cuándo se generó
- **Custom claims:** Información adicional para autorización

---

## 📝 13. ¿Cómo se vería el token JWE real?

### Estructura JWE (5 partes separadas por puntos)

```
[HEADER].[ENCRYPTED_KEY].[IV].[CIPHERTEXT].[TAG]
```

### Token JWE Real

```
eyJhbGciOiJkaXIiLCJlbmMiOiJBMjU2R0NNIiwiY3R5IjoiSldUIn0
.
.
zXkw7Q-vVPz_z3bJ
.
RqFv8Gz_hJ3K9L2mN5pQ8rS7tU0vW1xY2zA3bC4dE5fF6gH7hI8jJ9kK0lL1mM2nN3oO4pP5qQ6rR7sS8tT9uU0vV1wW2xX3yY4zA5aB6bC7cD8dE9eF0fG1gH2hI3iJ4jK5kL6lM7mN8nO9oP0pQ1qR2rS3sT4tU5uV6vW7wX8xY9yZ0zA1aB2bC3cD4dE5eF6fG7gH8hI9iJ0jK1kL2lM3mN4nO5oP6pQ7qR8rS9sT0tU1uV2vW3wX4xY5yZ6zA7aB8bC9cD0dE1eF2fG3gH4hI5iJ6jK7kL8lM9mN0nO1oP2pQ3qR4rS5sT6tU7uV8vW9wX0xY1yZ2zA3aB4bC5cD6dE7eF8fG9gH0hI1iJ2jK3kL4lM5mN6nO7oP8pQ9qR0rS1sT2tU3uV4vW5wX6xY7yZ8zA9aB0bC1cD2dE3eF4fG5gH6hI7iJ8jK9kL0lM1mN2nO3oP4pQ5qR6rS7sT8tU9uV0vW1wX2xY3yZ4zA5aB6bC7cD8dE9eF0fG1gH2hI3iJ4jK5kL6lM7mN8nO9oP0pQ1qR2rS3sT4tU5uV6vW7wX8xY9yZ
.
Hm3jN6kP9rT2wZ5aD8gJ1nQ4sV7yB0eF3hK6mP9rU2xA
```

**Nota:** Este token es **ilegible** sin la clave secreta (a diferencia de JWT).

---

## 🚀 14. ¿Cómo escalaría a más microservicios?

### Escenario: Agregar MS-BILLING

1. **Compartir el mismo secreto JWE:**

   ```yaml
   # MS-BILLING application.yml
   internal:
     security:
       jwe:
         secret: ${INTERNAL_JWE_SECRET}  # Mismo secreto
   ```

2. **Instalar Nimbus JOSE+JWT:**

   ```xml
   <dependency>
       <groupId>com.nimbusds</groupId>
       <artifactId>nimbus-jose-jwt</artifactId>
       <version>9.37.3</version>
   </dependency>
   ```

3. **Copiar JweTokenService:**
   - Generar tokens con `subject: "ms-billing"`

4. **Listo:** MS-BILLING puede llamar a MS-USERS con JWE

### Ventaja: **Reutilización de Código**

- Misma clase `JweTokenService`
- Misma configuración
- Solo cambiar `subject` por servicio

---

## 🔧 15. ¿Qué herramientas usaste?

### Librerías y Frameworks

| Herramienta | Versión | Propósito |
|-------------|---------|-----------|
| **Nimbus JOSE+JWT** | 9.37.3 | Implementación JWE/JWT |
| **Spring Boot** | 3.4.5 | Framework base |
| **Spring WebFlux** | Reactive | Programación reactiva |
| **Lombok** | Latest | Reducir boilerplate |

### ¿Por qué Nimbus JOSE+JWT?

1. ✅ Implementación completa de RFC 7516 (JWE)
2. ✅ Soporta todos los algoritmos estándar
3. ✅ Ampliamente usado en industria
4. ✅ Bien mantenido y documentado
5. ✅ Compatible con Spring Boot

**Alternativas descartadas:**

- `java-jwt` (Auth0): No soporta JWE, solo JWT
- `jjwt` (Jitpack): Soporte limitado de JWE
- Implementación manual: Propenso a errores de seguridad

---

## 📖 16. ¿Qué RFCs sigues?

### Estándares Implementados

| RFC | Título | Uso en el Proyecto |
|-----|--------|-------------------|
| **RFC 7516** | JSON Web Encryption (JWE) | Cifrado de tokens |
| **RFC 7518** | JSON Web Algorithms (JWA) | DIR + A256GCM |
| **RFC 7519** | JSON Web Token (JWT) | Estructura de claims |
| **RFC 7515** | JSON Web Signature (JWS) | (No usado, solo cifrado) |

### Cumplimiento de Estándares

✅ **100% compatible** con los RFCs de IETF
✅ Interoperable con otras implementaciones JWE
✅ No hay extensiones propietarias

---

## 🎓 17. Conceptos Clave para la Presentación

### Glosario Técnico

| Término | Definición |
|---------|-----------|
| **JWE** | JSON Web Encryption - Estándar para cifrar JSON |
| **DIR** | Direct Encryption - Usa clave simétrica directamente |
| **A256GCM** | AES-256 en modo Galois/Counter (autenticado) |
| **Secreto Compartido** | Clave simétrica conocida por ambos servicios |
| **Claims** | Atributos dentro del token (sub, iss, aud, exp) |
| **Issuer** | Emisor del token (quién lo genera) |
| **Audience** | Audiencia (para quién es el token) |
| **Subject** | Sujeto (identificador del servicio) |

---

## 📚 Referencias y Documentación

### Papers y Documentación Oficial

1. **RFC 7516 - JSON Web Encryption**
   - <https://datatracker.ietf.org/doc/html/rfc7516>

2. **Nimbus JOSE+JWT Documentation**
   - <https://connect2id.com/products/nimbus-jose-jwt>

3. **NIST SP 800-38D - GCM Specification**
   - <https://csrc.nist.gov/publications/detail/sp/800-38d/final>

4. **AES Encryption Standard (FIPS 197)**
   - <https://csrc.nist.gov/publications/detail/fips/197/final>

---

## ✅ Checklist de Conocimiento

### Antes de la Presentación, Asegúrate de Poder Explicar

- [ ] ¿Qué es JWE y en qué se diferencia de JWT?
- [ ] ¿Por qué elegiste DIR + A256GCM?
- [ ] ¿Dónde está el código de cifrado? (JweDemoService.java)
- [ ] ¿Cómo se valida el token? (InternalJweFilter.java)
- [ ] ¿Qué contiene el payload cifrado?
- [ ] ¿Por qué NO usaste RSA o curvas elípticas?
- [ ] ¿Cómo se gestiona el secreto compartido?
- [ ] ¿Qué protecciones de seguridad implementa?
- [ ] ¿Cuál es el overhead de rendimiento?
- [ ] ¿Cómo escalarías a más microservicios?

---

## 🎯 Respuesta Rápida para el Profesor

### Si te preguntan: "¿Por qué JWE y no JWT?"

**Respuesta corta:**
> "JWT solo firma el payload (HMAC-SHA256), por lo que cualquiera puede ver su contenido en Base64. JWE cifra todo el payload con AES-256-GCM, garantizando confidencialidad + integridad. Como comunicamos datos sensibles (roles, organizationId) entre microservicios internos, necesitamos que el contenido sea completamente privado, no solo verificable."

### Si te preguntan: "¿Por qué DIR y no RSA?"

**Respuesta corta:**
> "RSA requiere gestión de certificados PKI, es 10x más lento, y genera tokens más grandes. Como estamos en comunicación interna entre servicios de confianza, DIR con clave simétrica compartida es más eficiente, simple de mantener, y igual de seguro (AES-256 es estándar militar). RSA es mejor para comunicación pública donde no puedes compartir secretos."

---

¡Con esto deberías estar preparado para cualquier pregunta técnica! 🚀
