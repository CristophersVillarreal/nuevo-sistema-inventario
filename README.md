# Sistema de Gestión de Inventario – Minimarket "La Esquina"

Proyecto del curso **Desarrollo Web Integrado** (sección 34385).
Backend REST con **Spring Boot 3.3.4 + Spring Data JPA + MySQL + Spring Security (HTTP Basic)**,
y una interfaz web con **HTML, CSS, JavaScript y Bootstrap 5**.

> Este repositorio es una **nueva implementación** del sistema de inventario "La Esquina".
> Toma como referencia funcional un sistema anterior del mismo nombre, construido con otras herramientas,
> y lo desarrolla de nuevo con las tecnologías del curso (Java, Spring Boot, JPA, MySQL y Spring Security).

| Avance | Contenido |
|---|---|
| 1 | API REST de productos con datos en memoria. |
| **2 (actual)** | Persistencia en MySQL con JPA/Hibernate, consultas JPQL, transacciones y seguridad por roles. |
| Siguiente etapa | Asistente de IA (OpenAI), JWT, reportes. |

---

## 1. Requisitos

| Herramienta | Versión |
|---|---|
| JDK | 17 o superior (`java -version`) |
| MySQL Server | 8.4 LTS (Community) + MySQL Workbench |
| IDE | Apache NetBeans (o cualquier IDE con soporte Maven) |
| Postman | Para ejecutar la colección de pruebas |

No hace falta instalar Maven: el proyecto incluye el *wrapper* (`mvnw` / `mvnw.cmd`).

---

## 2. Base de datos

> Guía paso a paso para Windows y macOS (incluida la conexión de Workbench): **[docs/INSTALACION_MYSQL.md](docs/INSTALACION_MYSQL.md)**.

1. Instalar **MySQL 8.4 LTS** y anotar la contraseña de `root`.
   - **Windows**: MySQL Installer (dev.mysql.com). Si XAMPP está instalado, **apagar su MySQL**, porque ambos usan el puerto 3306.
   - **macOS**: instalador `.dmg` de dev.mysql.com, o `brew install mysql@8.4`.
2. Crear la base de datos (opcional: la aplicación la crea sola si no existe):
   ```sql
   CREATE DATABASE laesquina;
   ```
3. **No hay que crear tablas**: Hibernate las genera en el primer arranque (`ddl-auto=update`) y
   `DataInitializer` carga los roles, los usuarios, las categorías y los productos de prueba
   **solo si las tablas están vacías**. Los reinicios no duplican ni borran datos.

---

## 3. Variables de entorno

Las credenciales **no se escriben en el código**. Se leen de variables de entorno:

