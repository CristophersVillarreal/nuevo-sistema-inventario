

Readme · MD
# Sistema de Gestión de Inventario – Minimarket "La Esquina"

Proyecto del curso **Desarrollo Web Integrado** (sección 34385).
Backend REST con **Spring Boot 3.3.4 + Spring Data JPA + MySQL + Spring Security con JWT**,
y una interfaz web construida con **Angular 21**, consumiendo la API REST del backend.
Incluye un **asistente de inventario con IA real (Groq)**, que responde consultas en lenguaje
natural basándose en los datos reales de la base de datos.

> Este repositorio es una **nueva implementación** del sistema de inventario "La Esquina".
> Toma como referencia funcional un sistema anterior del mismo nombre, construido con otras herramientas,
> y lo desarrolla de nuevo con las tecnologías del curso (Java, Spring Boot, JPA, MySQL, Spring Security y Angular).

| Avance | Contenido |
|---|---|
| 1 | API REST de productos con datos en memoria. |
| 2 | Persistencia en MySQL con JPA/Hibernate, consultas JPQL, transacciones y seguridad por roles. |
| **3 (actual)** | Frontend en Angular (login, dashboard, CRUD, alertas, gestión de usuarios, perfil), autenticación con **JWT** y asistente de inventario con IA real (Groq). |
 
---

> **¿Primera vez en otra PC?** Sigue la guía paso a paso: **[docs/GUIA_EQUIPO.md](docs/GUIA_EQUIPO.md)** (instalación, ejecución, interfaz web y Postman).

## 1. Requisitos

| Herramienta | Versión |
|---|---|
| JDK | 17 o superior (`java -version`) |
| MySQL Server | 8.4 LTS (Community) + MySQL Workbench |
| Node.js + npm | LTS (`node -v`, `npm -v`) — necesario para el frontend en Angular |
| Angular CLI | Instalado globalmente (`npm install -g @angular/cli`) |
| IDE | Apache NetBeans / IntelliJ IDEA (backend), cualquier editor con terminal (frontend) |
| Postman | Para ejecutar la colección de pruebas |
| Cuenta en Groq | Gratuita, para el asistente con IA (ver sección 9) |

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
| `GROQ_API_KEY` | *(vacío)* | **API Key de Groq, obligatoria para el asistente con IA (ver sección 9)** |
| `JWT_SECRET` | clave de desarrollo | Clave para firmar los tokens JWT (mínimo 32 caracteres). **Obligatorio definirla fuera del entorno local** |
| `JWT_EXPIRACION_MINUTOS` | `480` | Duración del token (480 min = 8 horas) |

**NetBeans**: clic derecho en el proyecto → *Properties* → *Actions* → *Run project* → en
*Set Properties* agregar las líneas `Env.DB_PASSWORD=tu_clave` y `Env.GROQ_API_KEY=tu_key` (lo mismo para *Debug project*).

**Terminal (macOS / Linux)**:
```bash
export DB_PASSWORD='tu_clave'
export GROQ_API_KEY='tu_key_de_groq'
```

**Terminal (Windows PowerShell)**:
```powershell
$env:DB_PASSWORD = 'tu_clave'
$env:GROQ_API_KEY = 'tu_key_de_groq'
```
 
---

## 4. Ejecutar el backend

```bash
./mvnw spring-boot:run          # macOS / Linux (la primera vez: chmod +x mvnw)
mvnw.cmd spring-boot:run        # Windows
```

También se puede usar **Run Project** en NetBeans/IntelliJ.

La consola debe mostrar `HikariPool-1 - Start completed` (conexión a MySQL) y
`Started InventarioApplication`. El backend queda disponible en `http://localhost:8080`.
 
---

## 5. Ejecutar el frontend (Angular)

El frontend vive en la carpeta `frontend/`, como un proyecto independiente del backend.

