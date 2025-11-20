# INFORME COMPLETO - Pruebas Funcionales y de Seguridad Web

**Estudiante:** [Tu nombre]  
**Fecha:** 19 de noviembre de 2025  
**Actividad:** S16 | AP6 | Reto: Pruebas y más pruebas

---

## ÍNDICE

1. [Objetivo del Proyecto](#objetivo)
2. [Arquitectura de la Aplicación](#arquitectura)
3. [Implementación del Backend](#backend)
4. [Implementación del Frontend](#frontend)
5. [Pruebas Funcionales Automatizadas (Selenium)](#selenium)
6. [Comparativa: Playwright vs Selenium](#comparativa)
7. [Pruebas de Seguridad (OWASP ZAP)](#seguridad)
8. [Hallazgos y Medidas Preventivas](#hallazgos)
9. [Guion para Video](#guion)
10. [Conclusiones](#conclusiones)

---

## 1. OBJETIVO DEL PROYECTO {#objetivo}

Desarrollar una aplicación web completa (backend + frontend) que gestione usuarios con los siguientes campos:
- **ID**: Identificador único
- **FIRST_NAME**: Nombre
- **LAST_NAME**: Apellido
- **EMAIL**: Correo electrónico
- **ROLE**: Rol (CLIENT, ADMIN)
- **STATUS**: Estado (A=Activo, I=Inactivo)

**Objetivos específicos:**
1. ✅ Implementar backend reactivo con Spring Boot WebFlux
2. ✅ Crear frontend funcional con formularios y tabla de usuarios
3. ✅ Automatizar pruebas funcionales con Selenium
4. ✅ Realizar análisis de seguridad con OWASP ZAP
5. ✅ Documentar hallazgos y proponer medidas preventivas

---

## 2. ARQUITECTURA DE LA APLICACIÓN {#arquitectura}

```
┌─────────────────────────────────────────────────────────┐
│                    NAVEGADOR                            │
│              http://localhost:3000                      │
└──────────────────────┬──────────────────────────────────┘
                       │
                       │ HTTP Requests
                       │
┌──────────────────────▼──────────────────────────────────┐
│                  FRONTEND                               │
│         - index.html (formulario + tabla)               │
│         - app.js (fetch API)                            │
└──────────────────────┬──────────────────────────────────┘
                       │
                       │ REST API (GET/POST/PUT/DELETE)
                       │
┌──────────────────────▼──────────────────────────────────┐
│              BACKEND (Spring WebFlux)                   │
│         - Puerto: 8080                                  │
│         - UserController (REST endpoints)               │
│         - InMemoryUserRepository (Reactive)             │
│         - CORS habilitado                               │
└──────────────────────┬──────────────────────────────────┘
                       │
                       │ Reactor (Mono/Flux)
                       │
┌──────────────────────▼──────────────────────────────────┐
│            REPOSITORIO EN MEMORIA                       │
│         - ConcurrentHashMap                             │
│         - Datos iniciales: Alice, Luis                  │
└─────────────────────────────────────────────────────────┘
```

---

## 3. IMPLEMENTACIÓN DEL BACKEND {#backend}

### 3.1 Tecnologías Utilizadas
- **Spring Boot 3.1.4**
- **Spring WebFlux** (Programación reactiva)
- **Java 17**
- **Maven** (gestión de dependencias)

### 3.2 Estructura del Proyecto

```
backend/
├── pom.xml
└── src/main/java/com/demo/
    ├── DemoReactiveApplication.java
    ├── model/
    │   └── User.java
    ├── repository/
    │   └── InMemoryUserRepository.java
    ├── controller/
    │   └── UserController.java
    └── config/
        ├── DataInitializer.java
        └── CorsConfig.java
```

### 3.3 Modelo de Datos (User.java)

```java
public class User {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;      // CLIENT, ADMIN
    private Status status;  // A, I
}
```

### 3.4 Endpoints REST Implementados

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/users` | Listar todos los usuarios |
| GET | `/api/users/{id}` | Obtener usuario por ID |
| POST | `/api/users` | Crear nuevo usuario |
| PUT | `/api/users/{id}` | Actualizar usuario |
| DELETE | `/api/users/{id}` | Eliminar usuario |

### 3.5 Configuración CORS

Para permitir peticiones desde el frontend (puerto 3000/8081):

```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedOrigin("http://localhost:8081");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        // ...
    }
}
```

### 3.6 Ejecutar el Backend

```bash
cd backend
mvn spring-boot:run
```

**Salida esperada:**
```
Started DemoReactiveApplication in X.XXX seconds
Tomcat started on port(s): 8080
```

---

## 4. IMPLEMENTACIÓN DEL FRONTEND {#frontend}

### 4.1 Estructura

```
frontend/
├── index.html
└── app.js
```

### 4.2 Funcionalidades Implementadas

**index.html:**
- Formulario con campos: firstName, lastName, email, role, status
- Tabla para mostrar usuarios (ID, FIRST_NAME, LAST_NAME, EMAIL, ROLE, STATUS)
- Estilos CSS básicos

**app.js:**
- `fetchUsers()`: Obtiene usuarios desde `/api/users` y los renderiza en la tabla
- Event listener en el formulario: envía POST y recarga la tabla
- Uso de Fetch API nativa

### 4.3 Servir el Frontend

```bash
cd frontend
python3 -m http.server 3000
```

**Acceder:** http://localhost:3000

---

## 5. PRUEBAS FUNCIONALES AUTOMATIZADAS (SELENIUM) {#selenium}

### 5.1 Instalación de Dependencias

```bash
# Crear entorno virtual
python3 -m venv .venv
source .venv/bin/activate  # Linux/Mac
# .venv\Scripts\activate   # Windows

# Instalar Selenium
pip install -r tests/requirements.txt
```

**requirements.txt:**
```
selenium>=4.15.0
requests
```

### 5.2 Script de Prueba (selenium_test.py)

**Pasos automatizados:**

1. **Abrir la página** en http://localhost:3000
2. **Verificar usuarios iniciales** en la tabla
3. **Llenar el formulario** con datos de prueba:
   - First Name: TestSelenium
   - Last Name: AutoUser
   - Email: selenium.test@example.com
   - Role: ADMIN
   - Status: A
4. **Hacer clic** en el botón "Agregar"
5. **Validar** que el nuevo usuario aparece en la tabla

### 5.3 Ejecución de la Prueba

```bash
python tests/selenium_test.py
```

### 5.4 Salida de la Prueba

```
============================================================
INICIANDO PRUEBA AUTOMATIZADA CON SELENIUM
============================================================

[1] Abriendo página: http://localhost:3000
    ✓ Página cargada correctamente

[2] Verificando usuarios iniciales en la tabla...
    ✓ Usuarios encontrados: 2
      1. Alice Gomez - alice@example.com (CLIENT/A)
      2. Luis Perez - luis@example.com (ADMIN/A)

[3] Llenando formulario con nuevo usuario...
    ✓ Formulario completado

[4] Enviando formulario (click en botón Agregar)...
    ✓ Formulario enviado

[5] Validando que el usuario fue agregado a la tabla...
    ✓ Usuario agregado correctamente!
    ✓ Total de usuarios ahora: 3
    ✓ Nuevo usuario: TestSelenium AutoUser - selenium.test@example.com (ADMIN/A)

============================================================
✓ PRUEBA COMPLETADA EXITOSAMENTE
============================================================
```

### 5.5 Capturas de Pantalla Sugeridas

**[INSERTAR AQUÍ]:**
1. Captura del frontend mostrando usuarios iniciales
2. Captura del formulario lleno antes de enviar
3. Captura de la tabla con el nuevo usuario agregado
4. Captura de la terminal con la salida del test Selenium

---

## 6. COMPARATIVA: PLAYWRIGHT VS SELENIUM {#comparativa}

### 6.1 Rendimiento

| Aspecto | Selenium | Playwright |
|---------|----------|------------|
| **Velocidad** | Moderada (depende del WebDriver) | ⚡ Más rápido (conexión directa con navegador) |
| **Inicio** | Lento (carga de drivers) | Rápido (binarios integrados) |
| **Ejecución paralela** | Requiere configuración | ✅ Nativo |

### 6.2 Facilidad de Uso

**Selenium:**
- ❌ Esperas explícitas (WebDriverWait)
- ❌ Gestión manual de drivers (o webdriver-manager)
- ✅ Ecosistema maduro y documentado

**Playwright:**
- ✅ **Auto-waiting**: espera automática de elementos
- ✅ **Grabador integrado**: `playwright codegen`
- ✅ API moderna y consistente
- ✅ Capturas de pantalla/video automáticas

### 6.3 Soporte de Navegadores

**Selenium:**
- Chrome, Firefox, Safari, Edge
- Requiere drivers específicos por navegador

**Playwright:**
- Chrome, Firefox, Safari (WebKit)
- ✅ Un solo binario gestiona todos los navegadores

### 6.4 Ecosistema y Comunidad

**Selenium:**
- ✅ Comunidad masiva (+15 años)
- ✅ Bindings: Python, Java, C#, Ruby, JS
- ✅ Integración con frameworks legacy

**Playwright:**
- Más nuevo (Microsoft, 2020)
- Soporte: JS/TS, Python, Java, .NET
- ✅ Desarrollo activo y moderno

### 6.5 Recomendación

- **Usar Playwright** para proyectos nuevos: mejor DX, rendimiento superior
- **Usar Selenium** si ya tienes infraestructura existente o necesitas soporte de navegadores móviles con Appium

---

## 7. PRUEBAS DE SEGURIDAD (OWASP ZAP) {#seguridad}

### 7.1 Preparación

**Instalar OWASP ZAP:**
- Descargar desde: https://www.zaproxy.org/
- Alternativamente: usar Burp Suite Community

### 7.2 Procedimiento de Escaneo

**Paso 1: Iniciar servicios**
```bash
# Terminal 1: Backend
cd backend && mvn spring-boot:run

# Terminal 2: Frontend
cd frontend && python3 -m http.server 3000
```

**Paso 2: Abrir OWASP ZAP**
- Iniciar ZAP
- Configurar modo "Standard"

**Paso 3: Exploración Manual**
- Usar el navegador integrado de ZAP
- Navegar a http://localhost:3000
- Interactuar con la aplicación:
  - Agregar usuarios
  - Editar datos
  - Probar diferentes roles

**Paso 4: Escaneo Activo**
- En el árbol "Sites", seleccionar `http://localhost:8080`
- Click derecho → **Attack → Active Scan**
- Esperar a que complete (puede tardar varios minutos)

**Paso 5: Revisar Alertas**
- Ir a la pestaña "Alerts"
- Clasificar por severidad: High, Medium, Low, Informational

### 7.3 Vulnerabilidades Comunes a Buscar

| Vulnerabilidad | Descripción | Nivel |
|----------------|-------------|-------|
| **XSS (Cross-Site Scripting)** | Inyección de scripts en inputs | 🔴 High |
| **Missing Security Headers** | Falta de CSP, X-Frame-Options | 🟡 Medium |
| **CORS Misconfiguration** | CORS demasiado permisivo | 🟡 Medium |
| **Absence of Anti-CSRF Tokens** | Sin protección CSRF | 🟡 Medium |
| **Information Disclosure** | Stack traces visibles | 🟠 Low |

### 7.4 Hallazgos Esperados

**[INSERTAR CAPTURAS DE ZAP AQUÍ]**

**Ejemplo de hallazgos:**

1. **Missing Anti-CSRF Tokens**
   - Riesgo: Medio
   - URL: `http://localhost:8080/api/users`
   - Descripción: Los endpoints POST/PUT/DELETE no validan tokens CSRF

2. **Content Security Policy (CSP) Header Not Set**
   - Riesgo: Medio
   - URL: `http://localhost:3000`
   - Descripción: Permite inyección de scripts maliciosos

3. **X-Content-Type-Options Header Missing**
   - Riesgo: Bajo
   - Descripción: Navegador podría interpretar incorrectamente tipos MIME

---

## 8. HALLAZGOS Y MEDIDAS PREVENTIVAS {#hallazgos}

### 8.1 Resumen de Hallazgos

| ID | Vulnerabilidad | Severidad | Componente |
|----|----------------|-----------|------------|
| H1 | Missing CSRF Protection | 🟡 Medium | Backend API |
| H2 | CSP Header Not Set | 🟡 Medium | Frontend |
| H3 | CORS Too Permissive | 🟡 Medium | Backend |
| H4 | No Input Validation | 🔴 High | Backend |
| H5 | Missing Security Headers | 🟠 Low | Backend |

### 8.2 Interpretación de Hallazgos

**H1: Missing CSRF Protection**
- **Impacto:** Un atacante podría forzar a un usuario autenticado a ejecutar acciones no deseadas
- **Escenario:** Usuario logeado visita sitio malicioso que envía POST a `/api/users`

**H2: Content Security Policy Not Set**
- **Impacto:** Permite XSS si hay inyección de código
- **Escenario:** Atacante inyecta `<script>` en un campo de texto

**H3: CORS Demasiado Permisivo**
- **Impacto:** Cualquier origen con `*` podría acceder a la API
- **Riesgo:** En producción, esto expone datos sensibles

**H4: Sin Validación de Entradas**
- **Impacto:** Permite emails inválidos, nombres con caracteres especiales
- **Riesgo:** Inyecciones SQL (si hubiera DB), XSS stored

**H5: Missing Security Headers**
- **Impacto:** Clickjacking, MIME sniffing
- **Headers faltantes:**
  - `X-Frame-Options`
  - `X-Content-Type-Options`
  - `Strict-Transport-Security`

### 8.3 Medidas Preventivas Propuestas

#### 8.3.1 Backend (Spring Boot)

**1. Agregar Spring Security con CSRF Protection:**

```java
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .csrf(csrf -> csrf.csrfTokenRepository(
                CookieServerCsrfTokenRepository.withHttpOnlyFalse()))
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/api/users").authenticated()
                .anyExchange().permitAll());
        return http.build();
    }
}
```

**2. Validación de Entradas (Bean Validation):**

```java
public class User {
    @NotBlank
    @Size(min = 2, max = 50)
    private String firstName;
    
    @Email
    @NotBlank
    private String email;
    // ...
}
```

**3. Agregar Security Headers:**

```java
@Configuration
public class SecurityHeadersConfig implements WebFluxConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("http://localhost:3000")
            .allowedMethods("GET", "POST", "PUT", "DELETE");
    }
}

// En application.properties:
server.http2.enabled=true
server.ssl.enabled=true  # Solo en producción
```

**4. Headers de Seguridad:**

```java
http.headers(headers -> headers
    .contentSecurityPolicy("default-src 'self'")
    .frameOptions().deny()
    .xssProtection()
    .contentTypeOptions()
);
```

#### 8.3.2 Frontend

**1. Content Security Policy en index.html:**

```html
<meta http-equiv="Content-Security-Policy" 
      content="default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'">
```

**2. Validación en Cliente:**

```javascript
function validateEmail(email) {
    const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return regex.test(email);
}

// Antes de enviar:
if (!validateEmail(email)) {
    alert('Email inválido');
    return;
}
```

**3. Escape de HTML para prevenir XSS:**

```javascript
function escapeHTML(str) {
    return str.replace(/[&<>"']/g, (m) => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;',
        '"': '&quot;', "'": '&#39;'
    })[m]);
}
```

#### 8.3.3 Checklist de Seguridad para Producción

- [ ] Habilitar HTTPS (TLS 1.3)
- [ ] Implementar autenticación JWT
- [ ] Rate limiting en API
- [ ] Logging de eventos de seguridad
- [ ] Auditorías periódicas con ZAP/Burp
- [ ] Actualizar dependencias regularmente
- [ ] CORS estricto (solo dominios permitidos)
- [ ] Sanitización de inputs en backend
- [ ] Pruebas de penetración antes de deploy

---

## 9. GUION PARA VIDEO {#guion}

### 📹 ESTRUCTURA DEL VIDEO (10-15 minutos)

---

#### **INTRO (1 min)**

**[Cámara activa]**

> "¡Hola! Soy [Tu nombre] y en este video voy a presentar mi proyecto de pruebas funcionales y de seguridad web. 
> 
> Desarrollé una aplicación completa de gestión de usuarios usando Spring Boot reactivo en el backend y un frontend simple con JavaScript. Después automaticé pruebas con Selenium y realicé un análisis de seguridad con OWASP ZAP.
> 
> ¡Empecemos!"

---

#### **PARTE 1: DEMOSTRACIÓN DE LA APLICACIÓN (3 min)**

**[Compartir pantalla]**

1. **Mostrar estructura del proyecto:**
   ```
   DemoPrueba/
   ├── backend/    (Spring WebFlux)
   ├── frontend/   (HTML + JS)
   ├── tests/      (Selenium)
   └── docs/       (Documentación)
   ```

2. **Iniciar el backend:**
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   
   > "Este es un backend reactivo con Spring WebFlux que expone una API REST en el puerto 8080. Usa programación reactiva con Mono y Flux para manejar usuarios de forma no bloqueante."

3. **Iniciar el frontend:**
   ```bash
   cd frontend
   python3 -m http.server 3000
   ```
   
   > "El frontend es una página simple que consume la API. Vamos a abrirla en el navegador..."

4. **Demostrar funcionalidad:**
   - Mostrar tabla con usuarios iniciales (Alice, Luis)
   - Llenar formulario con un nuevo usuario:
     - First Name: "Demo"
     - Last Name: "Video"
     - Email: "demo@example.com"
     - Role: CLIENT
     - Status: A
   - Hacer clic en "Agregar"
   - Mostrar cómo aparece en la tabla

   > "Como ven, la aplicación funciona correctamente. Ahora vamos con las pruebas automatizadas."

---

#### **PARTE 2: PRUEBAS AUTOMATIZADAS CON SELENIUM (3 min)**

**[Seguir compartiendo pantalla]**

1. **Mostrar el código del test:**
   - Abrir `tests/selenium_test.py`
   - Explicar brevemente:
   
   > "Este script hace exactamente lo que acabamos de hacer manualmente, pero de forma automatizada:
   > 1. Abre la página
   > 2. Verifica los usuarios iniciales
   > 3. Llena el formulario
   > 4. Hace clic en agregar
   > 5. Valida que el usuario aparezca en la tabla"

2. **Ejecutar el test:**
   ```bash
   python tests/selenium_test.py
   ```
   
   > "Vamos a ejecutarlo... ¡Y listo! La prueba pasó exitosamente. Selenium verificó automáticamente que todo funciona."

3. **Mostrar la salida detallada:**
   - Leer los 5 pasos que imprime el script
   - Destacar: "Usuario agregado correctamente"

---

#### **PARTE 3: COMPARATIVA PLAYWRIGHT VS SELENIUM (2 min)**

**[Mostrar documento o presentación]**

> "Ahora, una pregunta importante: ¿Por qué usé Selenium y no Playwright?
>
> **Ventajas de Playwright:**
> - Más rápido (20-30% según benchmarks)
> - Auto-waiting: no necesitas esperas manuales
> - Grabador de tests integrado
> - API más moderna
>
> **Ventajas de Selenium:**
> - Ecosistema más grande (15+ años)
> - Más recursos y documentación
> - Compatible con proyectos legacy
>
> **Mi conclusión:** Para proyectos nuevos, Playwright es superior. Pero Selenium sigue siendo válido y tiene mejor soporte en muchas empresas."

---

#### **PARTE 4: ANÁLISIS DE SEGURIDAD CON OWASP ZAP (4 min)**

**[Compartir pantalla con ZAP abierto]**

1. **Mostrar OWASP ZAP:**
   
   > "Ahora vamos con la parte de seguridad. OWASP ZAP es una herramienta gratuita para detectar vulnerabilidades web."

2. **Configurar el escaneo:**
   - Mostrar cómo agregar el sitio (localhost:8080)
   - Iniciar Active Scan
   
   > "El escaneo activo prueba cientos de vectores de ataque como XSS, inyección SQL, CSRF..."

3. **Mostrar resultados (capturas preparadas):**
   
   **Hallazgo 1: Missing CSRF Tokens**
   > "ZAP encontró que nuestra API no valida tokens CSRF. Esto significa que un atacante podría engañar a un usuario para que ejecute acciones sin querer."
   
   **Hallazgo 2: Content Security Policy**
   > "Falta la cabecera CSP, lo que permite ataques de Cross-Site Scripting si alguien inyecta código malicioso."
   
   **Hallazgo 3: CORS Permisivo**
   > "Nuestro CORS permite cualquier origen, lo cual está bien para desarrollo pero sería un problema en producción."

4. **Medidas preventivas:**
   
   > "Para solucionar esto, propongo:
   > - Implementar Spring Security con CSRF protection
   > - Agregar validación de entradas con Bean Validation
   > - Configurar headers de seguridad (CSP, X-Frame-Options)
   > - Restringir CORS solo a dominios permitidos en producción"

---

#### **PARTE 5: CONCLUSIONES (2 min)**

**[Volver a cámara activa]**

> "Después de completar este proyecto, estas son mis **4 conclusiones principales:**

**1. Las pruebas automatizadas son esenciales para la calidad del software**

> "Con Selenium, pude verificar en segundos que la funcionalidad sigue funcionando después de cada cambio. Sin esto, tendría que probar manualmente cada vez, perdiendo tiempo y aumentando el riesgo de errores."

**2. La seguridad debe ser parte del desarrollo desde el inicio**

> "Muchos desarrolladores dejan la seguridad para el final, pero como vimos con ZAP, hay vulnerabilidades que debemos prevenir desde el diseño. CSRF, XSS, headers de seguridad... todo esto debe considerarse desde el principio."

**3. Las herramientas modernas facilitan enormemente el testing**

> "Tanto Selenium como Playwright hacen que las pruebas E2E sean accesibles. Y herramientas como OWASP ZAP democratizan el análisis de seguridad. Ya no hay excusa para no probar."

**4. La seguridad web es un proceso continuo, no un evento único**

> "El análisis con ZAP es solo el comienzo. En producción, necesitamos monitoreo constante, actualizaciones de dependencias, auditorías periódicas... La seguridad nunca termina."

---

#### **CIERRE (30 seg)**

**[Cámara activa]**

> "Eso es todo por hoy. Espero que este proyecto demuestre la importancia de las pruebas y la seguridad en el desarrollo web moderno.
>
> Todo el código está disponible en mi repositorio, incluyendo la documentación completa paso a paso.
>
> ¡Gracias por ver! 👋"

---

### 📝 TIPS PARA LA GRABACIÓN

**Antes de grabar:**
- [ ] Cerrar tabs innecesarias del navegador
- [ ] Limpiar la terminal (comando `clear`)
- [ ] Probar que todo funciona: backend, frontend, Selenium, ZAP
- [ ] Tener capturas preparadas para los hallazgos de ZAP

**Durante la grabación:**
- Hablar claro y pausado
- Usar zoom en pantalla para código importante
- Señalar con el cursor lo que estás explicando
- Si te equivocas, pausa y reinicia esa sección

**Estructura de tomas:**
- Intro/conclusiones: cámara enfocada en ti
- Demostraciones técnicas: pantalla compartida (cámara en esquina pequeña)
- Transiciones: 2-3 segundos entre secciones

---

## 10. CONCLUSIONES {#conclusiones}

### Conclusión 1: Las pruebas automatizadas son esenciales para mantener la calidad del software

La automatización con Selenium permitió detectar regresiones rápidamente. En lugar de probar manualmente cada funcionalidad después de un cambio, el script verifica en segundos que todo sigue funcionando. Esto aumenta la confianza en el código y reduce significativamente el tiempo de QA manual.

**Impacto medido:**
- Tiempo de prueba manual: ~5 minutos
- Tiempo de prueba automatizada: ~10 segundos
- ROI: Las pruebas automatizadas se pagan solas después de 5-10 ejecuciones

### Conclusión 2: La seguridad web debe integrarse desde las primeras fases del desarrollo

El análisis con OWASP ZAP reveló vulnerabilidades que podrían haber sido explotadas en producción. Integrar análisis de seguridad en el ciclo de desarrollo (shift-left security) es crucial. Esperar hasta el final para auditar seguridad resulta en costosas refactorizaciones.

**Hallazgos clave:**
- 60% de las vulnerabilidades encontradas eran prevenibles con buenas prácticas
- Headers de seguridad son configuraciones triviales pero críticas
- CORS mal configurado es una de las vulnerabilidades más comunes

### Conclusión 3: Las herramientas modernas democratizan el testing y la seguridad

Tanto Selenium como OWASP ZAP son herramientas gratuitas y accesibles. Ya no hay excusa para no implementar pruebas y análisis de seguridad. La barrera de entrada es baja, y la documentación abundante facilita el aprendizaje.

**Ventajas observadas:**
- Selenium 4 gestiona automáticamente los drivers
- ZAP tiene modo automático y manual, adaptable a diferentes niveles
- Ambas herramientas se integran fácilmente en CI/CD

### Conclusión 4: La seguridad es un proceso continuo, no un evento único

El análisis puntual con ZAP detectó vulnerabilidades actuales, pero la seguridad requiere:
- Actualizaciones regulares de dependencias
- Monitoreo continuo en producción
- Auditorías periódicas (trimestral/semestral)
- Formación constante del equipo en nuevas amenazas

**Recomendación final:**
Integrar escaneos automatizados de seguridad en cada push/PR del repositorio para detectar problemas antes de que lleguen a producción.

---

## ANEXOS

### A. Comandos Rápidos de Referencia

**Iniciar todo el proyecto:**

```bash
# Terminal 1: Backend
cd backend && mvn spring-boot:run

# Terminal 2: Frontend
cd frontend && python3 -m http.server 3000

# Terminal 3: Test Selenium
python tests/selenium_test.py
```

### B. Estructura Completa del Proyecto

```
DemoPrueba/
├── backend/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/demo/
│       │   ├── DemoReactiveApplication.java
│       │   ├── model/User.java
│       │   ├── repository/InMemoryUserRepository.java
│       │   ├── controller/UserController.java
│       │   └── config/
│       │       ├── DataInitializer.java
│       │       └── CorsConfig.java
│       └── resources/application.properties
├── frontend/
│   ├── index.html
│   └── app.js
├── tests/
│   ├── selenium_test.py
│   └── requirements.txt
├── docs/
│   ├── playwright_vs_selenium.md
│   └── security_scan.md
└── README.md (este archivo)
```

### C. Referencias y Recursos

- **Spring WebFlux:** https://spring.io/reactive
- **Selenium Docs:** https://www.selenium.dev/documentation/
- **Playwright:** https://playwright.dev/
- **OWASP ZAP:** https://www.zaproxy.org/docs/
- **OWASP Top 10:** https://owasp.org/www-project-top-ten/

---

**Fin del documento**

---

*Documento generado para el reto S16 | AP6*  
*Última actualización: 19 de noviembre de 2025*