| Variable | Valor por defecto | Descripción |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/laesquina?...` | URL de conexión |
| `DB_USER` | `root` | Usuario de MySQL |
| `DB_PASSWORD` | *(vacío)* | **Contraseña de MySQL (obligatoria si root tiene clave)** |

**NetBeans**: clic derecho en el proyecto → *Properties* → *Actions* → *Run project* → en
*Set Properties* agregar la línea `Env.DB_PASSWORD=tu_clave` (lo mismo para *Debug project*).

**Terminal (macOS / Linux)**:
```bash
export DB_PASSWORD='tu_clave'
```

**Terminal (Windows PowerShell)**:
```powershell
$env:DB_PASSWORD = 'tu_clave'
```

---

## 4. Ejecutar

```bash
./mvnw spring-boot:run          # macOS / Linux (la primera vez: chmod +x mvnw)
mvnw.cmd spring-boot:run        # Windows
```

También se puede usar **Run Project** en NetBeans.

La consola debe mostrar `HikariPool-1 - Start completed` (conexión a MySQL) y
`Started InventarioApplication`. Interfaz web: <http://localhost:8080> (el navegador pide usuario y contraseña).

---

## 5. Usuarios de prueba

Los crea `DataInitializer` con la contraseña **cifrada con BCrypt**:

| Usuario | Contraseña de prueba | Rol | Puede |
|---|---|---|---|
| `admin` | `Admin2026!` | ROLE_ADMIN | Todo: gestionar productos y categorías, consultar y registrar movimientos |
| `operador` | `Operador2026!` | ROLE_OPERADOR | Consultar productos, categorías y movimientos; registrar entradas y salidas |

> Son contraseñas **de prueba**. Se pueden cambiar con las variables `ADMIN_PASSWORD` y
> `OPERADOR_PASSWORD` **antes del primer arranque** (después ya están guardadas en la BD).

---

## 6. Endpoints

Todas las rutas exigen autenticación HTTP Basic. Sin credenciales → **401**; rol sin permiso → **403**.

| Método | Ruta | ADMIN | OPERADOR | Descripción |
|---|---|:-:|:-:|---|
| GET | `/api/productos` | ✔ | ✔ | Listar productos |
| GET | `/api/productos/{id}` | ✔ | ✔ | Consultar un producto (404 si no existe) |
| POST | `/api/productos` | ✔ | ✘ | Registrar producto (201) |
| PUT | `/api/productos/{id}` | ✔ | ✘ | Actualizar producto |
| DELETE | `/api/productos/{id}` | ✔ | ✘ | Eliminar (204; **409** si tiene movimientos) |
| GET | `/api/productos/stock-bajo` | ✔ | ✔ | **JPQL**: stock ≤ mínimo (RF03) |
| GET | `/api/productos/por-vencer?dias=30` | ✔ | ✔ | **JPQL**: próximos a vencer (RF04) |
| GET | `/api/categorias[/{id}]` | ✔ | ✔ | Listar o consultar categorías |
| POST/PUT/DELETE | `/api/categorias[/{id}]` | ✔ | ✘ | Gestionar categorías (409 si tiene productos) |
| GET | `/api/movimientos` | ✔ | ✔ | Todos los movimientos |
| GET | `/api/movimientos/producto/{id}` | ✔ | ✔ | **JPQL**: historial de un producto (RF05) |
| POST | `/api/movimientos` | ✔ | ✔ | **Transacción**: ENTRADA/SALIDA (400 si stock insuficiente) |
| POST | `/api/movimientos/lote` | ✔ | ✔ | **Transacción**: varios movimientos, todo o nada (rollback) |

Ejemplo de cuerpo para registrar un movimiento:
```json
{ "tipo": "SALIDA", "cantidad": 3, "producto": { "id": 4 } }
```

---

## 7. Pruebas

**Automáticas (JUnit 5)** – usan H2 en memoria, no tocan MySQL:
```bash
./mvnw test
```
16 pruebas: consultas JPQL, transacciones (incluido el rollback del lote) y seguridad (200/201/401/403).

**Postman** – importar los dos archivos de `docs/postman/` y ejecutar la colección con el *Runner*
usando el entorno *La Esquina - Local* (32 solicitudes, 40 aserciones). Para que los valores coincidan con
el informe, ejecutarla sobre una base recién creada.

---

## 8. Estructura

```
src/main/java/com/laesquina/inventario
├── InventarioApplication.java
├── config/       DataInitializer (datos iniciales)
├── controller/   ProductoController, CategoriaController, MovimientoController,
│                 ManejadorErrores, ErrorRespuesta
├── model/        Producto, Categoria, MovimientoInventario, TipoMovimiento, Usuario, Rol
├── repository/   Interfaces JpaRepository + consultas JPQL (@Query)
├── security/     SecurityConfig, UsuarioDetailsService, RespuestasSeguridad
└── service/      ProductoService, CategoriaService, MovimientoService (@Transactional)
docs/
├── PLAN_AVANCE_2.md        Plan del avance
├── GUIA_CAPTURAS.md        Qué capturar para el informe
├── INSTALACION_MYSQL.md    Instalación de MySQL 8.4 (Windows y macOS)
├── postman/                Colección y entorno de Postman
└── sql/                    Consultas de verificación para MySQL Workbench
```