```bash
cd frontend
npm install        # solo la primera vez, o si se agregaron dependencias nuevas
ng serve
```

Abrir `http://localhost:4200`. El backend debe estar corriendo en paralelo (puerto 8080), en otra terminal.

### Librerías instaladas en el frontend, además de las que trae Angular por defecto

| Librería | Comando de instalación | Para qué se usa |
|---|---|---|
| `apexcharts`, `ng-apexcharts` | `npm install apexcharts ng-apexcharts` | Gráficos dinámicos del Panel (barras, radial, líneas) |
| `ngx-markdown@21.3.0`, `marked@17.0.0` | `npm install ngx-markdown@21.3.0 marked@17.0.0` | Renderizar las respuestas del Asistente IA (incluye tablas) como HTML real, en vez de texto plano con símbolos |

> Las versiones de `ngx-markdown` y `marked` están fijadas explícitamente porque deben coincidir con la versión de Angular del proyecto (Angular 21) — una versión más nueva de la librería puede romper la instalación.
 
---

## 6. Usuarios de prueba

Los crea `DataInitializer` con la contraseña **cifrada con BCrypt**:

| Usuario | Contraseña de prueba | Rol | Puede |
|---|---|---|---|
| `admin` | `Admin2026!` | ROLE_ADMIN | Todo: gestionar productos, categorías y usuarios; consultar y registrar movimientos |
| `operador` | `Operador2026!` | ROLE_OPERADOR | Consultar productos, categorías y movimientos; registrar entradas y salidas |

> Son contraseñas **de prueba**. Se pueden cambiar con las variables `ADMIN_PASSWORD` y
> `OPERADOR_PASSWORD` **antes del primer arranque** (después ya están guardadas en la BD).
>
> El administrador puede crear usuarios adicionales (con cualquier rol) desde la vista **Usuarios**
> del dashboard, además de activar/desactivar cuentas existentes. Una cuenta desactivada no puede iniciar sesión.
 
---

## 7. Vistas del frontend (Angular)

| Vista | Ruta | Quién la ve | Descripción |
|---|---|---|---|
| Login | `/login` | Todos | Pantalla de acceso propia (no el cuadro del navegador) |
| Panel | `/dashboard/panel` | ADMIN y OPERADOR | Tarjetas resumen, gráficos dinámicos (stock por categoría, % de productos sin alerta, top productos) y calendario interactivo de vencimientos |
| Productos | `/dashboard/productos` | ADMIN y OPERADOR (CRUD solo ADMIN) | Listado completo con crear/editar/eliminar; el Operador solo consulta |
| Alertas de stock | `/dashboard/alertas` | ADMIN y OPERADOR | Productos con stock bajo (con indicador de severidad) y próximos a vencer |
| Asistente IA | `/dashboard/asistente` | ADMIN y OPERADOR | Chat con IA real (Groq), con historial de conversaciones, limitado estrictamente a consultas de inventario |
| Usuarios | `/dashboard/usuarios` | Solo ADMIN | Listar, crear usuarios con rol, y activar/desactivar cuentas |
| Mi perfil | `/dashboard/perfil` | ADMIN y OPERADOR | Cada usuario edita su propio nombre, apellidos y foto (usuario y contraseña quedan bloqueados) |
 
---

## 8. Endpoints

### Autenticación con JWT

1. El cliente inicia sesión **una sola vez** en `POST /api/auth/login`:
   ```json
   { "username": "admin", "password": "…" }
   ```
2. La API responde con un **token JWT** firmado (HS256) que contiene el usuario, el rol y la expiración:
   ```json
   { "token": "eyJhbGciOiJIUzI1NiJ9…", "tipo": "Bearer", "expiraEnSegundos": 28800, "usuario": "admin", "rol": "ADMIN", … }
   ```
3. En cada solicitud siguiente se envía la cabecera `Authorization: Bearer <token>`.
   `JwtAuthenticationFilter` valida la firma y la expiración, y carga el rol del usuario.

