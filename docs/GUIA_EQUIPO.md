# Guía del equipo: ejecutar el proyecto en otra PC

Esta guía explica, paso a paso, qué instalar y cómo ejecutar el sistema **La Esquina** en una PC nueva
(Windows o macOS), cómo ver la **interfaz web en Angular** y cómo probar la API con **Postman**.
Tiempo estimado: unos 40 minutos la primera vez (la mayor parte son descargas).

---

## 1. Qué necesitas instalar

| # | Programa | Para qué sirve | Versión |
|---|---|---|---|
| 1 | **Git** | Descargar (clonar) el proyecto desde GitHub | Cualquiera reciente |
| 2 | **JDK (Java)** | Compilar y ejecutar el backend | **17 o superior** (recomendado: Temurin 17 o 21) |
| 3 | **MySQL Server** | Base de datos del sistema | **8.4 LTS** |
| 4 | **MySQL Workbench** | Ver las tablas y los datos | La última |
| 5 | **Apache NetBeans** | Abrir y ejecutar el proyecto (opcional: también corre desde la terminal) | 23 o superior |
| 6 | **Postman** (aplicación de escritorio) | Probar los endpoints de la API | La última |
| 7 | **Node.js** (incluye npm) | Ejecutar la interfaz web en Angular | **24 LTS** (Angular 21 exige 20.19, 22.12 o 24+) |
| 8 | Un navegador | Ver la interfaz web | Chrome, Edge, Firefox o Safari |

> La primera ejecución necesita **internet**, porque Maven y npm descargan las librerías del proyecto
> (unos 100 MB del backend y unos 300 MB del frontend).

### Instalación rápida en Windows (con winget)

Abre **PowerShell** y ejecuta cada línea (Windows pedirá permiso de administrador en algunas):

```powershell
winget install --id Git.Git -e
winget install --id EclipseAdoptium.Temurin.17.JDK -e
winget install --id Oracle.MySQLWorkbench -e
winget install --id Apache.NetBeans -e
winget install --id Postman.Postman -e
winget install --id OpenJS.NodeJS.LTS -e --version 24.22.1
```

**MySQL Server conviene instalarlo con su instalador gráfico**, porque así se configura la contraseña y el servicio:

1. Entra a <https://dev.mysql.com/downloads/mysql/> → versión **8.4.x LTS** → *Windows (x86, 64-bit), MSI Installer* → descarga.
2. Ejecútalo con la opción **Typical**. Al final deja marcado **Run MySQL Configurator**.
3. En el Configurator, en el paso *Accounts and Roles*, escribe una **contraseña para root** y **anótala**.
4. Deja el puerto **3306** y la opción **Configure MySQL Server as a Windows Service**.

> **Si tienes XAMPP:** apaga su MySQL antes de iniciar MySQL 8.4, porque ambos usan el puerto 3306.
> El MariaDB de XAMPP **no** sirve para este proyecto (Hibernate exige MySQL 8 o superior).

### Instalación en macOS

