# 🎬 GUIÓN DE VOZ — VIDEO DE SEGURIDAD SIGEI
> **VERSIÓN ~10 MINUTOS (Sin Demo Postman)**
> Léelo tal cual. Está escrito en primera persona y dirigiéndose directamente al profesor ("usted").

---

## PARTE 1 — INTRODUCCIÓN Y ARQUITECTURA (1.5 min)

---

> Ante todo un cordial saludo profesor, En este video voy a explicar cómo hemos securizado los microservicios del sistema SIGEI usando Spring Security, OAuth2, JWT y Keycloak
Yo voy a encargarme de explicar el código y mostrarles exactamente dónde está cada configuración.
Para que quede claro desde el principio: la seguridad en nuestro sistema no está en un solo microservicio, está distribuida. Cada microservicio tiene su propia capa de seguridad, y todos trabajan juntos. Vamos a ver eso paso a paso.

>
> La seguridad en mi sistema no está en un solo punto, está distribuida en varias capas. Funciona así:
>
>
> A continuación, le mostraré el código clave de esta arquitectura paso a paso."

# Enseñas el readme bubu en esta seccion:

> Cuando un usuario quiere entrar al sistema, lo primero que ve es el Gateway. El Gateway es como la recepción de un edificio  si no tienes tu credencial, no pasas.
> La credencial en nuestro caso es el JWT, que es básicamente un token, un código cifrado que prueba que eres quien dices ser y que tienes ciertos permisos.
> Ahora bien, ¿cómo obtienes ese JWT? Yendo al microservicio de autenticación: vg-ms-authentication. Ahí es donde haces el login, y Keycloak  que es nuestro servidor de identidades  te da ese token.
> Una vez que tienes el token, cada petición que hagas lo lleva en el header. El Gateway lo valida, y si está bien, deja pasar la petición hacia el microservicio correspondiente.
> Cada microservicio  incluyendo los tres que son parte de este trabajo: vg-ms-users-management, vg-ms-teacher-assignment y vg-ms-students  también valida el token y revisa si el usuario tiene el rol necesario para hacer lo que está pidiendo.

---

## PARTE 2 — EL GATEWAY: LA PRIMERA BARRERA (2.5 min)
*(Abrir el microservicio vg-ms-gateway)*
### 📂 Abrir: `config/SecurityConfig.java` (del gateway)

---

> "Empiezo con el Gateway, la puerta de entrada de todo nuestro ecosistema.
>
> En el archivo `SecurityConfig.java`, tengo configurado que **absolutamente todas las rutas requieren autenticación**, a excepción de `/api/auth/**` (que es para hacer login) y las de monitoreo (`/actuator`).
> Si una petición no trae token, el Gateway la rechaza inmediatamente con un 401 Unauthorized. Es la primera gran barrera."

---

### 📂 Abrir: `config/JwtClaimsForwardFilter.java`

---

> "Pero lo que hace al Gateway realmente eficiente está en este archivo: `JwtClaimsForwardFilter.java`.
>
> Una vez que el Gateway comprueba que el JWT es válido, este filtro extrae la información más importante que viene dentro del token (como el ID del usuario, su rol y su institución).
> Luego, toma esos datos y los reenvía como **headers internos** (`X-User-Id`, `X-User-Role`, `X-Institution-Id`) hacia los demás microservicios.
>
> Gracias a esto, los microservicios de negocio ya no tienen que gastar recursos decodificando el JWT otra vez; simplemente leen estos headers y ya saben exactamente quién hace la petición. Es un enfoque mucho más limpio y seguro."

---

## PARTE 3 — AUTENTICACIÓN: OBTENIENDO EL TOKEN (3 min)
*(Abrir el microservicio vg-ms-authentication)*
### 📂 Abrir: `infrastructure/adapters/in/rest/AuthRest.java`

---

> "Ahora paso al microservicio de autenticación, que es el encargado exclusivo de generar y renovar los tokens.
>
> En el controlador `AuthRest.java`, tengo expuestos los endpoints públicos como `/api/auth/login`. Como le mencioné, son las únicas rutas libres porque, por lógica, un usuario necesita poder hacer login cuando aún no tiene su token."

---

### 📂 Abrir: `infrastructure/adapters/out/external/KeycloakClientImpl.java`

---

