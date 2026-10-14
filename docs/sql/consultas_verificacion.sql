-- =====================================================================
-- La Esquina - Avance 2
-- Consultas para VERIFICAR en MySQL Workbench lo que hace la API.
-- Se usan en las capturas del rubro 10 (ver docs/GUIA_CAPTURAS.md).
-- Ejecutar cada bloque por separado (Ctrl + Enter sobre la consulta).
-- =====================================================================

USE laesquina;

-- E01 - Tablas creadas automaticamente por Hibernate (ddl-auto=update)
SHOW TABLES;

-- E01 - Estructura de la tabla producto (columnas y claves foraneas)
DESCRIBE producto;

-- E04 / E05 / E06 / E07 - Productos con su categoria
SELECT p.id, p.nombre, c.nombre AS categoria, p.precio, p.stock, p.stock_minimo,
       p.fecha_vencimiento, p.ubicacion
FROM producto p
JOIN categoria c ON c.id = p.id_categoria
ORDER BY p.id;

-- E09 - Equivalente SQL de la consulta JPQL Q1 (stock bajo)
SELECT id, nombre, stock, stock_minimo
FROM producto
WHERE stock <= stock_minimo
ORDER BY stock;

-- E11 - Equivalente SQL de la consulta JPQL Q3 (vencen en los proximos 30 dias o ya vencidos)
SELECT id, nombre, fecha_vencimiento, ubicacion
FROM producto
WHERE fecha_vencimiento IS NOT NULL
  AND fecha_vencimiento <= CURDATE() + INTERVAL 30 DAY
ORDER BY fecha_vencimiento;

-- E12 / E13 / E14 - Stock de los productos usados en las transacciones
SELECT id, nombre, stock FROM producto WHERE id IN (1, 2, 4, 6) ORDER BY id;

-- E10 / E12 / E13 / E14 - Movimientos registrados (historial)
SELECT m.id, m.fecha, m.tipo, m.cantidad, p.nombre AS producto
FROM movimiento_inventario m
JOIN producto p ON p.id = m.id_producto
ORDER BY m.fecha DESC, m.id DESC;

-- E14 - Cantidad de movimientos (antes y despues del lote: debe ser la misma)
SELECT COUNT(*) AS total_movimientos FROM movimiento_inventario;

-- E19 - Usuarios: la contrasena se guarda cifrada con BCrypt (empieza con $2a$)
SELECT u.id, u.username, u.password, r.nombre AS rol
FROM usuario u
JOIN rol r ON r.id = u.id_rol;

-- E19 - Roles definidos
SELECT * FROM rol;