Respuestas de seguridad (siempre en JSON):

| Situación | Respuesta |
|---|---|
| Sin token | **401** "Debe iniciar sesión y enviar el token en la cabecera Authorization" |
| Usuario o contraseña incorrectos en el login | **401** "Usuario o contraseña incorrectos" |
| Token alterado o mal formado | **401** "El token no es válido" |
| Token expirado | **401** "El token expiró; inicie sesión nuevamente" |
| Cuenta desactivada (aunque su token siga vigente) | **401** "La cuenta está desactivada" |
| Usuario autenticado sin el rol necesario | **403** "Su rol no tiene permiso para realizar esta operación" |

En Angular, el token se guarda en `sessionStorage`: la sesión **sobrevive a recargar la página**, se borra al
cerrar la pestaña o con **Cerrar sesión**, y si el backend responde 401 se vuelve automáticamente al login.

Todas las rutas, salvo el inicio de sesión y las fotos de perfil, exigen un token válido.

### Inventario (ya existentes)

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

### Nuevos, agregados para el frontend en Angular y el asistente con IA

| Método | Ruta | ADMIN | OPERADOR | Descripción |
|---|---|:-:|:-:|---|
| POST | `/api/auth/login` | público | público | Inicia sesión y devuelve el token JWT (401 si las credenciales son incorrectas o la cuenta está desactivada) |
| GET | `/api/auth/me` | ✔ | ✔ | Datos del usuario dueño del token (usuario, rol, nombre, apellidos, foto) |
| POST | `/api/asistente/consultar` | ✔ | ✔ | Envía una pregunta al asistente; responde con IA real (Groq), usando el inventario actual como contexto |
| GET | `/api/usuarios` | ✔ | ✘ | Listar todos los usuarios del sistema |
| POST | `/api/usuarios` | ✔ | ✘ | Crear un usuario nuevo (usuario, contraseña, rol, nombre, apellidos) |
| PUT | `/api/usuarios/{id}/estado` | ✔ | ✘ | Activar o desactivar una cuenta, sin borrarla |
| GET | `/api/perfil` | ✔ | ✔ | Ver los datos del propio usuario autenticado |
| PUT | `/api/perfil` | ✔ | ✔ | Actualizar el propio nombre y apellidos (nunca usuario/contraseña) |
| POST | `/api/perfil/foto` | ✔ | ✔ | Subir/actualizar la foto de perfil propia (multipart) |

Ejemplo de cuerpo para registrar un movimiento:
```json
{ "tipo": "SALIDA", "cantidad": 3, "producto": { "id": 4 } }
```

Ejemplo de cuerpo para consultar al asistente:
```json
{ "pregunta": "¿qué productos están próximos a vencer?" }
```
 
---

## 9. Asistente de inventario con IA (Groq)

El asistente usa **Groq** (inferencia de modelos open-source, gratuita en su capa inicial) para responder
preguntas en lenguaje natural sobre el inventario, basándose siempre en los datos reales de la base de datos
(nunca inventa productos). Tiene reglas estrictas: no escribe código, no genera imágenes, y rechaza temas
ajenos al inventario — manteniendo un trato natural para saludos y cortesías.

### Cómo obtener y configurar la API Key

