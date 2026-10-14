package com.laesquina.inventario.controller;

import com.laesquina.inventario.model.Producto;
import com.laesquina.inventario.service.ProductoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST de productos.
 * Expone los endpoints /api/productos y devuelve JSON.
 */
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // GET /api/productos -> listar todos
    @GetMapping
    public List<Producto> listar() {
        return productoService.listarTodos();
    }

    // GET /api/productos/stock-bajo -> productos bajo el minimo
    // (se declara antes de /{id} para que no lo capture esa ruta)
    @GetMapping("/stock-bajo")
    public List<Producto> stockBajo() {
        return productoService.listarStockBajo();
    }

    // GET /api/productos/{id} -> consultar uno
    @GetMapping("/{id}")
    public Producto obtener(@PathVariable Long id) {
        return productoService.obtenerPorId(id);
    }

    // POST /api/productos -> registrar
    @PostMapping
    public ResponseEntity<Producto> crear(@RequestBody Producto producto) {
        Producto creado = productoService.crear(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // PUT /api/productos/{id} -> actualizar
    @PutMapping("/{id}")
    public Producto actualizar(@PathVariable Long id, @RequestBody Producto producto) {
        return productoService.actualizar(id, producto);
    }

    // DELETE /api/productos/{id} -> eliminar
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
