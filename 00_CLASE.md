# GUION COMPLETO - CLASE DE HOY: INTRODUCCION A DOCKER

## Curso: Fundamentos de Redes Inalambricas

## Profesor: Prof. Isael Fatama

## Duracion: 60 minutos

**Lee este archivo palabra x palabra en tu clase. Es tu libreto completo para hoy.**

---

## [00:00 - INICIO DE LA CLASE]

*(Muestra la Diapositiva 1: Portada)*

Muy buenos días a todos. Bienvenidos a una nueva clase del curso de Fundamentos de Redes Inalámbricas.

Hoy vamos a hacer un pequeño paréntesis en las ondas de radio y las antenas, para hablar de la tecnología que hace posible que las redes modernas, los controladores de red y las aplicaciones en la nube funcionen hoy en día.

Hoy vamos a hablar de Docker y los Contenedores.

Si alguna vez se han preguntado cómo Netflix, Google, o cómo las herramientas de monitoreo de redes que usamos en el laboratorio logran ejecutarse sin fallar en miles de servidores al mismo tiempo, la respuesta está en lo que vamos a aprender hoy.

Presten mucha atención, porque esta es una de las habilidades más demandadas en el mercado laboral de TI en este momento.

---

## [00:05 - EL PROBLEMA REAL]

*(Pasar a la Diapositiva 2: El Problema Real)*

Antes de hablar de la solución, hablemos del problema. Quiero que levante la mano el que alguna vez haya programado o configurado algo, y cuando se lo pasó a un compañero, o cuando me lo entregaron a mí para calificarlo, la pantalla les arrojó un error.

*(Hacer una pausa para que los alumnos se rían o asientan)*

Y la frase típica de ustedes es: "¡Pero profe, en mi máquina sí funcionaba!"

Este es el clásico error de las dependencias locales. Y ocurre todo el tiempo en el mundo profesional.

Un desarrollador programa una aplicación usando, digamos, Python versión 3.9 y una librería de red específica. Luego se lo pasa al equipo de operaciones para ponerlo en el servidor principal, pero el servidor tiene Python 3.10. ¿El resultado? Todo se rompe.

Tenemos conflictos de versiones, librerías incompatibles, y unas diferencias enormes entre los entornos de Desarrollo —su computadora—, QA —donde se prueba— y Producción —el servidor final—.

Durante décadas, los desarrolladores y los administradores de sistemas se peleaban por esto. El desarrollador decía: "Mi código está bien, es tu servidor." Y el administrador decía: "Tu código es el que rompe mi servidor."

Esta guerra entre equipos es lo que Docker viene a resolver.

---

## [00:10 - EVOLUCION DEL DESARROLLO]

*(Pasar a la Diapositiva 3: Evolución del Desarrollo)*

¿Cómo hemos intentado solucionar esto a lo largo de los años? Miremos la evolución del desarrollo.

En un principio teníamos los **Monolitos**. Imaginen una aplicación gigante donde absolutamente todo el código está mezclado en un solo bloque enorme.

Si la aplicación era una tienda virtual, el código del carrito de compras, el código del catálogo y el código de pagos estaban pegados todos ahí.

El problema es que si el sistema de pagos fallaba, se caía toda la página. Era difícilísimo de escalar y de mantener.

Luego evolucionamos a los **Microservicios**. Empezamos a picar ese gran monolito en servicios pequeñitos e independientes.

Ahora el carrito de compras es un mini-programa, y el pago es otro mini-programa. Son muy ágiles, sí, pero nos trajeron un problema nuevo: ahora son difícilísimos de gestionar.

¿Cómo instalo y configuro 50 programitas diferentes en un servidor sin volverme loco?

Y así llegamos a la era de los **Contenedores**. La solución final. Un empaquetado estandarizado y completamente aislado para cada uno de esos servicios.

---

## [00:15 - QUE ES DOCKER?]

*(Pasar a la Diapositiva 4 y 5: ¿Qué es Docker? / Analogía del barco)*

Entonces, ¿qué es Docker exactamente?

Docker es la plataforma de código abierto líder en el mundo para crear, desplegar y ejecutar exactamente estas aplicaciones fácilmente.

Para que lo entiendan a la perfección, miren la imagen del barco carguero en la diapositiva.

Hace unos 70 años, el comercio marítimo era un caos. Para cargar un barco había cajas de madera de un tamaño, barriles de vino de otro, sacos de café, piezas sueltas de autos. Cada tipo de carga requería una grúa distinta y un trato distinto. Cargar un barco tomaba semanas.

