# Instalación de MySQL 8.4 – La Esquina

Guía para dejar MySQL listo en **Windows** (desarrollo) y en **macOS** (sustentación).
La aplicación espera MySQL en `localhost:3306`, la base de datos `laesquina` y la contraseña en la
variable de entorno `DB_PASSWORD`.

---

## A. Windows (equipo de desarrollo) – YA INSTALADO

Se instaló con `winget` desde los paquetes oficiales de Oracle:

| Componente | Versión | Ubicación |
|---|---|---|
| MySQL Server (LTS) | 8.4.9 | `C:\Program Files\MySQL\MySQL Server 8.4` |
| MySQL Workbench | 26.7.0 (instalación por usuario) | `C:\Users\<usuario>\AppData\Local\Programs\MySQL\MySQL Workbench` |
| Instancia del proyecto | – | `C:\Users\<usuario>\MySQL84` (configuración y datos) |

La instancia **no es un servicio de Windows**: no arranca sola ni ocupa el puerto 3306 si no la usas.
Así no choca con el MySQL de XAMPP (pero **no enciendas los dos a la vez**, porque ambos usan el puerto 3306).

### Encender y apagar

En el escritorio quedaron dos accesos directos:

- **`iniciar-mysql (La Esquina)`**: enciende MySQL; la ventana muestra `mysqld is alive`.
- **`detener-mysql (La Esquina)`**: lo apaga de forma ordenada.

### Contraseña

- Se generó una contraseña aleatoria para `root`. Está en `C:\Users\<usuario>\MySQL84\CREDENCIALES-MySQL.txt`.
  **No la subas a GitHub ni la pongas en el informe.**
- La misma contraseña quedó en la **variable de entorno de usuario `DB_PASSWORD`**, que es la que lee
  `application.properties`. **Cierra y vuelve a abrir NetBeans** para que la tome. Ya no hace falta configurarla en
  *Properties → Actions*.

### Conectar MySQL Workbench

1. Abrir **MySQL Workbench** (menú Inicio).
2. Clic en **+** junto a *MySQL Connections*.
3. *Connection Name*: `La Esquina`; *Hostname*: `127.0.0.1`; *Port*: `3306`; *Username*: `root`.
4. *Store in Vault…* → pegar la contraseña de `CREDENCIALES-MySQL.txt` → **Test Connection** → OK.
5. Abrir `docs/sql/consultas_verificacion.sql` (*File → Open SQL Script*).

### Volver a empezar con la base vacía (antes de la sesión de capturas)

```sql
DROP DATABASE IF EXISTS laesquina;
CREATE DATABASE laesquina;
```

Al arrancar, la aplicación vuelve a crear las tablas y los datos de prueba.

---

## B. macOS (equipo de sustentación)

### 1. MySQL Server 8.4 LTS

**Opción A – instalador oficial (recomendada):**
1. Ir a <https://dev.mysql.com/downloads/mysql/> → versión **8.4.x LTS** → *macOS*.
   Elegir **ARM 64-bit** para Mac con chip Apple (M1–M4) o **x86 64-bit** para Mac con Intel
   ( → *Acerca de esta Mac*).
2. Abrir el `.dmg` y seguir el instalador. Elegir **Use Strong Password Encryption** y definir la contraseña de `root`.
   Anotarla.
3. Para encender o apagar: *Configuración del Sistema* → **MySQL** (al final del panel) → *Start/Stop*.

**Opción B – Homebrew:**
```bash
brew install mysql@8.4
brew services start mysql@8.4
mysql -u root
```
Y dentro de `mysql`:
```sql
ALTER USER 'root'@'localhost' IDENTIFIED BY 'tu_clave';
```

### 2. Crear la base de datos

```bash
mysql -u root -p -e "CREATE DATABASE laesquina;"
```

Esto es opcional: la aplicación la crea sola si no existe.

### 3. MySQL Workbench

Descargarlo de <https://dev.mysql.com/downloads/workbench/> (macOS) y crear la conexión igual que en Windows
(`127.0.0.1:3306`, usuario `root`).

### 4. Java 17 o superior

```bash
java -version
```

Si no está instalado: `brew install --cask temurin@17`, o descargarlo de <https://adoptium.net>.

### 5. Obtener el proyecto

```bash
git clone https://github.com/CristophersVillarreal/nuevo-sistema-inventario.git
cd nuevo-sistema-inventario
./mvnw test
```

`./mvnw test` debe terminar con `Tests run: 16, Failures: 0` y `BUILD SUCCESS`.

### 6. Contraseña para la aplicación

En macOS, las apps abiertas desde el Dock (como NetBeans) **no heredan** las variables de la terminal. Elige una opción:

- **Desde la terminal:**
  ```bash
  export DB_PASSWORD='tu_clave'
  ./mvnw spring-boot:run
  ```
- **Desde NetBeans:** clic derecho en el proyecto → *Properties* → *Actions* → *Run project* → en *Set Properties*
  agregar `Env.DB_PASSWORD=tu_clave` (repetir en *Debug project*).

  El archivo que guarda esto (`nbactions.xml`) está en `.gitignore`, así que la clave no se sube a GitHub.

### 7. Verificación final (un día antes de sustentar)

1. MySQL encendido → arrancar la app → en la consola deben aparecer `HikariPool-1 - Start completed` y `Started InventarioApplication`.
2. Abrir <http://localhost:8080> → entrar con el usuario `operador` (contraseña de prueba en el README).
3. Postman → importar `docs/postman/` → *Run collection* → **32 solicitudes y 40 aserciones en verde**.
4. Revisar en Workbench que existan las 5 tablas.

---

## Problemas frecuentes

| Síntoma | Causa | Solución |
|---|---|---|
| `Access denied for user 'root'@'localhost'` | `DB_PASSWORD` vacía o incorrecta | Revisar la variable; en Windows, reiniciar NetBeans |
| `Communications link failure` | MySQL apagado | Encenderlo (acceso directo o *Configuración del Sistema → MySQL*) |
| XAMPP no inicia su MySQL | El puerto 3306 lo usa MySQL 8.4 | Apagar MySQL 8.4 con `detener-mysql` |
| `./mvnw: Permission denied` (Mac) | Falta el permiso de ejecución | `chmod +x mvnw` |
| Aviso `HHH000511 … 5.5.5 version` | La app se conectó a MariaDB (XAMPP), no a MySQL 8.4 | Apagar XAMPP y encender MySQL 8.4 |
