package com.laesquina.inventario.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Entidad Producto: representa un articulo del inventario.
 * Avance 1: se guardaba en una lista en memoria.
 * Avance 2: es una entidad JPA que Hibernate guarda en la tabla producto.
 */
@Entity
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nombre;

    @PositiveOrZero(message = "El precio no puede ser negativo")
    private double precio;

    @PositiveOrZero(message = "El stock no puede ser negativo")
    private int stock;

    @PositiveOrZero(message = "El stock mínimo no puede ser negativo")
    private int stockMinimo;

    // Fecha de vencimiento (opcional: hay productos que no vencen).
    private LocalDate fechaVencimiento;

    // Lugar fisico en la tienda o almacen, p. ej. "Pasillo 2 - Estante B".
    @Size(max = 60)
    @Column(length = 60)
    private String ubicacion;

    // Muchos productos pertenecen a una categoria (clave foranea id_categoria).
    @NotNull(message = "La categoría es obligatoria")
    @ManyToOne
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    public Producto() {
    }

    public Producto(String nombre, Categoria categoria, double precio, int stock, int stockMinimo,
                    LocalDate fechaVencimiento, String ubicacion) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
        this.fechaVencimiento = fechaVencimiento;
        this.ubicacion = ubicacion;
    }

    // Indica si el producto esta por debajo (o igual) de su stock minimo.
    public boolean isStockBajo() {
        return stock <= stockMinimo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }
}