Hasta que alguien inventó el contenedor marítimo de metal estándar.

Al barco ya no le importa si adentro del contenedor llevas televisores de última generación o plátanos. A la grúa no le importa. Solo ven una caja estándar con dimensiones exactas, y saben exactamente cómo levantarla y apilarla.

Docker hace exactamente esto, pero con el software. Empaqueta tu programa, con su código, sus herramientas, su versión de Java o Python, y todas sus dependencias, en un "contenedor" de software estándar.

Una vez empaquetado, lo puedes poner en tu laptop, en un servidor de Amazon, o en una Raspberry Pi, y va a funcionar exactamente igual.

---

## [00:22 - QUE ES TECNICAMENTE UN CONTENEDOR?]

*(Pasar a la Diapositiva 6 y 7: ¿Qué es un contenedor? / VM vs Contenedor)*

Pero, ¿qué es técnicamente un contenedor? Anoten esto porque es pregunta de examen.

Un contenedor tiene tres características principales:

**Primero, es Ligero**. No requiere que instalemos un Sistema Operativo completo. Comparte el Kernel —es decir, el núcleo— del sistema operativo anfitrión.

**Segundo, es Aislado**. Se ejecuta en una "caja de arena" —un sandbox— segura. Tiene su propia red, sus propios procesos y su propio sistema de archivos. No choca con otros programas.

**Tercero, es Portable**. Como dije, el comportamiento es idéntico en tu PC o en la nube.

Ahora, seguro algunos aquí que han usado VirtualBox o VMware se preguntarán: "Profe, ¿eso no es lo mismo que una Máquina Virtual?"

La respuesta rotunda es NO.

Miremos esta tabla comparativa de la diapositiva.

Una Máquina Virtual —VM— virtualiza el Hardware físico. Para usarla, necesitas instalarle un Sistema Operativo "Invitado" completo: un Windows entero o un Ubuntu entero, solo para correr una pequeña aplicación.

Por eso, una VM pesa Gigabytes, y tarda minutos en encender, igual que tu computadora cuando la prendes.

Un Contenedor, en cambio, virtualiza solo el Sistema Operativo. Los contenedores comparten el "corazón" o núcleo —el Kernel— del sistema operativo anfitrión.

No instalan un Windows o Linux nuevo cada vez. Por eso, un contenedor pesa apenas unos Megabytes, ¡y arranca en cuestión de segundos o incluso milisegundos!

Usar una Máquina Virtual es como comprar una casa entera para alojar a un solo invitado. Usar Docker es como alquilarle una habitación en tu propia casa. Es muchísimo más eficiente.

---

## [00:30 - VENTAJAS DE DOCKER]

*(Pasar a la Diapositiva 8: Ventajas de Docker)*

Para resumir las ventajas antes de pasar a cómo se usa, quédense con esto:

**1. Portabilidad:** Construyes tu código una vez, y corre donde sea. "Build once, run anywhere." Eso es portabilidad real.

**2. Escalabilidad:** Si hoy tu red recibe mucho tráfico, en lugar de comprar otro servidor, puedes levantar 10 réplicas de tu contenedor en cuestión de dos segundos. Sin complicaciones.

**3. Eficiencia:** Aprovechas al máximo la memoria RAM y el procesador de tu servidor. No estás gastando recursos en ejecutar múltiples sistemas operativos completos.

**4. Estandarización:** Es la llave maestra para metodologías de automatización que verán más adelante en su carrera, como Integración y Despliegue Continuo —CI/CD—.

---

## [00:35 - CONCEPTOS CLAVE]

*(Pasar a la Diapositiva 9: Conceptos Clave)*

Bien, dejemos la teoría y pasemos a cómo está compuesto. Necesito que entiendan cuatro palabras clave en el mundo de Docker. Les voy a dar un ejemplo de hacer un pastel para que nunca se les olvide:

**1. Dockerfile:** Es tu script, tu archivo de texto con instrucciones. Es la **receta**. Dice qué ingredientes necesitas y cómo se cocina tu app.

**2. Imagen:** Es una plantilla estática de solo lectura. En nuestra analogía, es el **molde del pastel ya congelado** con los ingredientes adentro. No se puede modificar una vez creado.

**3. Contenedor:** Es la instancia viva y ejecutándose de una Imagen. Es el **pastel horneado y listo para comer**. De una sola "Imagen" —molde—, puedes crear 100 "Contenedores" —pasteles— idénticos.

