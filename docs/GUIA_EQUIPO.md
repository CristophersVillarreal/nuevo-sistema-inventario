# Guía del equipo: ejecutar el proyecto en otra PC

Esta guía explica, paso a paso, qué instalar y cómo ejecutar el sistema **La Esquina** en una PC nueva
(Windows o macOS), cómo ver la **interfaz web** y cómo probar la API con **Postman**.
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
| 7 | Un navegador | Ver la interfaz web | Chrome, Edge, Firefox o Safari |

> La primera ejecución necesita **internet**, porque Maven descarga las librerías del proyecto (unos 100 MB).

### Instalación rápida en Windows (con winget)

Abre **PowerShell** y ejecuta cada línea (Windows pedirá permiso de administrador en algunas):

```powershell
winget install --id Git.Git -e
winget install --id EclipseAdoptium.Temurin.17.JDK -e
winget install --id Oracle.MySQLWorkbench -e
winget install --id Apache.NetBeans -e
winget install --id Postman.Postman -e
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

---

## 5. Ejecutar el proyecto

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

## 6. Ver la interfaz web

Con la aplicación corriendo:

1. Abre el navegador en **<http://localhost:8080>**.
2. El navegador muestra un cuadro de **inicio de sesión**: es la autenticación HTTP Basic de Spring Security.
   Ingresa uno de los usuarios de prueba (las contraseñas están en el `README.md`, sección 5):

   | Usuario | Rol | Qué puede hacer en la interfaz |
   |---|---|---|
   | `admin` | Administrador | Ver todo, crear, editar y eliminar productos |
   | `operador` | Operador de almacén | Ver todo; si intenta crear, editar o eliminar, aparece el mensaje *"Su rol no tiene permiso para realizar esta operación"* |

3. Recorre las vistas del menú izquierdo:

   | Vista | Qué muestra |
   |---|---|
   | **Panel** | Total de productos, unidades en stock, valor del inventario y alertas de stock mínimo |
   | **Productos** | Tabla con categoría, ubicación, vencimiento, precio y stock; botones Nuevo, Editar y Eliminar |
   | **Alertas de stock** | Productos con stock bajo (consulta JPQL) y productos próximos a vencer en 30 días (consulta JPQL) |
   | **Asistente IA** | **Vista previa sin IA real** (responde con reglas simples). La IA con OpenAI se implementará en la etapa final |

**Para cambiar de usuario:** el navegador recuerda las credenciales mientras está abierto. Para entrar con el otro
usuario, cierra **todas** las ventanas del navegador y vuelve a abrirlo, o usa una **ventana de incógnito**
(`Ctrl + Shift + N` en Chrome/Edge). Un buen truco para la demo es tener el `admin` en una ventana normal y el
`operador` en una de incógnito.

> Limitación conocida del Avance 2: la interfaz todavía **no tiene la vista de movimientos** (entradas, salidas y lote).
> Esas operaciones se prueban desde Postman (sección 7).

---

## 7. Probar la API con Postman

1. Abre **Postman** → **Import** → arrastra los dos archivos de la carpeta `docs/postman` del proyecto:
   - `LaEsquina_Avance2.postman_collection.json`
   - `LaEsquina_Local.postman_environment.json`
2. Arriba a la derecha, cambia *No Environment* por **La Esquina - Local**.
3. Para probar una solicitud: ábrela en el panel izquierdo → **Send**.
4. Para probar todo de una vez: pasa el mouse sobre la colección → **⋯ → Run** → **Run La Esquina - Avance 2**.
   Debe terminar con **40 aserciones aprobadas y 0 fallidas**.

> Si Postman abre la carpeta en *Local mode* y muestra el aviso "Upgrade to v3", **no lo apliques**: cambia a la
> vista de colecciones (ícono del cubo, arriba a la izquierda) e importa los archivos con `Ctrl + O`.

---

## 8. Ejecutar las pruebas automáticas

```bash
./mvnw test          # macOS / Linux
mvnw.cmd test        # Windows
```

O en NetBeans: clic derecho en el proyecto → **Test**.
Resultado esperado: `Tests run: 16, Failures: 0, Errors: 0` y `BUILD SUCCESS`.
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
| El navegador pide la contraseña una y otra vez | Usuario o contraseña incorrectos | Revisa los usuarios de prueba en el `README.md` |
| `./mvnw: Permission denied` (Mac) | Falta el permiso de ejecución | `chmod +x mvnw` |
| La interfaz se ve sin estilos | Sin internet (Bootstrap se carga desde un CDN) | Conéctate a internet y recarga la página |
| Postman: `{{baseUrl}}` en rojo o "Could not send request" | No se seleccionó el entorno, o la app está apagada | Selecciona **La Esquina - Local** y verifica que la app esté corriendo |

---

## 11. Lista de verificación final

- [ ] `java -version` muestra 17 o superior.
- [ ] MySQL 8.4 está encendido y Workbench se conecta.
- [ ] `DB_PASSWORD` está configurada.
- [ ] La consola muestra `HikariPool-1 - Start completed` y `Started InventarioApplication`.
- [ ] <http://localhost:8080> pide usuario y contraseña, y muestra el panel.
- [ ] En Workbench aparecen las 5 tablas dentro de `laesquina`.
- [ ] El Runner de Postman termina con 40/40 aserciones.
- [ ] `mvnw test` termina con 16 pruebas y `BUILD SUCCESS`.
