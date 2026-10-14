package com.laesquina.inventario.controller;

import com.laesquina.inventario.model.Producto;
import com.laesquina.inventario.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST de productos.
 * Expone los endpoints /api/productos y devuelve JSON.
 * Los permisos por rol se definen en SecurityConfig.
 */
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // GET /api/productos -> listar todos (ADMIN y OPERADOR)
    @GetMapping
    public List<Producto> listar() {
        return productoService.listarTodos();
    }

    // GET /api/productos/stock-bajo -> productos bajo el minimo (JPQL)
    // (se declara antes de /{id} para que no lo capture esa ruta)
    @GetMapping("/stock-bajo")
    public List<Producto> stockBajo() {
        return productoService.listarStockBajo();
    }

    // GET /api/productos/por-vencer?dias=30 -> proximos a vencer (JPQL)
    @GetMapping("/por-vencer")
    public List<Producto> porVencer(@RequestParam(defaultValue = "30") int dias) {
        return productoService.listarPorVencer(dias);
    }

    // GET /api/productos/{id} -> consultar uno
    @GetMapping("/{id}")
    public Producto obtener(@PathVariable Long id) {
        return productoService.obtenerPorId(id);
    }

    // POST /api/productos -> registrar (solo ADMIN)
    @PostMapping
    public ResponseEntity<Producto> crear(@Valid @RequestBody Producto producto) {
        Producto creado = productoService.crear(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // PUT /api/productos/{id} -> actualizar (solo ADMIN)
    @PutMapping("/{id}")
    public Producto actualizar(@PathVariable Long id, @Valid @RequestBody Producto producto) {
        return productoService.actualizar(id, producto);
    }

    // DELETE /api/productos/{id} -> eliminar (solo ADMIN)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
