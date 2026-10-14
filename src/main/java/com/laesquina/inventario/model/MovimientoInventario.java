package com.laesquina.inventario.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

/**
 * Entidad MovimientoInventario: registra cada entrada o salida de un producto.
 * Es el historial (trazabilidad) que pedia el problema del Avance 1.
 * Tabla: movimiento_inventario.
 */
@Entity
@Table(name = "movimiento_inventario")
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Se guarda como texto ("ENTRADA" / "SALIDA") para que sea legible en MySQL.
    @NotNull(message = "El tipo de movimiento es obligatorio (ENTRADA o SALIDA)")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoMovimiento tipo;

    @Positive(message = "La cantidad debe ser mayor que cero")
    private int cantidad;

    // La asigna el servidor al registrar el movimiento.
    @Column(nullable = false)
    private LocalDateTime fecha;

    // Muchos movimientos pertenecen a un producto (clave foranea id_producto).
    @NotNull(message = "El producto es obligatorio")
    @ManyToOne
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    public MovimientoInventario() {
    }

    public MovimientoInventario(TipoMovimiento tipo, int cantidad, Producto producto) {
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.producto = producto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimiento tipo) {
        this.tipo = tipo;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }
}