**4. Docker Hub:** Es un repositorio público en internet. Imagínenlo como el **supermercado o la biblioteca mundial** donde empresas como Microsoft, Ubuntu, o Nginx suben sus Imágenes base para que nosotros las descarguemos gratis y no tengamos que inventar la rueda desde cero.

---

## [00:42 - ARQUITECTURA DE DOCKER]

*(Pasar a la Diapositiva 10 y 11: Arquitectura y Comandos Básicos)*

¿Cómo interactuamos nosotros con Docker? Miren el esquema de la arquitectura.

Nosotros nos sentamos en nuestra computadora y usamos el **Cliente Docker** —la consola o CLI—. Escribimos comandos ahí.

Esos comandos se los mandamos al **Engine** o **Docker Host**, que es un programa silencioso corriendo de fondo. Él es el verdadero jefe. Él es el que crea, apaga y borra contenedores.

Si le pedimos al Engine una Imagen que no tiene, él se conecta a internet al **Registry** —Docker Hub— y la descarga automáticamente.

¿Y cuáles son esos comandos mágicos que le mandamos desde la consola? Anoten estos seis comandos, porque son el pan de cada día:

●  **docker pull:** Para descargar una imagen desde el Docker Hub.

●  **docker images:** Para ver qué imágenes ya tenemos descargadas en nuestra PC.

●  **docker ps:** Muy importante. Te muestra qué contenedores están encendidos en este preciso instante.

●  **docker run:** El más usado. Toma una imagen, crea el contenedor y lo enciende.

●  **docker stop:** Apaga el contenedor suavemente, sin borrarlo.

●  **docker rm:** Elimina el contenedor por completo. Lo borra del disco.

---

## [00:48 - EJEMPLO PRACTICO]

*(Pasar a la Diapositiva 12: Ejemplo Práctico)*

Veamos un ejemplo práctico en pantalla.

Si yo en mi terminal escribo: `docker run hello-world`

Docker buscará la imagen "hello-world", la descargará si no la tiene, y la ejecutará.

Lo único que hace este contenedor es imprimir un mensaje que dice "Hello from Docker!" comprobando que instalamos todo bien.

Pero miren el segundo ejemplo, este es mi favorito: `docker run -it ubuntu bash`

Con este comando, en menos de dos segundos, Docker descarga una distribución limpia de Ubuntu Linux, la arranca, y gracias a las letras -it —Interactive Terminal—, nos mete directamente dentro de su línea de comandos —bash—.

De repente, son administradores root —superusuarios— dentro de un Linux, corriendo sobre su propio Windows, sin haber configurado ninguna máquina virtual ni formateado nada.

Así de poderoso es Docker.

---

## [00:52 - VOLUMENES]

*(Pasar a la Diapositiva 13 y 14: Volúmenes y Dockerfile)*

A estas alturas hay algo que debemos solucionar. Les dije que un contenedor se enciende rápido y se puede destruir —`docker rm`— rápido. Son efímeros por diseño.

Pero, pónganse a pensar... ¿Qué pasa si dentro de nuestro contenedor tenemos corriendo nuestra Base de Datos con toda la información de la empresa, y el contenedor se apaga o se borra por error?

¡Perdemos todos los datos!

El contenedor tiene "amnesia". Todo lo que se genera adentro, muere con él.

Para solucionar la amnesia usamos los **Volúmenes**. Un volumen es literalmente como conectar un disco duro externo o una memoria USB virtual a nuestro contenedor.

Guardamos los datos en el disco duro de nuestra computadora real.

Así, si el contenedor de la Base de Datos explota y se borra, no importa; encendemos uno nuevo en un segundo, lo conectamos a ese mismo "USB" virtual —Volumen—, y los datos siguen ahí intactos.

Es persistencia. Los datos no desaparecen cuando el contenedor muere.

---

## [00:55 - DOCKERFILE]

Para crear una imagen propia usamos un archivo llamado **Dockerfile**.

Miren la estructura típica que ven en la diapositiva:

```
FROM node:18-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
EXPOSE 8080
CMD ["npm", "start"]
```

Vamos línea por línea:

●  **FROM:** Define desde qué imagen base empezamos. Por ejemplo, Linux con Node.

●  **WORKDIR:** Crea una carpeta llamada `/app` adentro del contenedor. Es nuestro espacio de trabajo.

●  **COPY:** Copia nuestros archivos locales de la PC adentro del contenedor.

●  **RUN:** Ejecuta comandos de instalación. Aquí es donde instalan dependencias, como `npm install`.

●  **EXPOSE:** Documenta cuál es el puerto que la aplicación usa internamente. En este caso, 8080.

