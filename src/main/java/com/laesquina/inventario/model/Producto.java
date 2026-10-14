package com.laesquina.inventario.model;

/**
 * Entidad Producto: representa un articulo del inventario.
 * En Fase 1 se guarda en memoria; en Fase 2 sera una entidad JPA.
 */
public class Producto {

    private Long id;
    private String nombre;
    private String categoria;
    private double precio;
    private int stock;
    private int stockMinimo;

    public Producto() {
    }

    public Producto(Long id, String nombre, String categoria, double precio, int stock, int stockMinimo) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
    }

    // Indica si el producto esta por debajo de su stock minimo.
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

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
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
}
