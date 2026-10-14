package com.laesquina.inventario.service;

import com.laesquina.inventario.model.MovimientoInventario;
import com.laesquina.inventario.model.Producto;
import com.laesquina.inventario.model.TipoMovimiento;
import com.laesquina.inventario.repository.MovimientoInventarioRepository;
import com.laesquina.inventario.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Capa de servicio: movimientos de inventario (entradas y salidas).
 *
 * Registrar un movimiento implica DOS cambios que dependen entre si:
 * actualizar el stock del producto y guardar el movimiento. Por eso se usa
 * @Transactional: si todo sale bien se confirman ambos (commit); si ocurre un
 * error, se deshacen todos los cambios (rollback).
 */
@Service
public class MovimientoService {

    private final MovimientoInventarioRepository movimientoRepository;
    private final ProductoRepository productoRepository;

    public MovimientoService(MovimientoInventarioRepository movimientoRepository,
                             ProductoRepository productoRepository) {
        this.movimientoRepository = movimientoRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<MovimientoInventario> listarTodos() {
        return movimientoRepository.findAllByOrderByFechaDescIdDesc();
    }

    // RF05: historial de un producto (JPQL Q2).
    @Transactional(readOnly = true)
    public List<MovimientoInventario> historial(Long productoId) {
        if (!productoRepository.existsById(productoId)) {
            throw new NoSuchElementException("Producto no encontrado con id " + productoId);
        }
        return movimientoRepository.buscarHistorial(productoId);
    }

    /**
     * Registra UN movimiento. Transaccion: stock del producto + movimiento.
     */
    @Transactional
    public MovimientoInventario registrar(MovimientoInventario mov) {
        return aplicar(mov);
    }

    /**
     * Registra VARIOS movimientos (p. ej. la recepcion de un pedido) en UNA sola transaccion.
     *
     * Si cualquier item falla (producto inexistente, stock insuficiente, datos invalidos),
     * se lanza una excepcion y Spring hace ROLLBACK de TODO el lote: tambien se deshacen
     * los items anteriores que ya se habian aplicado. O se registra el pedido completo,
     * o no se registra nada.
     */
    @Transactional
    public List<MovimientoInventario> registrarLote(List<MovimientoInventario> lote) {
        if (lote == null || lote.isEmpty()) {
            throw new IllegalArgumentException("El lote debe tener al menos un movimiento");
        }
        List<MovimientoInventario> registrados = new ArrayList<>();
        for (int i = 0; i < lote.size(); i++) {
            try {
                registrados.add(aplicar(lote.get(i)));
            } catch (NoSuchElementException e) {
                throw new NoSuchElementException("Ítem " + (i + 1) + " del lote: " + e.getMessage()
                        + ". Se revirtió todo el lote");
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Ítem " + (i + 1) + " del lote: " + e.getMessage()
                        + ". Se revirtió todo el lote");
            }
        }
        return registrados;
    }

    // Logica comun: valida, actualiza el stock y guarda el movimiento.
    // Las excepciones son no verificadas (RuntimeException), por eso provocan rollback.
    private MovimientoInventario aplicar(MovimientoInventario mov) {
        if (mov == null || mov.getProducto() == null || mov.getProducto().getId() == null) {
            throw new IllegalArgumentException("El producto es obligatorio");
        }
        if (mov.getTipo() == null) {
            throw new IllegalArgumentException("El tipo de movimiento es obligatorio (ENTRADA o SALIDA)");
        }
        if (mov.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }

        Producto p = productoRepository.findById(mov.getProducto().getId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Producto no encontrado con id " + mov.getProducto().getId()));

        if (mov.getTipo() == TipoMovimiento.SALIDA) {
            if (p.getStock() < mov.getCantidad()) {
                throw new IllegalArgumentException("Stock insuficiente para " + p.getNombre()
                        + ": disponible " + p.getStock() + ", solicitado " + mov.getCantidad());
            }
            p.setStock(p.getStock() - mov.getCantidad());
        } else {
            p.setStock(p.getStock() + mov.getCantidad());
        }

        mov.setId(null);
        mov.setProducto(p);
        mov.setFecha(LocalDateTime.now());

        productoRepository.save(p);
        return movimientoRepository.save(mov);
    }
}
