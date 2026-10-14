package com.laesquina.inventario.controller;

import com.laesquina.inventario.model.MovimientoInventario;
import com.laesquina.inventario.service.MovimientoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST de movimientos de inventario: /api/movimientos.
 * ADMIN y OPERADOR pueden consultar y registrar movimientos.
 */
@RestController
@RequestMapping("/api/movimientos")
public class MovimientoController {

    private final MovimientoService movimientoService;

    public MovimientoController(MovimientoService movimientoService) {
        this.movimientoService = movimientoService;
    }

    // GET /api/movimientos -> todos, del mas reciente al mas antiguo
    @GetMapping
    public List<MovimientoInventario> listar() {
        return movimientoService.listarTodos();
    }

    // GET /api/movimientos/producto/{id} -> historial de un producto (JPQL)
    @GetMapping("/producto/{id}")
    public List<MovimientoInventario> historial(@PathVariable Long id) {
        return movimientoService.historial(id);
    }

    // POST /api/movimientos -> registra una ENTRADA o SALIDA (transaccion)
    @PostMapping
    public ResponseEntity<MovimientoInventario> registrar(@Valid @RequestBody MovimientoInventario movimiento) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movimientoService.registrar(movimiento));
    }

    // POST /api/movimientos/lote -> registra varios movimientos: todos o ninguno (rollback)
    @PostMapping("/lote")
    public ResponseEntity<List<MovimientoInventario>> registrarLote(@RequestBody List<MovimientoInventario> lote) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movimientoService.registrarLote(lote));
    }
}