●  **CMD:** Le dice a Docker qué hacer cuando el contenedor por fin arranque. Aquí es donde inician la app.

Ese Dockerfile es tu "receta" para construir una imagen. Cuando lo ejecutes, Docker leerá cada línea, la realizará paso a paso, y al final tendrás una imagen lista para correr.

---

## [00:58 - BUENAS PRACTICAS]

*(Pasar a la Diapositiva 15: Buenas Prácticas)*

Como futuros profesionales de Redes y Sistemas, no basta con saber que Docker existe; tienen que usarlo bien.

Aquí les dejo las Buenas Prácticas de oro:

**1. Usen imágenes Alpine:** Cuando elijan una base para sus contenedores, busquen que diga "alpine". Es una versión de Linux hiper-reducida que pesa unos 5 Megabytes. Descarga más rápido, ahorra espacio y, al tener menos componentes, es mucho más segura contra ataques cibernéticos.

**2. Nombres explícitos:** Siempre usen el parámetro `--name`. Si no le ponen nombre a sus contenedores, Docker les pone nombres aleatorios graciosos pero inútiles, como "hungry_einstein" o "sleepy_tesla". Usen nombres como "mi-base-de-datos".

**3. Archivo .dockerignore:** Evita copiar `node_modules/` o información sensible al contenedor.

**4. Un Proceso por Contenedor:** Esta es la regla suprema de los microservicios. Nunca metan el Servidor Web —Nginx— y la Base de Datos —MySQL— en el mismo contenedor. Sepárenlos. Así, si la web se sobrecarga, clonamos solo el contenedor de la web, sin afectar la base de datos.

---

## [01:00 - PREPARACION PARA LABORATORIO Y CIERRE]

*(Pasar a la Diapositiva 16: Preparación para Laboratorio y Cierre)*

Para terminar la clase de hoy, y para que se preparen mentalmente para lo que haremos en nuestra sesión de Laboratorio, miren este comando en pantalla. Es el que vamos a ejecutar:

```
docker run -d -p 8080:80 -v /mi/ruta:/usr/share/nginx/html --name mi-web nginx
```

Quiero que entiendan qué hace cada pieza:

●  **docker run:** Arranca el contenedor.

●  **-d (Detached):** Lo manda al fondo —background— para que no bloquee nuestra terminal y siga funcionando.

●  **-p 8080:80 (Puertos):** Magia de redes. Enlaza el puerto 80 del contenedor con el puerto 8080 de nuestra computadora local. Así, al abrir nuestro navegador en localhost:8080, veremos la página.

●  **-v (Volumen):** Está conectando una carpeta de mi computadora —donde haré un archivo de texto HTML— a la carpeta interna del servidor Nginx. Si yo cambio mi código HTML, la página web se actualiza al instante.

●  **--name mi-web:** Lo nombramos para identificarlo fácilmente.

●  **nginx:** Es la imagen pública del servidor web más popular del mundo.

---

## CHECKLIST PARA LA PROXIMA CLASE PRACTICA

Para la próxima clase de laboratorio, necesito que todos vengan con Docker Desktop instalado en sus laptops, o el motor de Docker en sus máquinas virtuales de Linux.

Vamos a levantar servidores web y hacer networking entre ellos.

**Checklist:**

1. ✓ Docker Desktop instalado y abierto (Windows/macOS) o Docker Engine en Linux.
2. ✓ Ejecutar: `docker run hello-world` para validar instalación.
3. ✓ Editor de código (VS Code recomendado).
4. ✓ Una carpeta local lista para montar con volúmenes.
5. ✓ Acceso de administrador en su equipo o VM.

---

## CIERRE DE LA CLASE

¿Alguna pregunta hasta aquí con la teoría?

*(Dar un par de minutos para preguntas de los estudiantes)*

Bien, ahora que entendemos por qué Docker existe, qué problema resuelve y cómo se usa en lo básico, vimos que un contenedor es ligero, aislado y portable; que no es lo mismo que una VM; y que con comandos simples podemos levantar servicios reales.

En la siguiente clase de laboratorio vamos a construir y ejecutar nuestros propios contenedores, mapear puertos y trabajar persistencia con volúmenes. Van a vivir la experiencia completa.

Preparense mentalmente, porque Docker es una puerta a infinitas posibilidades en cloud computing, microservicios y automatización. Esto que aprendemos hoy es el fundamento de tecnologías como Kubernetes, que controlan miles de contenedores en la nube.

¡Que se diviertan en el laboratorio!

---

**FIN DEL GUION DE CLASE**