1. Crear una cuenta gratuita en **[console.groq.com](https://console.groq.com)**.
2. En el menú lateral, ir a **API Keys → Create API Key**, ponerle un nombre y copiarla (solo se muestra una vez).
3. Definir la variable de entorno `GROQ_API_KEY` con esa key (ver sección 3).
4. El modelo usado es `openai/gpt-oss-20b`, configurado en `application.properties`:
```properties
   groq.api.key=${GROQ_API_KEY:}
   groq.api.url=https://api.groq.com/openai/v1/chat/completions
   groq.api.modelo=openai/gpt-oss-20b
```

> La lista de modelos disponibles en Groq cambia con el tiempo. Si el modelo configurado deja de estar
> disponible, se puede consultar la lista vigente para la cuenta con:
> `GET https://api.groq.com/openai/v1/models` (con el header `Authorization: Bearer <tu_key>`).
 
---

## 10. Pruebas

**Automáticas (JUnit 5)** – usan H2 en memoria, no tocan MySQL:
```bash
./mvnw test
```
22 pruebas: consultas JPQL, transacciones (incluido el rollback del lote) y seguridad con JWT (login correcto e
incorrecto, token alterado, token expirado, cuenta desactivada y la matriz de permisos 200/201/401/403).

**Postman** – importar los dos archivos de `docs/postman/` y ejecutar la colección con el *Runner*
usando el entorno *La Esquina - Local* (38 solicitudes, 52 aserciones). La carpeta **00 Autenticación (JWT)**
va primero: inicia sesión con `admin` y `operador` y guarda sus tokens, que usan todas las demás solicitudes.
Para que los valores coincidan con el informe, ejecutarla sobre una base recién creada.
 
---

## 11. Estructura actual del proyecto

```
nuevo-sistema-inventario/
├── src/main/java/com/laesquina/inventario
│   ├── InventarioApplication.java
│   ├── config/
│   │   ├── DataInitializer.java       Datos iniciales (roles, usuarios, categorías, productos)
│   │   ├── WebConfig.java             CORS, para que Angular (puerto 4200) consuma la API
│   │   └── RecursosConfig.java        Expone las fotos de perfil como archivos estáticos
│   ├── controller/
│   │   ├── ProductoController.java
│   │   ├── CategoriaController.java
│   │   ├── MovimientoController.java
│   │   ├── AsistenteController.java   POST /api/asistente/consultar
│   │   ├── AuthController.java        POST /api/auth/login (emite el JWT) y GET /api/auth/me
│   │   ├── UsuarioController.java     Gestión de usuarios (solo ADMIN)
│   │   ├── PerfilController.java      Perfil del usuario autenticado
│   │   ├── ManejadorErrores.java
│   │   └── ErrorRespuesta.java
│   ├── model/
│   │   ├── Producto.java, Categoria.java, MovimientoInventario.java, TipoMovimiento.java
│   │   ├── Usuario.java               Ampliado: nombre, apellidos, foto, activo
│   │   └── Rol.java
│   ├── repository/     Interfaces JpaRepository + consultas JPQL (@Query)
│   ├── security/        SecurityConfig, JwtService, JwtAuthenticationFilter,
│   │                    UsuarioDetailsService, RespuestasSeguridad
│   └── service/
│       ├── ProductoService.java, CategoriaService.java, MovimientoService.java (@Transactional)
│       └── AsistenteService.java      Arma el contexto real y consulta a Groq
├── fotos-perfil/        Carpeta donde se guardan las fotos de perfil subidas (generada en tiempo de ejecución)
├── frontend/             Proyecto Angular 21, independiente del backend
│   └── src/app/
│       ├── core/
│       │   ├── auth.ts, auth-interceptor.ts, auth-guard.ts   Sesión con JWT (token en sessionStorage)
│       │   ├── inventario.ts          Servicio centralizado: productos, categorías, usuarios, perfil
│       │   └── asistente-chat.ts      Estado del historial de chats (persiste al navegar entre vistas)
│       ├── layout/dashboard/          Sidebar contraíble + navbar con avatar/nombre
│       └── pages/
│           ├── login/, panel/, productos/, alertas/, asistente/, usuarios/, perfil/
└── docs/
    ├── GUIA_EQUIPO.md          Guía para ejecutar el proyecto en otra PC
    ├── INSTALACION_MYSQL.md    Instalación de MySQL 8.4 (Windows y macOS)
    ├── postman/                Colección y entorno de Postman
    └── sql/                    Consultas de verificación para MySQL Workbench
```
 