> "Cuando se llama al login, este archivo `KeycloakClientImpl.java` entra en acción.
>
> Literalmente se encarga de hablar con nuestro servidor de identidades, **Keycloak**, usando el cliente `WebClient` de Spring. Utiliza el flujo `password` de OAuth2: le manda a Keycloak el usuario y la contraseña, y si todo está correcto, Keycloak nos devuelve un `access_token` seguro y cifrado.
>
> Además, este archivo mantiene todo sincronizado. Si yo creo o desactivo un usuario dentro del sistema, automáticamente hace la llamada a la Admin API de Keycloak para reflejar ese cambio allá. La seguridad va siempre de la mano en ambas partes."

---

### 📂 Abrir: `infrastructure/config/KeycloakMapperInitializer.java`

---

> "Y antes de salir de este microservicio, le muestro el `KeycloakMapperInitializer.java`.
>
> Cuando el microservicio arranca, este archivo configura automáticamente a Keycloak para que los tokens JWT incluyan nuestros propios 'claims' o datos extra (como el `userId` y el `institutionId` de nuestro sistema). Por eso el Gateway, que vimos antes, puede leerlos y mandarlos en los headers. Todo encaja perfectamente."

---

## PARTE 4 — MICROSERVICIOS DE NEGOCIO: CONTROL DE ROLES (3 min)
*(Abrir el microservicio vg-ms-users-management)*
### 📂 Abrir: `infrastructure/config/SecurityConfig.java` (de users)

---

> "Finalmente, veamos cómo se protegen los microservicios individuales a nivel de roles. Le muestro primero el de gestión de usuarios (`vg-ms-users-management`).
>
> En su `SecurityConfig.java`, separo quién puede hacer qué:"

*(Señalar los roles en pantalla)*

> "Solo roles gerenciales como ADMINISTRADOR o DIRECTOR pueden modificar datos (hacer POST, PUT, DELETE). Sin embargo, un DOCENTE o AUXILIAR tiene acceso, pero solo de lectura (hacer GET).
>
> Toda esta magia de validación de roles funciona gracias a mi clase `KeycloakRoleConverter`, que lee el rol exacto del JWT y se lo pasa a Spring Security con el prefijo `ROLE_`."

---
*(Abrir el microservicio vg-ms-teacher-assignment)*
### 📂 Abrir: `infrastructure/config/SecurityConfig.java` (de teacher-assignment)

---

> "Y esta seguridad a nivel de métodos y rutas la apliqué a todos mis microservicios.
>
> Por ejemplo, aquí en **asignación de docentes**, configuré una ruta especial: `/api/teacher-assignments/teacher/**`. A esta ruta solo entra el DOCENTE, lo que significa que un profesor únicamente puede ver sus *propias* asignaciones, sin poder curiosear o modificar las asignaciones de sus compañeros."

---
*(Abrir el microservicio vg-ms-students)*
### 📂 Abrir: `infrastructure/config/SecurityConfig.java` (de students)

---

> "Y para terminar, en el microservicio de **estudiantes**, apliqué la misma lógica para los padres de familia.
>
> Creé la ruta exclusiva `/api/students/my-children/**`. Un padre de familia o apoderado solo tiene permiso para entrar aquí, lo que garantiza que solo pueda consultar la información de sus propios hijos, protegiendo así la privacidad del resto de alumnos.
>
> Con esto le demuestro que la seguridad no solo restringe el acceso al sistema, sino que aísla los datos internamente según quién los esté pidiendo.
>
> Y con eso concluyo mi explicación, profesor. Muchas gracias por su atención."

---

## ⏱️ TIEMPOS SUGERIDOS

| Parte | Tiempo aprox. |
|---|---|
| 1. Introducción y Arquitectura | 1.5 min |
| 2. Gateway | 2.5 min |
| 3. Autenticación (Login) | 3 min |
| 4. Microservicios (Roles en Users, Teachers, Students) | 3 min |
| **TOTAL** | **~10 min** |

---

## 📂 ORDEN DE ARCHIVOS A ABRIR EN EL VIDEO

1. `vg-ms-gateway` → `SecurityConfig.java`
2. `vg-ms-gateway` → `JwtClaimsForwardFilter.java`
3. `vg-ms-authentication` → `AuthRest.java`
4. `vg-ms-authentication` → `KeycloakClientImpl.java`
5. `vg-ms-authentication` → `KeycloakMapperInitializer.java`
6. `vg-ms-users-management` → `SecurityConfig.java`
7. `vg-ms-teacher-assignment` → `SecurityConfig.java`
8. `vg-ms-students` → `SecurityConfig.java`