1. **JDK:** `brew install --cask temurin@17` (o desde <https://adoptium.net>).
2. **MySQL 8.4:** instalador `.dmg` de <https://dev.mysql.com/downloads/mysql/>. Elige **ARM 64-bit** si tu Mac tiene chip Apple (M1 a M4) o **x86** si es Intel. Durante la instalación define la contraseña de root. Para encenderlo o apagarlo: *Configuración del Sistema → MySQL*.
3. **Workbench, NetBeans y Postman:** desde sus páginas oficiales.
4. **Git:** viene con las *Command Line Tools* (`xcode-select --install`).
5. **Node.js 24:** `brew install node@24` (o el instalador de <https://nodejs.org>, versión 24 LTS).

Comprueba la instalación de Java en una terminal nueva:

```bash
java -version
```

Debe decir 17 o superior.

---

## 2. Descargar el proyecto

```bash
git clone https://github.com/CristophersVillarreal/nuevo-sistema-inventario.git
```

Se crea la carpeta `nuevo-sistema-inventario`. Si GitHub te pide acceso, pídele a Cristophers que te agregue como
colaborador (en GitHub: *Settings → Collaborators*).

Para traer los cambios más recientes en el futuro:

```bash
cd nuevo-sistema-inventario
git pull
```

---

## 3. Preparar la base de datos

1. Abre **MySQL Workbench** → clic en **+** (nueva conexión) → *Hostname* `127.0.0.1`, *Port* `3306`,
   *Username* `root` → *Store Password* con tu contraseña de root → **Test Connection** → **OK**.
2. Entra a la conexión y ejecuta:

   ```sql
   CREATE DATABASE laesquina;
   ```

   (Es opcional: si no existe, la aplicación la crea sola.)

**No tienes que crear tablas ni datos.** Al ejecutarse por primera vez, la aplicación crea las 5 tablas y carga
los roles, los usuarios de prueba, las categorías y los productos.

---

## 4. Decirle a la aplicación tu contraseña de MySQL

La contraseña **no está en el código**. La aplicación la lee de la variable de entorno `DB_PASSWORD`.
Elige **una** de estas formas.

**Opción A – Windows, permanente (recomendada).** En PowerShell, con tu contraseña real:

```powershell
setx DB_PASSWORD "tu_clave_de_mysql"
```

Después **cierra y vuelve a abrir** NetBeans (o la terminal) para que tome la variable.

**Opción B – Solo en NetBeans (Windows o Mac).** Clic derecho en el proyecto → *Properties* → *Actions* →
selecciona *Run project* → en *Set Properties* agrega esta línea:

```
Env.DB_PASSWORD=tu_clave_de_mysql
```

Repite lo mismo en *Debug project*. Este ajuste se guarda en `nbactions.xml`, que **no** se sube a GitHub.

**Opción C – Terminal (macOS / Linux):**

```bash
export DB_PASSWORD='tu_clave_de_mysql'
```

> Si tu usuario de MySQL no es `root`, define también `DB_USER`. Si tu MySQL usa otro puerto,
> define `DB_URL=jdbc:mysql://localhost:PUERTO/laesquina`.
>
> Otras variables (se configuran igual que `DB_PASSWORD`):
> - `GROQ_API_KEY`: necesaria solo para el **Asistente IA** (cómo obtenerla: README, sección 9).
> - `JWT_SECRET`: clave con la que se firman los tokens de sesión. En tu PC puedes omitirla (hay una clave de
>   desarrollo); si la defines, debe tener al menos 32 caracteres.

---

## 5. Ejecutar el backend

### Desde NetBeans

1. *File → Open Project* → selecciona la carpeta `nuevo-sistema-inventario` (tiene el ícono de Maven) → **Open Project**.
2. Espera a que termine la barra de carga (abajo a la derecha): la primera vez descarga las librerías.
3. Clic derecho en el proyecto → **Run**.

### Desde la terminal (sin NetBeans)

```bash
cd nuevo-sistema-inventario
./mvnw spring-boot:run        # macOS / Linux (si dice "Permission denied": chmod +x mvnw)
mvnw.cmd spring-boot:run      # Windows
```

### ¿Cómo sé que funcionó?

En la consola deben aparecer estas líneas:

```
HikariPool-1 - Start completed.                        ← se conectó a MySQL
Roles iniciales creados: ROLE_ADMIN y ROLE_OPERADOR    ← solo la primera vez
Usuarios de prueba creados: admin (ADMIN) y operador (OPERADOR)
Categorias y productos de prueba creados
Started InventarioApplication in X seconds            ← la aplicación está lista
```

**La aplicación queda corriendo:** no cierres NetBeans ni la terminal mientras la uses.
Para detenerla: botón rojo ■ en NetBeans, o `Ctrl + C` en la terminal.

---

## 6. Ejecutar y ver la interfaz web (Angular)

La interfaz es un proyecto **Angular** separado, en la carpeta `frontend/`. Corre en el puerto **4200** y consume
la API del backend (puerto 8080), así que **el backend debe estar corriendo al mismo tiempo**.

En una **segunda terminal**:

```bash
cd nuevo-sistema-inventario/frontend
npm install          # solo la primera vez (o cuando cambien las dependencias)
npx ng serve
```

Cuando aparezca `Local: http://localhost:4200/`, abre **<http://localhost:4200>** en el navegador.

1. Aparece la **pantalla de inicio de sesión** del sistema. Ingresa uno de los usuarios de prueba
   (las contraseñas están en el `README.md`, sección 6):

   | Usuario | Rol | Qué puede hacer en la interfaz |
   |---|---|---|
   | `admin` | Administrador | Todo: productos, alertas, asistente, gestión de usuarios y perfil |
   | `operador` | Operador de almacén | Consultar productos y alertas, usar el asistente y editar su perfil. No ve la gestión de usuarios ni puede modificar el catálogo |

2. Recorre las vistas del menú izquierdo:

   | Vista | Qué muestra |
   |---|---|
   | **Panel** | Tarjetas resumen, gráficos de stock por categoría y calendario de vencimientos |
   | **Productos** | Listado con crear, editar y eliminar (solo ADMIN) |
   | **Alertas de stock** | Productos con stock bajo y próximos a vencer (consultas JPQL) |
   | **Asistente IA** | Chat con IA real (Groq) sobre el inventario. Necesita la variable `GROQ_API_KEY` (README, sección 9) |
   | **Usuarios** | Crear usuarios y activar o desactivar cuentas (solo ADMIN) |
   | **Mi perfil** | Nombre, apellidos y foto del usuario |

3. **Cerrar sesión** (menú lateral) borra el token y vuelve al login. Así se cambia de usuario.

**Cómo funciona la sesión (JWT):** al iniciar sesión, el backend entrega un **token** que la interfaz envía en cada
petición. El token se guarda en la pestaña del navegador: la sesión **sigue activa si recargas la página (F5)**,
y se pierde al cerrar la pestaña, al cerrar sesión o cuando el token expira (a las 8 horas). Si el token deja de
ser válido, la interfaz te devuelve automáticamente al login.

> Si la terminal dice que `ng` no se reconoce, usa siempre `npx ng serve` (no necesita instalar Angular CLI
> de forma global).

---

## 7. Probar la API con Postman

1. Abre **Postman** → **Import** → arrastra los dos archivos de la carpeta `docs/postman` del proyecto:
   - `LaEsquina_API.postman_collection.json`
   - `LaEsquina_Local.postman_environment.json`
2. Arriba a la derecha, cambia *No Environment* por **La Esquina - Local**.
3. **Primero inicia sesión:** abre la carpeta **00 Autenticación (JWT)** y envía *Login ADMIN* y *Login OPERADOR*.
   Cada una guarda su token, y las demás solicitudes lo usan automáticamente (pestaña *Authorization* → *Bearer Token*).
4. Para probar una solicitud: ábrela en el panel izquierdo → **Send**. Si responde 401 con "El token expiró",
   vuelve a enviar los logins de la carpeta 00.
5. Para probar todo de una vez: pasa el mouse sobre la colección → **⋯ → Run** → **Run La Esquina - API (JWT)**.
   Debe terminar con **38 solicitudes y 52 aserciones aprobadas, 0 fallidas**.

> Si Postman abre la carpeta en *Local mode* y muestra el aviso "Upgrade to v3", **no lo apliques**: cambia a la
> vista de colecciones (ícono del cubo, arriba a la izquierda) e importa los archivos con `Ctrl + O`.

---

## 8. Ejecutar las pruebas automáticas

```bash
./mvnw test          # macOS / Linux
mvnw.cmd test        # Windows
```

O en NetBeans: clic derecho en el proyecto → **Test**.
Resultado esperado: `Tests run: 22, Failures: 0, Errors: 0` y `BUILD SUCCESS`.
Estas pruebas usan una base de datos en memoria (H2), así que **no modifican** tu MySQL.

---

## 9. Volver a empezar con datos limpios

Las pruebas (sobre todo el Runner de Postman) modifican los datos. Para dejar la base como el primer día:

1. Detén la aplicación.
2. En Workbench:

   ```sql
   DROP DATABASE laesquina;
   CREATE DATABASE laesquina;
   ```

3. Ejecuta la aplicación de nuevo: se vuelven a crear las tablas y los datos de prueba.

Con la base limpia, los valores coinciden con los del informe (Coca Cola con stock 5, Arroz 30, etc.).

---

## 10. Problemas frecuentes

| Mensaje o síntoma | Causa | Solución |
|---|---|---|
| `Access denied for user 'root'@'localhost'` | `DB_PASSWORD` vacía o incorrecta | Revisa la sección 4; cierra y abre NetBeans después de `setx` |
| `Communications link failure` | MySQL está apagado | Enciéndelo (Servicios de Windows → MySQL84, o *Configuración del Sistema → MySQL* en Mac) |
| `HHH000511 ... 5.5.5 version ... minimum supported version is 8.0.0` | Te conectaste al MariaDB de XAMPP, no a MySQL 8.4 | Apaga XAMPP y enciende MySQL 8.4 |
| `Port 8080 was already in use` | La aplicación ya está corriendo (u otro programa usa el puerto) | Detén la ejecución anterior (botón rojo ■ en NetBeans) |
| `Could not find or load main class ${start-class}` | Versión antigua del proyecto | Ejecuta `git pull` (ya está corregido en el `pom.xml`) |
| `release version 17 not supported` | Tu JDK es menor que 17 | Instala el JDK 17 o 21 y selecciónalo en NetBeans (*Tools → Java Platforms*) |
| El login dice "Usuario o contraseña incorrectos" | Credenciales equivocadas | Revisa los usuarios de prueba en el `README.md` (sección 6) |
| El login dice "La cuenta está desactivada" | Un administrador desactivó ese usuario | Actívalo desde la vista **Usuarios** con el `admin` |
| La interfaz te devuelve al login sin motivo aparente | El token expiró (8 h) o se reinició el backend con otro `JWT_SECRET` | Vuelve a iniciar sesión |
| `ng` no se reconoce como comando | Angular CLI no está instalado de forma global | Usa `npx ng serve` |
| La interfaz carga pero no muestra datos | El backend está apagado | Ejecuta el backend (sección 5) en otra terminal |
| `./mvnw: Permission denied` (Mac) | Falta el permiso de ejecución | `chmod +x mvnw` |
| Postman: `{{baseUrl}}` en rojo o "Could not send request" | No se seleccionó el entorno, o la app está apagada | Selecciona **La Esquina - Local** y verifica que la app esté corriendo |
| Postman: todas las solicitudes dan 401 | Aún no se ejecutaron los logins o el token expiró | Envía las solicitudes de la carpeta **00 Autenticación (JWT)** |

---

## 11. Lista de verificación final

- [ ] `java -version` muestra 17 o superior.
- [ ] MySQL 8.4 está encendido y Workbench se conecta.
- [ ] `DB_PASSWORD` está configurada.
- [ ] La consola muestra `HikariPool-1 - Start completed` y `Started InventarioApplication`.
- [ ] `node -v` muestra 24 (o 20.19+ / 22.12+).
- [ ] <http://localhost:4200> muestra el login; con `admin` entra al panel y **Cerrar sesión** vuelve al login.
- [ ] En Workbench aparecen las 5 tablas dentro de `laesquina`.
- [ ] El Runner de Postman termina con 52/52 aserciones.
- [ ] `mvnw test` termina con 22 pruebas y `BUILD SUCCESS`.
